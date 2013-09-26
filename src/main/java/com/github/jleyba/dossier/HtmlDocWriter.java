// Copyright 2013 Jason Leyba
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.github.jleyba.dossier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nullable;

/**
 * Generates HTML documentation.
 */
class HtmlDocWriter implements DocWriter {

  private final Config config;
  private final DocRegistry docRegistry;
  private final LinkResolver resolver;
  private SoyTofu tofu;
  private Iterable<Descriptor> sortedTypes;
  private Iterable<Path> sortedFiles;

  HtmlDocWriter(Config config, DocRegistry registry, LinkResolver resolver) {
    this.config = checkNotNull(config);
    this.docRegistry = checkNotNull(registry);
    this.resolver = checkNotNull(resolver);
  }

  @Override
  public void generateDocs(final JSTypeRegistry registry) throws IOException {
    tofu = new SoyFileSet.Builder()
        .add(HtmlDocWriter.class.getResource("/dossier.soy"))
        .build()
        .compileToTofu();

    sortedTypes = FluentIterable.from(docRegistry.getTypes())
        .toSortedList(DescriptorNameComparator.INSTANCE);
    sortedFiles = FluentIterable.from(config.getSources())
        .toSortedList(PathComparator.INSTANCE);

    Files.createDirectories(config.getOutput());
    copyResources();
    copySourceFiles();

    for (Descriptor descriptor : sortedTypes) {
      generateDocs(descriptor, registry);
    }

    writeTypesJson();
  }

  private void generateDocs(Descriptor descriptor, JSTypeRegistry registry) throws IOException {
    Path output = resolver.getFilePath(descriptor);
    Files.createDirectories(output.getParent());

    SoyMapData sideNavData = buildSideNavData(output);
    SoyMapData desc = new SoyMapData(
        "name", descriptor.getFullName(),
        "isClass", descriptor.isConstructor(),
        "isInterface", descriptor.isInterface(),
        "isEnum", descriptor.isEnum(),
        "sourceLink", getSourcePath(descriptor),
        "descriptionHtml", CommentUtil.getBlockDescription(resolver, descriptor.getInfo()),
        "typedefs", getTypeDefs(descriptor),
        "nested", new SoyMapData(
            "interfaces", getNestedTypeSummaries(descriptor, isInterface()),
            "classes", getNestedTypeSummaries(descriptor, isClass()),
            "enums", getNestedTypeSummaries(descriptor, isEnum())),
        "prototype", getPrototypeData(descriptor, registry),
        "static", getStaticData(descriptor));

    if (descriptor.isDeprecated()) {
      desc.put("isDeprecated", true);
      desc.put("deprecationHtml", CommentUtil.formatCommentText(
          resolver, descriptor.getDeprecationReason()));
    }

    if (descriptor.isEnum() && descriptor.getInfo() != null) {
      JSDocInfo info = descriptor.getInfo();
      String type = CommentUtil.formatTypeExpression(info.getEnumParameterType(), resolver);
      desc.put("enumType", type);
    }

    desc.put("inheritedTypes", getInheritedTypes(descriptor, registry));

    Set<String> interfaces = descriptor.isInterface()
        ? descriptor.getExtendedInterfaces(registry)
        : descriptor.getImplementedInterfaces(registry);
    desc.put("interfaces", transform(Ordering.natural().sortedCopy(interfaces),
        new Function<String, String>() {
          @Override
          public String apply(String input) {
            return new StringBuilder("<code>")
                .append(getTypeLink(input))
                .append("</code>")
                .toString();
          }
        }));

    if (descriptor.isConstructor()) {
      desc.put("ctor", getFunctionData(descriptor, true));
    }

    if (descriptor.isEnum()) {
      desc.put("enumValues", getEnumData(descriptor));
    }

    try (BufferedWriter writer = Files.newBufferedWriter(output, Charsets.UTF_8)) {
      tofu.newRenderer("dossier.typefile")
          .setData(new SoyMapData(
              "title", descriptor.getFullName(),
              "styleSheets", new SoyListData("dossier.css"),
              "scripts", new SoyListData("types.js", "dossier.js"),
              "descriptor", desc,
              "index", sideNavData))
          .render(writer);
    }
  }

