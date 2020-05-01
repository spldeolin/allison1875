package com.spldeolin.allison1875.base.collection.ast;


import java.util.Collection;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.classloader.MavenProjectClassLoaderFactory;
import lombok.extern.log4j.Log4j2;

/**
 * CompilationUnit对象的收集器（基于GlobalCollectionStrategy提供的策略收集）
 *
 * @author Deolin 2020-02-03
 */
@Log4j2
public class CompilationUnitCollector {

    public Collection<CompilationUnit> collect(SourceRoot sourceRoot) {
        long start = System.currentTimeMillis();
        int count = 0;

        TypeSolver typeSolver;
        if (BaseConfig.getInstace().getWithClassLoader()) {
            ClassLoader classLoader = MavenProjectClassLoaderFactory.getClassLoader(sourceRoot);
            if (classLoader != null) {
                typeSolver = new ClassLoaderTypeSolver(classLoader);
            } else {
                // 因为某些原因无法类加载，只能使用JavaParserTypeSolver
                typeSolver = new JavaParserTypeSolver(sourceRoot.getRoot());
            }
        } else {
            typeSolver = new JavaParserTypeSolver(sourceRoot.getRoot());
        }

        sourceRoot.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));

        Collection<CompilationUnit> result = Lists.newLinkedList();
        for (ParseResult<CompilationUnit> parseResult : sourceRoot.tryToParseParallelized()) {
            if (parseResult.isSuccessful()) {
                parseResult.getResult().ifPresent(result::add);
                count++;
            } else {
                log.warn("Parse with problems, ignore and continue. [{}]", parseResult.getProblems());
            }
        }

        if (count > 0) {
            log.info("(Detail) {} CompilationUnit has parsed and collected from [{}] elapsing {}ms.", count,
                    sourceRoot.getRoot(), System.currentTimeMillis() - start);
        }
        return result;
    }

}
