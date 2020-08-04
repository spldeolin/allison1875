package com.spldeolin.allison1875.base.collection.ast;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.CommentsCollection;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.classloader.ModuleJavaSymbolSolverFactory;
import lombok.extern.log4j.Log4j2;

/**
 * CompilationUnit对象的收集器
 *
 * @author Deolin 2020-02-03
 */
@Log4j2
class CompilationUnitCollector {

    Collection<CompilationUnit> collect(SourceRoot sourceRoot) {
        if (sourceRoot.toString().endsWith("src" + File.separator + "test" + File.separator + "java")) {
            return Lists.newArrayList();
        }

        JavaSymbolSolver symbolSolver = ModuleJavaSymbolSolverFactory.getJavaSymbolSolver();
        sourceRoot.getParserConfiguration().setSymbolResolver(symbolSolver);

        long start = System.currentTimeMillis();
        int count = 0;

        List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParseParallelized();
        Collection<CompilationUnit> result = Lists.newArrayListWithCapacity(parseResults.size());
        for (ParseResult<CompilationUnit> parseResult : parseResults) {
            if (parseResult.isSuccessful()) {
                parseResult.getResult().ifPresent(result::add);
                count++;
            } else {
                this.reportProblems(parseResult);
            }
        }
        if (count > 0) {
            log.info("CompilationUnit collected. [{} of {} at {} in {}ms]", count,
                    English.plural("CompilationUnit", count),
                    BaseConfig.getInstance().getCommonPart().relativize(sourceRoot.getRoot()),
                    System.currentTimeMillis() - start);
        }
        return result;
    }

    private void reportProblems(ParseResult<CompilationUnit> parseResult) {
        Optional<CommentsCollection> commentsCollection = parseResult.getCommentsCollection();
        String longestComment = "";
        if (commentsCollection.isPresent()) {
            Optional<LineComment> longestMight = commentsCollection.get().getLineComments().stream()
                    .max((o1, o2) -> Ints.compare(o1.toString().length(), o2.toString().length()));
            if (longestMight.isPresent()) {
                longestComment = longestMight.get().toString();
            }
        }
        for (Problem problem : parseResult.getProblems()) {
            log.warn("Parse with problems, ignore and continue. longestComment={}, message={}", longestComment,
                    problem.getVerboseMessage());
        }
    }

}
