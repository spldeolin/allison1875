package com.spldeolin.allison1875.base.classloader;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import com.spldeolin.allison1875.base.BaseConfig;

/**
 * 为module提供com.github.javaparser.symbolsolver.JavaSymbolSolver 对象的工厂
 *
 * @author Deolin 2020-05-02
 */
public class ModuleJavaSymbolSolverFactory {

    public static JavaSymbolSolver getJavaSymbolSolver(SourceRoot sourceRoot) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        if (BaseConfig.getInstace().getWithClassLoader()) {
            ClassLoader classLoader = ModuleClassLoaderFactory.getClassLoader(sourceRoot);
            if (classLoader != null) {
                combinedTypeSolver.add(new ClassLoaderTypeSolver(classLoader));
            } else {
                // 因为某些原因无法类加载，只能使用JavaParserTypeSolver
                combinedTypeSolver.add(new JavaParserTypeSolver(sourceRoot.getRoot()));
                combinedTypeSolver.add(new ReflectionTypeSolver());
            }
        } else {
            combinedTypeSolver.add(new JavaParserTypeSolver(sourceRoot.getRoot()));
            combinedTypeSolver.add(new ReflectionTypeSolver());
        }

        return new JavaSymbolSolver(combinedTypeSolver);
    }

}
