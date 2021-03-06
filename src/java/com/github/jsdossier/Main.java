/*
 Copyright 2013-2016 Jason Leyba

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

import static com.github.jsdossier.jscomp.Types.isTypedef;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.io.Files.getFileExtension;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;

import com.github.jsdossier.annotations.Args;
import com.github.jsdossier.annotations.DocumentationScoped;
import com.github.jsdossier.annotations.Input;
import com.github.jsdossier.annotations.ModuleExterns;
import com.github.jsdossier.annotations.ModuleFilter;
import com.github.jsdossier.annotations.ModulePrefix;
import com.github.jsdossier.annotations.Modules;
import com.github.jsdossier.annotations.Output;
import com.github.jsdossier.annotations.Readme;
import com.github.jsdossier.annotations.SourcePrefix;
import com.github.jsdossier.annotations.SourceUrlTemplate;
import com.github.jsdossier.annotations.Stderr;
import com.github.jsdossier.annotations.Stdout;
import com.github.jsdossier.annotations.TypeFilter;
import com.github.jsdossier.jscomp.DossierCommandLineRunner;
import com.github.jsdossier.jscomp.DossierCompiler;
import com.github.jsdossier.jscomp.Module;
import com.github.jsdossier.jscomp.NominalType;
import com.github.jsdossier.jscomp.TypeRegistry;
import com.github.jsdossier.soy.Renderer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

final class Main {
  private Main() {}

  private static final Logger log = Logger.getLogger(Main.class.getName());

  private static final String INDEX_FILE_NAME = "index.html";

  private static final List<String> STANDARD_FLAGS = ImmutableList.of(
      "--jscomp_warning=accessControls",
      "--jscomp_warning=ambiguousFunctionDecl",
      "--jscomp_warning=checkRegExp",
      "--jscomp_warning=checkTypes",
      "--jscomp_warning=checkVars",
      "--jscomp_warning=constantProperty",
      "--jscomp_warning=deprecated",
      "--jscomp_warning=duplicateMessage",
      "--jscomp_warning=es5Strict",
      "--jscomp_warning=externsValidation",
      "--jscomp_warning=fileoverviewTags",
      "--jscomp_warning=globalThis",
      "--jscomp_warning=invalidCasts",
      "--jscomp_warning=missingProperties",
      "--jscomp_warning=nonStandardJsDocs",
      "--jscomp_warning=strictModuleDepCheck",
      "--jscomp_warning=typeInvalidation",
      "--jscomp_warning=undefinedVars",
      "--jscomp_warning=unknownDefines",
      "--jscomp_warning=uselessCode",
      "--jscomp_warning=visibility",
      "--third_party=false");

  private static final ExplicitScope DOCUMENTATION_SCOPE = new ExplicitScope();

  private static class DossierModule extends AbstractModule {

    private final Flags flags;
    private final Config config;
    private final Path outputDir;

    private DossierModule(Flags flags, Config config, Path outputDir) {
      this.flags = flags;
      this.config = config;
      this.outputDir = outputDir;
    }

    @Override
    protected void configure() {
      bindScope(DocumentationScoped.class, DOCUMENTATION_SCOPE);

      bindConstant().annotatedWith(Annotations.NumThreads.class).to(flags.numThreads);

      bind(PrintStream.class).annotatedWith(Stderr.class).toInstance(System.err);
      bind(PrintStream.class).annotatedWith(Stdout.class).toInstance(
          new PrintStream(ByteStreams.nullOutputStream()));

      bind(Key.get(new TypeLiteral<Optional<Path>>() {}, Readme.class))
          .toInstance(config.getReadme());
      bind(Key.get(new TypeLiteral<Iterable<Path>>() {}, Input.class))
          .toInstance(concat(config.getSources(), config.getModules()));
      bind(Key.get(new TypeLiteral<ImmutableSet<Path>>() {}, Modules.class))
          .toInstance(config.getModules());
      bind(Key.get(new TypeLiteral<ImmutableSet<Path>>() {}, ModuleExterns.class))
          .toInstance(config.getExternModules());
      bind(new TypeLiteral<ImmutableSet<MarkdownPage>>(){})
          .toInstance(config.getCustomPages());

      bind(Key.get(new TypeLiteral<Optional<Path>>() {}, ModulePrefix.class))
          .toInstance(config.getModulePrefix());
      bind(Key.get(Path.class, ModulePrefix.class))
          .toProvider(ModulePrefixProvider.class)
          .in(DocumentationScoped.class);
      bind(Path.class).annotatedWith(SourcePrefix.class).toInstance(config.getSrcPrefix());

      bind(Path.class).annotatedWith(Output.class).toInstance(outputDir);
      bind(FileSystem.class).annotatedWith(Output.class).toInstance(outputDir.getFileSystem());
      bind(FileSystem.class).annotatedWith(Input.class).toInstance(config.getFileSystem());

      bind(ModuleNamingConvention.class).toInstance(config.getModuleNamingConvention());

      bind(DocTemplate.class).to(DefaultDocTemplate.class).in(DocumentationScoped.class);
      bind(Renderer.class).in(DocumentationScoped.class);
    }

    @Provides
    @SourceUrlTemplate
    Optional<String> provideUrlTemplate() {
      return config.getSourceUrlTemplate();
    }

    @Provides
    @ModuleFilter
    Predicate<Path> provideModulePathFilter() {
      return new Predicate<Path>() {
        @Override
        public boolean apply(Path input) {
          return config.isFilteredModule(input);
        }
      };
    }

    @Provides
    @TypeFilter
    Predicate<String> provideTypeNameFilter() {
      return new Predicate<String>() {
        @Override
        public boolean apply(String input) {
          return config.isFilteredType(input);
        }
      };
    }

    @Provides
    @Input
    LanguageMode provideInputLanguage() {
      return config.getLanguage().toMode();
    }

    @Provides
    @Args
    String[] provideCompilerFlags() {
      Iterable<String> standardFlags = STANDARD_FLAGS;
      if (config.isStrict()) {
        standardFlags = transform(standardFlags, new Function<String, String>() {
          @Override
          public String apply(String input) {
            return input.replace("--jscomp_warning", "--jscomp_error");
          }
        });
      }

      ImmutableList<String> compilerFlags = ImmutableList.<String>builder()
          .addAll(transform(config.getSources(), toFlag("--js=")))
          .addAll(transform(config.getModules(), toFlag("--js=")))
          .addAll(transform(config.getExterns(), toFlag("--externs=")))
          .addAll(standardFlags)
          .build();
      return compilerFlags.toArray(new String[compilerFlags.size()]);
    }

    @Provides
    @DocumentationScoped
    NavIndexFactory provideNavIndexFactory(
        @Output Path outputDir,
        TypeRegistry typeRegistry) {
      boolean showModules = false;
      for (Module module : typeRegistry.getAllModules()) {
        if (module.getType() != Module.Type.CLOSURE) {
          showModules = true;
          break;
        }
      }

      boolean showTypes = false;
      for (NominalType type : typeRegistry.getAllTypes()) {
        if (!type.getModule().isPresent()
            || type.getModule().get().getType() == Module.Type.CLOSURE) {
          showTypes = true;
          break;
        }
      }
      return NavIndexFactory.create(
          outputDir.resolve(INDEX_FILE_NAME),
          showModules,
          showTypes,
          config.getCustomPages());
    }
  }

  private static Function<Path, String> toFlag(final String flagPrefix) {
    return new Function<Path, String>() {
      @Override
      public String apply(Path input) {
        return flagPrefix + input;
      }
    };
  }

  private static void print(Config config) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String header = " Configuration  ";
    int len = header.length();
    String pad = Strings.repeat("=", len / 2);

    System.err.println(pad + header + pad);
    System.err.println(gson.toJson(config.toJson()));
    System.err.println(Strings.repeat("=", 79));
  }

  private static void configureLogging() {
    Logger log = Logger.getLogger(Main.class.getPackage().getName());
    log.setLevel(Level.WARNING);
    log.addHandler(new Handler() {
      @Override
      public void publish(LogRecord record) {
        System.err.printf(
            "[%s][%s] %s\n",
            record.getLevel(),
            record.getLoggerName(),
            record.getMessage());
      }

      @Override public void flush() {}
      @Override public void close() {}
    });
  }

  @VisibleForTesting
  static int run(String[] args, FileSystem fileSystem) throws IOException {
    Flags flags = Flags.parse(args, fileSystem);
    Config config = Config.fromFlags(flags, fileSystem);

    if (flags.printConfig) {
      print(config);
      return 1;
    }

    Path output = config.getOutput();
    if ("zip".equals(getFileExtension(output.toString()))) {
      try (FileSystem outputFs = openZipFileSystem(output)) {
        output = outputFs.getPath("/");
        return run(flags, config, output);
      }
    }
    return run(flags, config, output);
  }

  private static FileSystem openZipFileSystem(Path zip) throws IOException {
    ImmutableMap<String, String> attributes = ImmutableMap.of(
        "create", "true",
        "encoding", UTF_8.displayName());
    if (zip.getFileSystem() == FileSystems.getDefault()) {
      URI uri = URI.create("jar:file:" + zip.toAbsolutePath());
      return FileSystems.newFileSystem(uri, attributes);
    }
    // An in-memory file system used for testing.
    return zip.getFileSystem().provider().newFileSystem(zip, attributes);
  }

  private static int run(Flags flags, Config config, Path outputDir) throws IOException {
    configureLogging();

    Injector injector = Guice.createInjector(
        new CompilerModule(),
        new DossierModule(flags, config, outputDir));

    DossierCommandLineRunner runner = injector.getInstance(DossierCommandLineRunner.class);
    if (!runner.shouldRunCompiler()) {
      return -1;
    }

    Instant start = Instant.now();
    System.out.println("Generating documentation...");

    int result = runner.call();
    if (result != 0) {
      System.out.println("Compilation failed; aborting...");
      return result;
    }

    TypeRegistry typeRegistry = injector.getInstance(TypeRegistry.class);
    DossierCompiler compiler = injector.getInstance(DossierCompiler.class);
    typeRegistry.computeTypeRelationships(compiler.getTopScope(), compiler.getTypeRegistry());

    try {
      DOCUMENTATION_SCOPE.enter();
      createDirectories(outputDir);
      DocTemplate template = injector.getInstance(DocTemplate.class);
      RenderTaskExecutor executor = injector.getInstance(RenderTaskExecutor.class)
          .renderIndex()
          .renderDocumentation(filter(typeRegistry.getAllTypes(), not(isTypedef())))
          .renderMarkdown(config.getCustomPages())
          .renderResources(concat(template.getCss(), template.getHeadJs(), template.getTailJs()));
      if (!config.getSourceUrlTemplate().isPresent()) {
        executor.renderSourceFiles(concat(config.getSources(), config.getModules()));
      }
      List<Path> files = executor.awaitTermination().get();
      if (log.isLoggable(Level.FINER)) {
        log.fine("Rendered:\n  " + Joiner.on("\n  ").join(files));
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Rendering was interrupted", e);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      }
      throw new RuntimeException(e.getCause());
    } finally {
      DOCUMENTATION_SCOPE.exit();
    }

    Instant stop = Instant.now();
    String output = new PeriodFormatterBuilder()
        .appendHours().appendSuffix("h")  // I hope not...
        .appendSeparator(" ")
        .appendMinutes().appendSuffix("m")
        .appendSeparator(" ")
        .appendSecondsWithOptionalMillis().appendSuffix("s")
        .toFormatter()
        .print(new Period(start, stop));

    System.out.println("Finished in " + output);
    return 0;
  }

  public static void main(String[] args) {
    int exitCode;
    try {
      exitCode = run(args, FileSystems.getDefault());
    } catch (IOException e) {
      e.printStackTrace(System.err);
      exitCode = 2;
    }
    System.exit(exitCode);
  }
}
