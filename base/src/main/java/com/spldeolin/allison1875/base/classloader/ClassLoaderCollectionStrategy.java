package com.spldeolin.allison1875.base.classloader;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;

/**
 * @author Deolin 2019-12-27
 */
public class ClassLoaderCollectionStrategy extends SymbolSolverCollectionStrategy {

    public ClassLoaderCollectionStrategy(ClassLoader classLoader) {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ClassLoaderTypeSolver(classLoader));
        typeSolver.add(new ReflectionTypeSolver(false));
        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);

        super.getParserConfiguration().setSymbolResolver(javaSymbolSolver);
    }

}
