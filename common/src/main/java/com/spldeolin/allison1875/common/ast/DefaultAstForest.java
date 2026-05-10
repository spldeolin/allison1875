package com.spldeolin.allison1875.common.ast;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-01-19
 */
@Slf4j
public class DefaultAstForest implements AstForest {

    private final ClassLoader classLoader;

    private final File sourceRoot;

    public DefaultAstForest(ClassLoader classLoader, File sourceRoot) {
        this.classLoader = classLoader;
        try {
            this.sourceRoot = sourceRoot.getCanonicalFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // JavaParser全局设置ClassLoader SymbolSolver
        StaticJavaParser.getParserConfiguration().setLanguageLevel(LanguageLevel.JAVA_17)
                .setSymbolResolver(new JavaSymbolSolver(new ClassLoaderTypeSolver(classLoader)));
        Thread.currentThread().setContextClassLoader(classLoader); // TODO 这步是必须的吗？
        log.info("AstForest created, sourceRoot={}", sourceRoot);
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        // java files
        Iterator<File> javaFilesItr = FileUtils.iterateFiles(sourceRoot, new String[]{"java"}, true);
        // cus
        Iterator<CompilationUnit> cusItr = Iterators.transform(javaFilesItr, CompilationUnitUtils::parseJava);
        return cusItr;
    }

    @Override
    public AstForest cloneWithResetting() {
        return new DefaultAstForest(classLoader, sourceRoot);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Path getSourceRoot() {
        return sourceRoot.toPath();
    }

}
