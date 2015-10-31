/*
 Copyright 2013-2015 Jason Leyba

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.github.jsdossier;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Verify.verify;
import static com.google.javascript.jscomp.NodeTraversal.traverseTyped;

import com.github.jsdossier.annotations.Input;
import com.github.jsdossier.jscomp.DossierCompiler;
import com.github.jsdossier.jscomp.JsDoc;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.Var;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.EnumElementType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.NamedType;
import com.google.javascript.rhino.jstype.NoType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.Property;
import com.google.javascript.rhino.jstype.PrototypeObjectType;
import com.google.javascript.rhino.jstype.ProxyObjectType;
import com.google.javascript.rhino.jstype.TemplateType;
import com.google.javascript.rhino.jstype.TemplatizedType;
import com.google.javascript.rhino.jstype.UnionType;
import com.google.javascript.rhino.jstype.Visitor;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A {@link CompilerPass} that collects the symbols the symbols to generate documentation for.
 */
final class DocPass implements CompilerPass {

  private static final Logger log = Logger.getLogger(DocPass.class.getName());

  private final AbstractCompiler compiler;
  private final TypeRegistry typeRegistry;
  private final FileSystem fileSystem;

  @Inject
  DocPass(
      DossierCompiler compiler,
      TypeRegistry typeRegistry,
      @Input FileSystem fileSystem) {
    this.compiler = compiler;
    this.typeRegistry = typeRegistry;
    this.fileSystem = fileSystem;
  }

  @Override
  public void process(Node externsRoot, Node root) {
    if (compiler.getErrorManager().getErrorCount() > 0) {
      return;
    }
    Externs externs = new Externs();
    traverseTyped(compiler, externsRoot, new ExternCollector(externs));
    traverseTyped(compiler, root, new TypeCollector(externs));
  }
  
  private static final class Externs {
    private final Map<String, JSType> byName = new HashMap<>();
    private final Set<JSType> types = new HashSet<>();
    
    public void addExtern(String name, JSType type) {
      byName.put(name, type);
      types.add(type);
    }
    
    public boolean isExtern(JSType type) {
      return types.contains(type);
    }
  }

  private class ExternCollector implements NodeTraversal.Callback, Visitor<Object> {
    private final Externs externs;

    private final Joiner joiner = Joiner.on('.');
    private final Set<JSType> seen = new HashSet<>();
    private final Deque<String> names = new ArrayDeque<>();

    private ExternCollector(Externs externs) {
      this.externs = externs;
    }

    @Override
    public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      return n.isBlock() && parent == null;
    }

    @Override
    public void visit(NodeTraversal t, Node n, Node parent) {
      verify(t.getScope().isGlobal());
      for (Var var : t.getScope().getAllSymbols()) {
        @Nullable JSType type = var.getNameNode().getJSType();
        if (type != null) {
          crawl(var.getName(), type);
        }
      }
    }

    private void crawl(String name, JSType type) {
      names.addLast(name);
      if (!seen.contains(type)) {
        type.visit(this);
      }

      if ((type.isNominalType() && !type.isInstanceType())
          || type.isNominalConstructor()
          || type.isEnumType()) {
        String qualifiedName = joiner.join(names);
        externs.addExtern(qualifiedName, type);
      }
      names.removeLast();
    }

    @Override
    public Object caseFunctionType(FunctionType type) {
      for (String name : type.getOwnPropertyNames()) {
        if (!"apply".equals(name)
            && !"bind".equals(name)
            && !"call".equals(name)
            && !"prototype".equals(name)) {
          crawl(name, type.getPropertyType(name));
        }
      }
      return null;
    }

    @Override
    public Object caseObjectType(ObjectType type) {
      if (type.isGlobalThisType()) {
        return null;
      }

      for (String name : type.getOwnPropertyNames()) {
        if (type.getPropertyType(name).isEnumElementType()) {
          continue;
        }
        crawl(name, type.getPropertyType(name));
      }
      return null;
    }

