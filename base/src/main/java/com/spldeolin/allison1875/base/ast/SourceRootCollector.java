package com.spldeolin.allison1875.base.ast;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;

/**
 * SourceRoot对象的收集器
 *
 * @author Deolin 2020-05-01
 */
@Log4j2
class SourceRootCollector {

    Collection<SourceRoot> collect(Collection<String> projectPaths) {
        List<SourceRoot> result = Lists.newArrayList();
        for (String projectPath : projectPaths) {
            List<SourceRoot> sourceRoots = new SymbolSolverCollectionStrategy().collect(Paths.get(projectPath))
                    .getSourceRoots();
            result.addAll(sourceRoots);
        }
        return result;
    }

}