  private void copyResources() throws IOException {
    FileSystem fs = config.getOutput().getFileSystem();
    copyResource(fs.getPath("/dossier.css"), config.getOutput());
    copyResource(fs.getPath("/dossier.js"), config.getOutput());
  }

  private static void copyResource(Path resourcePath, Path outputDir) throws IOException {
    try (InputStream stream = DocPass.class.getResourceAsStream(resourcePath.toString())) {
      Path outputPath = outputDir.resolve(resourcePath.getFileName());
      Files.copy(stream, outputPath, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private void writeTypesJson() throws IOException {
    JSONArray classes = new JSONArray();
    JSONArray enums = new JSONArray();
    JSONArray interfaces = new JSONArray();
    JSONArray namespaces = new JSONArray();

    for (Descriptor descriptor : sortedTypes) {
      String name = descriptor.getFullName();
      if (descriptor.isConstructor()) {
        classes.put(name);
      } else if (descriptor.isInterface()) {
        interfaces.put(name);
      } else if (descriptor.isEnum()) {
        enums.put(name);
      } else {
        namespaces.put(name);
      }
    }

    try {
      JSONObject json = new JSONObject()
          .put("classes", classes)
          .put("interfaces", interfaces)
          .put("enums", enums)
          .put("namespaces", namespaces);

      String content = "var TYPES = " + json + ";";

      Path outputPath = config.getOutput().resolve("types.js");

      if (Files.exists(outputPath)) {
        Files.delete(outputPath);
      }
      Files.write(outputPath, content.getBytes(Charsets.UTF_8));
    } catch (JSONException e) {
      throw new IOException(e);
    }
  }

  /**
   * Builds the SoyMapData for the side navigation pane.
   *
   * @param path Path to the file under the output directory that all navigation paths
   *     should be relative to.
   */
  private SoyMapData buildSideNavData(Path path) {
    SideNavData data = new SideNavData();
    for (Path source : sortedFiles) {
      Path displayPath = config.getSrcPrefix().relativize(source);
      Path sourcePath = config.getSourceOutput().resolve(displayPath.toString() + ".src.html");
      Path pathToSource = Paths.getRelativePath(path, sourcePath);
      data.addFile(displayPath.toString(), pathToSource);
    }

    for (Descriptor type : sortedTypes) {
      Path typePath = resolver.getFilePath(type);
      Path pathToType = Paths.getRelativePath(path, typePath);
      data.addType(type.getFullName(), pathToType, type.isInterface());
    }

    return data.toSoy();
  }

  @Nullable
  private String getSourcePath(Descriptor descriptor) {
    String strPath = descriptor.getSource();
    if (strPath == null) {
      return null;
    }
    Path path = config.getOutput().getFileSystem().getPath(strPath).toAbsolutePath().normalize();
    path = config.getSrcPrefix().relativize(path);
    path = config.getSourceOutput().resolve(path);
    path = path.resolveSibling(path.getFileName() + ".src.html");
    path = config.getOutput().relativize(path);

    int lineNum = descriptor.getLineNum();
    if (lineNum > 1) {
      return path + "#l" + (lineNum - 1);
    }
    return path.toString();
  }

  private Path getPathToOutputDir(Path from) {
    if (!Files.isDirectory(from)) {
      from = from.getParent();
    }
    Path path = from.getFileSystem().getPath("..");
    for (Iterator<Path> it = from.iterator(); it.hasNext(); it.next()) {
      path = path.resolve("..");
    }
    return path;
  }

  private void copySourceFiles() throws IOException {
    Path fileDir = config.getSourceOutput();
    for (Path source : config.getSources()) {
      Path simpleSource = config.getSrcPrefix().relativize(source);
      Path dest = fileDir.resolve(simpleSource.toString() + ".src.html");

      Path relativePath = getPathToOutputDir(simpleSource);
      Path toDossierCss = relativePath.resolve("dossier.css");
      Path toTypesJs = relativePath.resolve("types.js");
      Path toDossierJs = relativePath.resolve("dossier.js");

      Files.createDirectories(dest.getParent());
      try (BufferedWriter writer = Files.newBufferedWriter(dest, Charsets.UTF_8)) {
        tofu.newRenderer("dossier.srcfile")
            .setData(new SoyMapData(
                "title", source.getFileName().toString(),
                "styleSheets", new SoyListData(toDossierCss.toString()),
                "displayPath", simpleSource.toString(),
                "index", buildSideNavData(dest),
                "lines", new SoyListData(Files.readAllLines(source, Charsets.UTF_8)),
                "scripts", new SoyListData(toTypesJs.toString(), toDossierJs.toString())))
            .render(writer);
      }
    }
  }

  private SoyListData getInheritedTypes(Descriptor descriptor, JSTypeRegistry registry) {
    Stack<String> types = descriptor.getAllTypes(registry);
    SoyListData inheritedTypes = new SoyListData();
    while (!types.isEmpty()) {
      String type = types.pop();
      inheritedTypes.add(types.isEmpty() ? type : getTypeLink(type));
    }
    return inheritedTypes;
  }

  private SoyListData getEnumData(Descriptor descriptor) {
    SoyListData values = new SoyListData();
    ObjectType object = descriptor.toObjectType();
    for (String name : object.getOwnPropertyNames()) {
      JSType type = object.getPropertyType(name);
      if (type.isEnumElementType()) {
        Node node = object.getPropertyNode(name);
        JSDocInfo info = node == null ? null : node.getJSDocInfo();

        SoyMapData data = new SoyMapData(
            "name", name,
            "fullName", descriptor.getFullName() + "." + name);
        values.add(data);

        if (node == null || info == null) {
          continue;
        }

        if (info.isDeprecated()) {
          data.put("isDeprecated", true);
          data.put("deprecationHtml", CommentUtil.formatCommentText(
              resolver, info.getDeprecationReason()));
        }
        data.put("descriptionHtml", CommentUtil.getBlockDescription(resolver, info));
      }
    }
    return values;
  }

  private String getTypeLink(String type) {
    String path = resolver.getLink(type);
    if (path == null) {
      return type;
    }
    return String.format("<a href=\"%s\">%s</a>", path, type);
  }

  private SoyListData getTypeDefs(Descriptor descriptor) {
    List<Descriptor> typedefs = FluentIterable.from(descriptor.getProperties())
        .filter(isTypedef())
        .toSortedList(DescriptorNameComparator.INSTANCE);

    SoyListData typedefList = new SoyListData();
    for (Descriptor typedef : typedefs) {
      JSDocInfo info = checkNotNull(typedef.getInfo());

      SoyMapData typedefData = new SoyMapData(
          "name", typedef.getFullName(),
          "typeHtml", CommentUtil.formatTypeExpression(info.getTypedefType(), resolver),
          "href", getSourcePath(typedef),
          "descriptionHtml", CommentUtil.getBlockDescription(resolver, info));
      typedefList.add(typedefData);

      if (typedef.isDeprecated()) {
        typedefData.put("isDeprecated", typedef.isDeprecated());
        typedefData.put("deprecationHtml", CommentUtil.formatCommentText(resolver,
            typedef.getDeprecationReason()));
      }
    }
    return typedefList;
  }

  private SoyListData getNestedTypeSummaries(Descriptor descriptor, Predicate<Descriptor> predicate) {
    List<Descriptor> children = FluentIterable.from(descriptor.getProperties())
        .filter(predicate)
        .toSortedList(DescriptorNameComparator.INSTANCE);

    SoyListData types = new SoyListData();
    for (Descriptor child : children) {
      JSDocInfo info = checkNotNull(child.getInfo());
      String comment = CommentUtil.getBlockDescription(resolver, info);
      String summary = CommentUtil.getSummary(comment);
      types.add(new SoyMapData(
          "name", child.getFullName(),
          "href", resolver.getFilePath(child).getFileName().toString(),
          "summaryHtml", summary));
    }
    return types;
  }

  private SoyMapData getPrototypeData(Descriptor descriptor, JSTypeRegistry registry) {
    SoyListData prototypes = new SoyListData();
    SoyMapData prototypeData = new SoyMapData("chain", prototypes);

    if (!descriptor.isConstructor() && !descriptor.isInterface()) {
      return prototypeData;
    }

    List<Descriptor> seen = new LinkedList<>();
    for (String type : descriptor.getAssignableTypes(registry)) {
      Descriptor typeDescriptor = docRegistry.getType(type);
      if (typeDescriptor == null) {
        continue;
      }

      FluentIterable<Descriptor> unsorted = FluentIterable
          .from(typeDescriptor.getInstanceProperties());
      if (typeDescriptor != descriptor) {
        // Filter out properties that have been overridden.
        unsorted = unsorted.filter(notOwnPropertyOf(seen));
      }

      List<Descriptor> properties = unsorted.toSortedList(DescriptorNameComparator.INSTANCE);
      seen.add(typeDescriptor);
      if (properties.isEmpty()) {
        continue;
      }

      SoyListData props = new SoyListData();
      SoyListData methods = new SoyListData();
      SoyMapData propertySet = new SoyMapData(
          "name", typeDescriptor.getFullName(),
          "methods", methods,
          "properties", props);
      prototypes.add(propertySet);
      if (typeDescriptor != descriptor) {
        propertySet.put("href", resolver.getLink(typeDescriptor.getFullName()));
      }

      for (Descriptor property : properties) {
        if (isProperty(property)) {
          prototypeData.put("hasProperties", true);
          props.add(getPropertyData(property, false));
        } else if (isFunction(property)) {
          prototypeData.put("hasMethods", true);
          methods.add(getFunctionData(property, false));
        }
      }
    }
    return prototypeData;
  }

  private SoyMapData getPropertyData(Descriptor property, boolean useFullName) {
    SoyMapData data = new SoyMapData(
        "fullName", property.getFullName(),
        "name", property.getName(),
        "href", getSourcePath(property),
        "frag", useFullName ? property.getFullName() : property.getFullName()
            .replace("." + property.getName(), "$" + property.getName()));

    if (property.isDeprecated()) {
      data.put("isDeprecated", true);
      data.put("deprecationHtml", CommentUtil.formatCommentText(
          resolver, property.getDeprecationReason()));
    }

    JSDocInfo info = property.getInfo();
    if (info != null) {
      data.put("descriptionHtml", CommentUtil.getBlockDescription(resolver, info));
      if (info.getType() != null) {
        data.put("typeHtml", CommentUtil.formatTypeExpression(info.getType(), resolver));
      }
    } else if (property.getType() != null) {
      Descriptor propertyTypeDescriptor =
          docRegistry.getType(property.getType().toString());
      if (propertyTypeDescriptor == null) {
        propertyTypeDescriptor = docRegistry.getType(property.getType().toString());
      }

      if (propertyTypeDescriptor != null) {
        data.put("typeHtml", String.format("<a href=\"%s\">%s</a>",
            resolver.getLink(propertyTypeDescriptor.getFullName()),
            propertyTypeDescriptor.getFullName()));
      } else {
        data.put("typeHtml", property.getType().toString());
      }
    }

    return data;
  }

  private SoyMapData getStaticData(Descriptor descriptor) {
    SoyListData functions = new SoyListData();
    SoyListData properties = new SoyListData();

    List<Descriptor> props = FluentIterable.from(descriptor.getProperties())
        .toSortedList(DescriptorNameComparator.INSTANCE);
    for (Descriptor property : props) {
      if (isProperty(property)) {
        properties.add(getPropertyData(property, true));
      } else if (isFunction(property)) {
        functions.add(getFunctionData(property, true));
      }
    }
    return new SoyMapData(
        "functions", functions,
        "properties", properties);
  }

  private SoyMapData buildFunctionDetail(
      @Nullable String name, @Nullable String typeHtml, @Nullable String rawText) {
    return new SoyMapData(
        "name", name,
        "typeHtml", typeHtml,
        "descriptionHtml", CommentUtil.formatCommentText(resolver, rawText));
  }

  private SoyListData buildThrowsData(JSDocInfo info) {
    SoyListData throwsData = new SoyListData();
    // Manually scan the function markers so we can associated thrown types with the
    // document description for why that type would be thrown.
    for (JSDocInfo.Marker marker : info.getMarkers()) {
      if (!"throws".equals(marker.getAnnotation().getItem())) {
        continue;
      }

      @Nullable String thrownType = null;
      @Nullable String thrownDescription = null;
      if (marker.getType() != null) {
        JSTypeExpression expr = new JSTypeExpression(
            marker.getType().getItem(), info.getSourceName());
        thrownType = CommentUtil.formatTypeExpression(expr, resolver);
      }

      if (marker.getDescription() != null) {
        thrownDescription = marker.getDescription().getItem();
      }

      throwsData.add(buildFunctionDetail(null, thrownType, thrownDescription));
    }
    return throwsData;
  }

  private SoyMapData getFunctionData(Descriptor function, boolean useFullName) {
    SoyMapData data = new SoyMapData(
        "fullName", function.getFullName(),
        "name", function.getName(),
        "href", getSourcePath(function),
        "frag", useFullName ? function.getFullName()
            : function.getFullName().replace(
                "." + function.getName(),
                "$" + function.getName()));

    JSDocInfo info = function.getInfo();

    if (!function.isConstructor()) {
      data.put("returns", buildFunctionDetail(null,
          getReturnType(function),
          info == null ? null : info.getReturnDescription()));
    }

    if (info != null) {
      data.put("descriptionHtml", CommentUtil.getBlockDescription(resolver, info));
      data.put("throws", buildThrowsData(info));
      data.put("templateNames", info.getTemplateTypeNames());
    }

    if (function.isDeprecated()) {
      data.put("isDeprecated", true);
      data.put("deprecationHtml", CommentUtil.formatCommentText(
          resolver, function.getDeprecationReason()));
    }

    data.put("args", transform(function.getArgs(), new Function<ArgDescriptor, SoyMapData>() {
      @Override
      public SoyMapData apply(ArgDescriptor arg) {
        return buildFunctionDetail(
            arg.getName(),
            arg.getType() == null ? null
                : CommentUtil.formatTypeExpression(arg.getType(), resolver),
            arg.getDescription());
      }
    }));

    return data;
  }

  @Nullable
  private String getReturnType(Descriptor function) {
    JSDocInfo info = function.getInfo();
    if (info != null) {
      JSTypeExpression expr = info.getReturnType();
      if (expr != null) {
        return CommentUtil.formatTypeExpression(expr, resolver);
      }
    }
    JSType type = ((FunctionType) function.toObjectType()).getReturnType();
    return type == null ? null : type.toString();
  }

  private static Predicate<Descriptor> isTypedef() {
    return new Predicate<Descriptor>() {
      @Override
      public boolean apply(Descriptor input) {
        return input.getInfo() != null && input.getInfo().getTypedefType() != null;
      }
    };
  }

  private static Predicate<Descriptor> isClass() {
    return new Predicate<Descriptor>() {
      @Override
      public boolean apply(Descriptor input) {
        return input.isConstructor();
      }
    };
  }

  private static Predicate<Descriptor> isInterface() {
    return new Predicate<Descriptor>() {
      @Override
      public boolean apply(Descriptor input) {
        return input.isInterface();
      }
    };
  }

  private static Predicate<Descriptor> isEnum() {
    return new Predicate<Descriptor>() {
      @Override
      public boolean apply(Descriptor input) {
        return input.isEnum();
      }
    };
  }

  private boolean isFunction(Descriptor descriptor) {
    return descriptor.isFunction()
        && !descriptor.isConstructor()
        && !descriptor.isInterface();
  }

  private static Predicate<Descriptor> notOwnPropertyOf(final Iterable<Descriptor> descriptors) {
    return new Predicate<Descriptor>() {
      @Override
      public boolean apply(Descriptor input) {
        for (Descriptor descriptor : descriptors) {
          if (descriptor.hasOwnInstanceProprety(input.getName())) {
            return false;
          }
        }
        return true;
      }
    };
  }

  private boolean isProperty(Descriptor descriptor) {
    if (docRegistry.isKnownType(descriptor.getFullName())) {
      return false;
    }

    JSType type = descriptor.getType();
    if (type == null) {
      return true;
    }
    return !type.isFunctionPrototypeType()
        && !type.isEnumType()
        && !type.isEnumElementType()
        && !type.isFunctionType()
        && !type.isConstructor()
        && !type.isInterface();
  }

  private static enum DescriptorNameComparator implements Comparator<Descriptor> {
    INSTANCE;

    @Override
    public int compare(Descriptor a, Descriptor b) {
      return a.getFullName().compareTo(b.getFullName());
    }
  }

  private static enum PathComparator implements Comparator<Path> {
    INSTANCE;

    @Override
    public int compare(Path o1, Path o2) {
      return o1.toString().compareTo(o2.toString());
    }
  }
}