    @Override public Object caseProxyObjectType(ProxyObjectType type) { return null; }
    @Override public Object caseNoType(NoType type) { return null; }
    @Override public Object caseEnumElementType(EnumElementType type) { return null; }
    @Override public Object caseAllType() { return null; }
    @Override public Object caseBooleanType() { return null; }
    @Override public Object caseNoObjectType() { return null; }
    @Override public Object caseUnknownType() { return null; }
    @Override public Object caseNullType() { return null; }
    @Override public Object caseNamedType(NamedType type) { return null; }
    @Override public Object caseNumberType() { return null; }
    @Override public Object caseStringType() { return null; }
    @Override public Object caseVoidType() { return null; }
    @Override public Object caseUnionType(UnionType type) { return null; }
    @Override public Object caseTemplatizedType(TemplatizedType type) { return null; }
    @Override public Object caseTemplateType(TemplateType templateType) { return null; }
  }

  /**
   * Traverses the object graph collecting all type definitions.
   */
  private class TypeCollector implements NodeTraversal.Callback, Visitor<Object> {

    private final Externs externs;
    private final LinkedList<NominalType> types = new LinkedList<>();

    public TypeCollector(Externs externs) {
      this.externs = externs;
    }

    @Override
    public boolean shouldTraverse(NodeTraversal t, Node n, @Nullable Node parent) {
      if (null == parent && n.isBlock()) {
        return true;
      }

      if (n.isScript() && null != parent && parent.isBlock()) {
        if (null != n.getJSDocInfo()) {
          Path path = fileSystem.getPath(n.getSourceFileName());
          typeRegistry.recordFileOverview(path, JsDoc.from(n.getJSDocInfo()));
        }
      }
      return false;
    }

    @Override
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (!t.getScope().isGlobal()) {
        return;
      }

      for (Var var : t.getScope().getAllSymbols()) {
        String name = var.getName();
        if (TypeRegistry.isInternalNamespaceVar(name)) {
          logfmt("Skipping internal compiler namespace %s", name);
          continue;
        }

        @Nullable Node node = var.getNameNode();
        if (node == null) {
          logfmt("Skipping type without a source node: %s", name);
          continue;
        }
        @Nullable JSType type = node.getJSType();

        @Nullable ModuleDescriptor module = typeRegistry.getModuleDescriptor(name);
        @Nullable JSDocInfo info = var.getJSDocInfo();
        if ((info == null || (module != null && isNullOrEmpty(info.getOriginalCommentString())))
            && type != null) {
          info = type.getJSDocInfo();
        }

        if (type == null) {
          logfmt("Unable to determine type for %s; skipping type", name);
          continue;
        } else if (type.isGlobalThisType()) {
          logfmt("Skipping global this");
          continue;
        } else if (isPrimitive(type) && (info == null || info.getTypedefType() == null)) {
          logfmt("Skipping primitive type assigned to %s: %s", name, type);
          continue;
        }

        crawl(name, info, node, type, module);
      }
    }

    void crawl(
        String name, JSDocInfo info, Node node, JSType jsType, @Nullable ModuleDescriptor module) {
      NominalType.TypeDescriptor descriptor = typeRegistry.findTypeDescriptor(jsType);

      // If we've already crawled the type, we know it's documentable.
      if (descriptor != null) {
        NominalType type = new NominalType(null, name, descriptor, node, JsDoc.from(info), module);
        typeRegistry.addType(type);
        logfmt("Found type alias: %s -> %s", name, jsType);

      } else {
        defineType(new NominalType(
            types.peek(),
            name,
            new NominalType.TypeDescriptor(jsType),
            node,
            JsDoc.from(info),
            module));
      }
    }

    private void defineType(NominalType type) {
      JSType jsType = type.getJsType();
      if (jsType.isConstructor() && externs.isExtern(jsType)) {
        logfmt("Not documenting extern constructor alias: %s -> %s", type.getName(), jsType);
        return;
      }

      // Check if this is a namespace the type registry knows about. Otherwise, the type is likely
      // an object confused for a namespace.
      if (!jsType.isConstructor()
          && !jsType.isInterface()
          && !jsType.isEnumType()
          && !typeRegistry.hasNamespace(type.getQualifiedName())
          && !(type.getJsdoc() != null && type.getJsdoc().isTypedef())) {
        logfmt("Ignorning undeclared namespace %s", type.getQualifiedName());
        return;
      }

      try {
        types.push(type);
        typeRegistry.addType(type);
        type.getJsType().visit(this);
      } finally {
        types.pop();
      }
    }

    private void crawlProperty(Property property) {
      NominalType parent = types.peek();

      JSDocInfo info = property.getJSDocInfo();
      if (info == null && !isTheObjectType(property.getType())) {
        info = property.getType().getJSDocInfo();
      }
      JsDoc jsdoc = JsDoc.from(info);

      if (jsdoc != null && jsdoc.isTypedef()) {
        JSType typedefType = typeRegistry.evaluate(jsdoc.getInfo().getTypedefType());
        NominalType child = new NominalType(
            parent,
            property.getName(),
            new NominalType.TypeDescriptor(typedefType),
            property.getNode(),
            jsdoc,
            null);
        typeRegistry.addType(child);
        return;
      }

      JSType propertyType = property.getType();
      if (propertyType.isInstanceType() && jsdoc != null && jsdoc.isConstructor()) {
        JSType ctor = ((PrototypeObjectType) propertyType).getConstructor();
        if (ctor instanceof FunctionType && parent.getJsType().equals(ctor)) {
          propertyType = ctor;
        }
      }

      if (propertyType.isConstructor()) {
        // If jsdoc is present and says this is not a constructor, we've found a
        // constructor reference, which should not be documented as a unique nominal type:
        //     /** @type {function(new: Foo)} */ var x;
        //     /** @private {function(new: Foo)} */ var x;
        //
        // We do not check jsdoc.isConstructor() since the Closure compiler may create a stub
        // JSDocInfo entry as part of one of its passes, i.e. rewriting a goog.module and an
        // exported property is an internal class:
        //     goog.module('foo');
        //     /** @constructor */
        //     function Internal() {}
        //     exports.Public = Internal;
        //
        // The exception to the rule is if this property is exporting a constructor as part of
        // a CommonJS module's public API - then we document the symbol as a type.
        if (parent.isModuleExports() || isConstructorTypeDefinition(propertyType, jsdoc)) {
          recordPropertyAsNestedType(property);
        } else {
          parent.addProperty(property);
        }

      } else if (propertyType.isInterface() || propertyType.isEnumType()) {
        recordPropertyAsNestedType(property);

      } else if (!propertyType.isInstanceType()
          && propertyType instanceof PrototypeObjectType
          && (typeRegistry.hasNamespace(parent.getQualifiedName() + "." + property.getName())
          || typeRegistry.findTypeDescriptor(propertyType) != null)) {
        recordPropertyAsNestedType(property);
        if (property.getType().isOrdinaryFunction()) {
          types.peek().addProperty(property);
        }
      } else {
        parent.addProperty(property);
      }
    }

    private boolean isConstructorTypeDefinition(JSType type, JsDoc jsdoc) {
      if (type.isConstructor()) {
        if (jsdoc == null || jsdoc.isConstructor()) {
          return true;
        }
        return jsdoc.isConst()
            && !hasTypeExpression(jsdoc.getMarker(JsDoc.Annotation.TYPE))
            && !hasTypeExpression(jsdoc.getMarker(JsDoc.Annotation.PUBLIC))
            && !hasTypeExpression(jsdoc.getMarker(JsDoc.Annotation.PROTECTED))
            && !hasTypeExpression(jsdoc.getMarker(JsDoc.Annotation.PRIVATE));
      }
      return false;
    }

    private boolean hasTypeExpression(Optional<JSDocInfo.Marker> marker) {
      return marker.isPresent() && marker.get().getType() != null;
    }

    private void recordPropertyAsNestedType(Property property) {
      String qualifiedName = types.peek().getQualifiedName(true)
          + "." + property.getName();
      ModuleDescriptor module = typeRegistry.getModuleDescriptor(qualifiedName);

      NominalType.TypeDescriptor seen = typeRegistry.findTypeDescriptor(property.getType());
      if (seen != null) {
        logfmt("Found type alias as property: %s -> %s", property.getName(), property.getType());
        NominalType child = new NominalType(
            types.peek(),
            property.getName(),
            seen,
            property.getNode(),
            JsDoc.from(property.getJSDocInfo()),
            module);
        typeRegistry.addType(child);
        return;
      }

      JSDocInfo info = property.getJSDocInfo();
      if (info != null
          && info.isConstant()
          && property.getType().getJSDocInfo() != null) {
        info = property.getType().getJSDocInfo();
      }

      defineType(new NominalType(
          types.peek(),
          property.getName(),
          new NominalType.TypeDescriptor(property.getType()),
          property.getNode(),
          JsDoc.from(info),
          module));
    }

    @Override
    public Boolean caseFunctionType(FunctionType type) {
      for (String name : type.getOwnPropertyNames()) {
        if (!"apply".equals(name)
            && !"bind".equals(name)
            && !"call".equals(name)
            && !"prototype".equals(name)) {
          crawlProperty(type.getOwnSlot(name));
        }
      }
      return null;
    }

    @Override
    public Boolean caseObjectType(ObjectType type) {
      if (type.isGlobalThisType()) {
        return null;
      }
      for (String name : type.getOwnPropertyNames()) {
        if (!"prototype".equals(name)) {
          crawlProperty(type.getOwnSlot(name));
        }
      }
      return null;
    }

    @Override public Object caseProxyObjectType(ProxyObjectType type) { return null; }
    @Override public Object caseNoType(NoType type) { return null; }
    @Override public Object caseEnumElementType(EnumElementType type) { return null; }
    @Override public Object caseAllType() { return null; }
    @Override public Object caseBooleanType() { return null; }
    @Override public Object caseNoObjectType() { return null; }
    @Override public Object caseUnknownType() { return null; }
    @Override public Object caseNullType() { return null; }
    @Override public Object caseNamedType(NamedType type) { return null; }
    @Override public Object caseNumberType() { return null; }
    @Override public Object caseStringType() { return null; }
    @Override public Object caseVoidType() { return null; }
    @Override public Object caseUnionType(UnionType type) { return null; }
    @Override public Object caseTemplatizedType(TemplatizedType type) { return null; }
    @Override public Object caseTemplateType(TemplateType templateType) { return null; }
  }

  private static void logfmt(String msg, Object... args) {
    if (log.isLoggable(Level.FINE)) {
      log.fine(String.format(msg, args));
    }
  }

  private static boolean isTheObjectType(JSType type) {
    if (!type.isInstanceType()) {
      return false;
    }
    ObjectType obj = type.toObjectType();
    return obj.getConstructor().isNativeObjectType()
        && "Object".equals(obj.getConstructor().getReferenceName());
  }

  private static boolean isPrimitive(JSType type) {
    return !type.isEnumElementType()
        && (type.isBooleanValueType()
        || type.isBooleanObjectType()
        || type.isNumber()
        || type.isNumberValueType()
        || type.isNumberObjectType()
        || type.isString()
        || type.isStringObjectType()
        || type.isStringValueType()
        || type.isVoidType()
        || type.isArrayType());
  }

}
