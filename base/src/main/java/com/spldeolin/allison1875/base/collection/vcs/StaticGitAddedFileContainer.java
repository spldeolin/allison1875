package com.spldeolin.allison1875.base.collection.vcs;

import static com.spldeolin.allison1875.base.BaseConfig.CONFIG;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import com.github.javaparser.ast.Node;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.Locations;

/**
 * Git新增文件集合的静态访问口
 *
 * @author Deolin 2020-02-12
 */
public class StaticGitAddedFileContainer {

    private static final Optional<Set<Path>> GIT_ADDED_FILES = new GitAddedFileCollector()
            .collectSinceAssignedTime(CONFIG.getProjectPath(), CONFIG.getGiveUpResultAddedSinceTime());

    public static boolean contain(Node node) {
        Path nodeCuPath = Locations.getAbsolutePath(node);
        return GIT_ADDED_FILES.map(pathCollection -> pathCollection.contains(nodeCuPath)).orElse(true);
    }

    public static <T extends Node> Collection<T> removeIfNotContain(Collection<T> nodes) {
        Collection<T> result = Lists.newLinkedList(nodes);
        result.removeIf(node -> !contain(node));
        return result;
    }

}
