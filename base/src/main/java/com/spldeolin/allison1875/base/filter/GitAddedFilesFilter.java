package com.spldeolin.allison1875.base.filter;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import com.github.javaparser.ast.Node;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.Config;
import com.spldeolin.allison1875.base.util.Locations;
import lombok.extern.log4j.Log4j2;

/**
 * Git新增文件过滤器
 *
 * 通过指定时间点或是commit id，区分新代码和老代码，判断AST Node是否位于新代码中
 *
 * @author Deolin 2020-02-11
 */
@Log4j2
public class GitAddedFilesFilter {

    private static final Optional<Set<Path>> GIT_ADDED_FILES = new GitAddedFilesCollector()
            .collectSinceAssignedTime(Config.getProjectPath(), Config.getGiveUpResultAddedSinceTime());


    public boolean filter(Node node) {
        Path nodeCuPath = Locations.getAbsolutePath(node);
        return GIT_ADDED_FILES.map(pathCollection -> pathCollection.contains(nodeCuPath)).orElse(true);
    }

    public <T extends Node> Collection<T> filter(Collection<T> nodes) {
        Collection<T> result = Lists.newLinkedList(nodes);
        result.removeIf(node -> !filter(node));
        if (result.size() == 0) {
            log.info("All nodes were not through GitAddedFilesFilter.");
        }
        return result;
    }

}
