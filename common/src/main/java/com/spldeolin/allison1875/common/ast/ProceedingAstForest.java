package com.spldeolin.allison1875.common.ast;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Lists;

/**
 * @author Deolin 2026-02-16
 */
public class ProceedingAstForest implements AstForest {

    private final ClassLoader classLoader;

    private final File sourceRoot;

    private final List<CompilationUnit> cus;

    public ProceedingAstForest(AstForest astForest) {
        this.classLoader = astForest.getClassLoader();
        this.sourceRoot = astForest.getSourceRoot().toFile();
        this.cus = Lists.newArrayList(astForest);
    }

    public ProceedingAstForest addUnflushedCus(List<CompilationUnit> unflushedCus) {
        for (CompilationUnit cu : unflushedCus) {
            this.cus.add(setSymbolResolver(cu));
        }
        return this;
    }

    public ProceedingAstForest addUnflushedCu(CompilationUnit unflushedCu) {
        this.cus.add(setSymbolResolver(unflushedCu));
        return this;
    }

    @Override
    public AstForest cloneWithResetting() {
        return this;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Path getSourceRoot() {
        return sourceRoot.toPath();
    }

    @Override
    public Optional<CompilationUnit> tryFindCu(String primaryTypeQualifier) {
        for (CompilationUnit cu : cus) {
            Optional<TypeDeclaration<?>> pt = cu.getPrimaryType();
            if (pt.isPresent()) {
                Optional<String> fullyQualifiedName = pt.get().getFullyQualifiedName();
                if (fullyQualifiedName.isPresent()) {
                    if (fullyQualifiedName.get().equals(primaryTypeQualifier)) {
                        return Optional.of(cu);
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Iterator<CompilationUnit> iterator() {
        return cus.iterator();
    }

    private CompilationUnit setSymbolResolver(CompilationUnit cu) {
        CompilationUnit parsed = StaticJavaParser.parse(cu.toString());
        parsed.setStorage(cu.getStorage().get().getPath(), cu.getStorage().get().getEncoding());
        return parsed;
    }

}
