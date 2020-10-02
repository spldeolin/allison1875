package com.spldeolin.allison1875.inspector.vcs;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import com.github.javaparser.ast.Node;
import com.spldeolin.allison1875.inspector.InspectorConfig;

/**
 * @author Deolin 2020-02-26
 */
public class StaticVcsContainer {

    private static final VcsContainer fromConfigPath = new VcsContainer(
            Paths.get(InspectorConfig.getInstance().getProjectLocalGitPath()));

    public StaticVcsContainer() {
    }

    public static Path getProjectPath() {
        return fromConfigPath.getProjectPath();
    }

    public static boolean contain(Node node) {
        return fromConfigPath.contain(node);
    }

    public static <T extends Node> Collection<T> removeIfNotContain(Collection<T> nodes) {
        return fromConfigPath.removeIfNotContain(nodes);
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StaticVcsContainer)) {
            return false;
        }
        final StaticVcsContainer other = (StaticVcsContainer) o;
        return other.canEqual(this);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof StaticVcsContainer;
    }

    public int hashCode() {
        int result = 1;
        return result;
    }

    public String toString() {
        return "StaticVcsContainer()";
    }

}
