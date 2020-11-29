package com.spldeolin.allison1875.inspector.vcs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import com.github.javaparser.ast.Node;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.inspector.processor.Inspector;

/**
 * @author Deolin 2020-02-26
 */
public class VcsContainer {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(VcsContainer.class);

    private final Path projectPath;

    private Optional<Set<Path>> addedFiles;

    private Map<Path, String> firstCommitAuthorsByFile;

    VcsContainer(Path projectPath) {
        this.projectPath = projectPath;
        try {
            GitLoader loader = new GitLoader().projectPath(projectPath).openAndLoad();
            addedFiles = new AddedFileCollector().collectSinceTime(loader, Inspector.CONFIG.get().getTargetFileSince());
            loader.close();
        } catch (IOException | GitAPIException e) {
            log.error(e);
            addedFiles = Optional.empty();
            firstCommitAuthorsByFile = Maps.newHashMap();
        }
    }

    public boolean contain(Node node) {
        Path nodeCuPath = Locations.getAbsolutePath(node);
        return addedFiles.map(pathCollection -> pathCollection.contains(nodeCuPath)).orElse(true);
    }

    public <T extends Node> Collection<T> removeIfNotContain(Collection<T> nodes) {
        Collection<T> result = Lists.newArrayList(nodes);
        result.removeIf(node -> !contain(node));
        return result;
    }

    public String toString() {
        return "VcsContainer(projectPath=" + this.projectPath + ", addedFiles=" + this.addedFiles
                + ", firstCommitAuthorsByFile=" + this.firstCommitAuthorsByFile + ")";
    }

    public Path getProjectPath() {
        return this.projectPath;
    }

    public Map<Path, String> getFirstCommitAuthorsByFile() {
        return this.firstCommitAuthorsByFile;
    }

}
