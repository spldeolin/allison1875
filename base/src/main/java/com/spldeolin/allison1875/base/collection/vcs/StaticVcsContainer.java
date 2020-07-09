package com.spldeolin.allison1875.base.collection.vcs;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import com.github.javaparser.ast.Node;
import com.google.common.collect.Iterables;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-26
 */
@Log4j2
@Data
public class StaticVcsContainer {

    private static VcsContainer fromConfigPath = new VcsContainer(
            Paths.get(Iterables.getFirst(BaseConfig.getInstace().getProjectPaths(), null)));

    public static Path getProjectPath() {
        return fromConfigPath.getProjectPath();
    }

    public static boolean contain(Node node) {
        return fromConfigPath.contain(node);
    }

    public static <T extends Node> Collection<T> removeIfNotContain(Collection<T> nodes) {
        return fromConfigPath.removeIfNotContain(nodes);
    }

}
