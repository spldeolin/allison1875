package com.spldeolin.allison1875.base.ast;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.spldeolin.allison1875.base.util.ast.Locations;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-02-02
 */
@Log4j2
public class AstIterator implements Iterator<CompilationUnit> {

    private final ClassLoader primaryClassLoader;

    private final Iterator<Path> javaPaths;

    public AstIterator(ClassLoader primaryClassLoader, Set<Path> javaPaths) {
        this.primaryClassLoader = primaryClassLoader;
        this.javaPaths = javaPaths.iterator();
        StaticJavaParser.getConfiguration().setSymbolResolver(createSymbolSolver());
    }

    @Override
    public boolean hasNext() {
        return javaPaths.hasNext();
    }

    @Override
    public CompilationUnit next() {
        Path javaPath = javaPaths.next();
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaPath);
            log.debug("CompilationUnit@{} <- SourceCode {}", cu.hashCode(),
                    Locations.getStorage(cu).getSourceRoot().relativize(Locations.getAbsolutePath(cu)));
            return cu;
        } catch (ParseProblemException e) {
            log.warn("SourceCode parse causing problems [{}] [{}]", javaPath, e.getMessage());
        } catch (Exception e) {
            log.warn("SourceCode parse unsuccessfully [{}]", javaPath, e);
        }
        return null;
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