package com.spldeolin.allison1875.base.classloader;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;

/**
 * 为module提供com.github.javaparser.symbolsolver.JavaSymbolSolver 对象的工厂
 *
 * @author Deolin 2020-05-02
 */
public class ModuleJavaSymbolSolverFactory {

    public static JavaSymbolSolver getJavaSymbolSolver() {
        ClassLoaderTypeSolver classLoaderTypeSolver = new ClassLoaderTypeSolver(
                ModuleJavaSymbolSolverFactory.class.getClassLoader());
        return new JavaSymbolSolver(classLoaderTypeSolver);
    }

}
