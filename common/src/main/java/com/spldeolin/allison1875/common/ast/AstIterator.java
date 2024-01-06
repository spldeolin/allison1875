package com.spldeolin.allison1875.common.ast;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.spldeolin.allison1875.common.util.ast.Cus;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-02-02
 */
@Log4j2
public class AstIterator implements Iterator<CompilationUnit> {

    private final ClassLoader primaryClassLoader;

    private final Iterator<File> javaFiles;

    public AstIterator(ClassLoader primaryClassLoader, Set<File> javaFiles) {
        this.primaryClassLoader = primaryClassLoader;
        this.javaFiles = javaFiles.iterator();
        StaticJavaParser.getConfiguration().setSymbolResolver(createSymbolSolver());
    }

    @Override
    public boolean hasNext() {
        return javaFiles.hasNext();
    }

    @Override
    public CompilationUnit next() {
        File javaFile = javaFiles.next();
        return Cus.parseCu(javaFile);
    }

    private SymbolResolver createSymbolSolver() {
        SymbolResolver symbolSolver = new JavaSymbolSolver(new ClassLoaderTypeSolver(primaryClassLoader));
        return symbolSolver;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}