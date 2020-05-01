package com.spldeolin.allison1875.base.collection.ast;


import java.util.Collection;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.classloader.ModuleJavaSymbolSolverFactory;
import lombok.extern.log4j.Log4j2;

/**
 * CompilationUnit对象的收集器
 *
 * @author Deolin 2020-02-03
 */
@Log4j2
public class CompilationUnitCollector {

    public Collection<CompilationUnit> collect(SourceRoot sourceRoot) {
        long start = System.currentTimeMillis();
        int count = 0;

        JavaSymbolSolver symbolSolver = ModuleJavaSymbolSolverFactory.getClassLoader(sourceRoot);
        sourceRoot.getParserConfiguration().setSymbolResolver(symbolSolver);

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
