package com.github.jleyba.dossier;

import static com.google.common.collect.Lists.newLinkedList;
import static org.junit.Assert.assertEquals;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.CustomPassExecutionTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.file.FileSystems;
import java.util.Collection;
import java.util.List;

/**
 * Tests for {@link ProvidedSymbolsCollectionPass}.
 */
@RunWith(JUnit4.class)
public class ProvidedSymbolsCollectionPassTest {

  @Test
  public void collectsProvidedSymbols() {
    Compiler compiler = new Compiler(System.err);
    ProvidedSymbolsCollectionPass pass = new ProvidedSymbolsCollectionPass(compiler);
    CompilerUtil util = new CompilerUtil(compiler, createOptions(pass));

    util.compile(FileSystems.getDefault().getPath("foo/bar.js"),
        "goog.provide('Foo');",
        "goog.provide('foo.Bar');",
        "goog.provide('foo.bar.Baz');",
        "goog.provide('one.two.three.Four');");

    assertEquals(
        ImmutableSet.of(
            "Foo",
            "foo.Bar",
            "foo.bar.Baz",
            "one.two.three.Four"),
        pass.getSymbols());
  }

  private static CompilerOptions createOptions(CompilerPass pass) {
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
    CompilationLevel.ADVANCED_OPTIMIZATIONS.setTypeBasedOptimizationOptions(options);

    Multimap<CustomPassExecutionTime, CompilerPass> customPasses;
    customPasses = Multimaps.newListMultimap(
        Maps.<CustomPassExecutionTime, Collection<CompilerPass>>newHashMap(),
        new Supplier<List<CompilerPass>>() {
          @Override
          public List<CompilerPass> get() {
            return newLinkedList();
          }
        });
    customPasses.put(CustomPassExecutionTime.BEFORE_CHECKS, pass);
    options.setCustomPasses(customPasses);

    return options;
  }
}
