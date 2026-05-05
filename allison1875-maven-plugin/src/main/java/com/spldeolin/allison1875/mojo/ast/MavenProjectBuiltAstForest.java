package com.spldeolin.allison1875.mojo.ast;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-01-19
 */
@Slf4j
public class MavenProjectBuiltAstForest implements AstForest {

    private final ClassLoader classLoader;

    private final Set<File> sourceRoots;

    private final File primarySourceRoot;

    public MavenProjectBuiltAstForest(ClassLoader classLoader, Set<File> sourceRoots, File primarySourceRoot) {
        Preconditions.checkNotNull(classLoader, "required Argument 'classLoader' must not be null");
        Preconditions.checkNotNull(sourceRoots, "required Argument 'sourceRoots' must not be null");
        Preconditions.checkArgument(!sourceRoots.isEmpty(), "sourceRoots must not be empty");
        Preconditions.checkNotNull(primarySourceRoot, "required Argument 'primarySourceRoot' must not be null");
        Set<File> canonicalRoots = new LinkedHashSet<>();
        for (File sr : sourceRoots) {
            try {
                canonicalRoots.add(sr.getCanonicalFile());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        this.sourceRoots = canonicalRoots;
        try {
            this.primarySourceRoot = primarySourceRoot.getCanonicalFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.classLoader = classLoader;
        StaticJavaParser.getParserConfiguration().setLanguageLevel(LanguageLevel.JAVA_17)
                .setSymbolResolver(new JavaSymbolSolver(new ClassLoaderTypeSolver(classLoader)));
        Thread.currentThread().setContextClassLoader(classLoader);
        log.info("AstForest created, sourceRoots={}", this.sourceRoots);
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        Iterator<CompilationUnit> combined = Iterators.concat();
        for (File sourceRoot : sourceRoots) {
            if (!sourceRoot.exists()) {
                log.warn("sourceRoot does not exist, skip: {}", sourceRoot);
                continue;
            }
            Iterator<File> javaFilesItr = FileUtils.iterateFiles(sourceRoot, new String[]{"java"}, true);
            Iterator<CompilationUnit> cusItr = Iterators.transform(javaFilesItr, CompilationUnitUtils::parseJava);
            combined = Iterators.concat(combined, cusItr);
        }
        return combined;
    }

    @Override
    public AstForest cloneWithResetting() {
        return new MavenProjectBuiltAstForest(classLoader, sourceRoots, primarySourceRoot);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Set<Path> getSourceRoots() {
        Set<Path> paths = new LinkedHashSet<>();
        for (File sr : sourceRoots) {
            paths.add(sr.toPath());
        }
        return paths;
    }

    @Override
    public Path getPrimarySourceRoot() {
        return primarySourceRoot.toPath();
    }

}
