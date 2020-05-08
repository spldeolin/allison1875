package com.spldeolin.allison1875.base.collection.ast;

import java.nio.file.Path;
import java.util.Collection;
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

    Collection<SourceRoot> collect(Collection<Path> projectPaths, Collection<Path> sourceRootPaths) {
        Collection<SourceRoot> result = Lists.newLinkedList();
        for (Path sourceRootPath : sourceRootPaths) {
            for (Path projectPath : projectPaths) {
                if (isChild(projectPath, sourceRootPath)) {
                    result.add(new SourceRoot(sourceRootPath));
                }
            }
        }
        result.forEach(sr -> log
                .info("SourceRoot collected. [{}]", BaseConfig.getInstace().getCommonPart().relativize(sr.getRoot())));
        return result;
    }

    private boolean isChild(Path parent, Path childMight) {
        parent = parent.toAbsolutePath();
        return childMight.startsWith(parent);
    }

}
