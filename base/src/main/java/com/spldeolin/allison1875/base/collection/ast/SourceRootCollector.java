package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.extern.log4j.Log4j2;

/**
 * SourceRoot对象的收集器
 *
 * @author Deolin 2020-05-01
 */
@Log4j2
class SourceRootCollector {

    Collection<SourceRoot> collect(Collection<Path> projectPaths) {
        Collection<SourceRoot> result = Lists.newLinkedList();
        for (Path projectPath : projectPaths) {
            List<SourceRoot> sourceRoots = new SymbolSolverCollectionStrategy().collect(projectPath).getSourceRoots();
            result.addAll(sourceRoots);
        }
        for (SourceRoot sr : result) {
            log.info("SourceRoot collected. [{}]", BaseConfig.getInstace().getCommonPart().relativize(sr.getRoot()));
        }
        return result;
    }

}
