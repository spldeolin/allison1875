package com.spldeolin.allison1875.base.classloader;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import com.spldeolin.allison1875.base.BaseConfig;

/**
 * 为module提供com.github.javaparser.symbolsolver.JavaSymbolSolver 对象的工厂
 *
 * @author Deolin 2020-05-02
 */
public class ModuleJavaSymbolSolverFactory {

    public static JavaSymbolSolver getJavaSymbolSolver(SourceRoot sourceRoot) {
        TypeSolver typeSolver;
        if (BaseConfig.getInstace().getWithClassLoader()) {
            ClassLoader classLoader = ModuleClassLoaderFactory.getClassLoader(sourceRoot);
            if (classLoader != null) {
                typeSolver = new ClassLoaderTypeSolver(classLoader);
            } else {
                // 因为某些原因无法类加载，只能使用JavaParserTypeSolver
                typeSolver = new JavaParserTypeSolver(sourceRoot.getRoot());
            }
        } else {
            typeSolver = new JavaParserTypeSolver(sourceRoot.getRoot());
        }

        return new JavaSymbolSolver(typeSolver);
    }

}
