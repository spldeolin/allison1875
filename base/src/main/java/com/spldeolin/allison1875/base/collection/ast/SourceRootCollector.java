package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
import com.github.javaparser.utils.ParserCollectionStrategy;
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

    Collection<SourceRoot> collect(Collection<Path> projectPaths) {
        Collection<SourceRoot> result = Lists.newLinkedList();
        ParserCollectionStrategy strategy = new ParserCollectionStrategy();
        for (Path projectPath : projectPaths) {
            result.addAll(strategy.collect(projectPath).getSourceRoots());
        }
        return result;
    }

}
