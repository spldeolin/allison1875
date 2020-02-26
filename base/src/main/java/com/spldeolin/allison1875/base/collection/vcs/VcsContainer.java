package com.spldeolin.allison1875.base.collection.vcs;

import static com.spldeolin.allison1875.base.BaseConfig.CONFIG;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jgit.api.errors.GitAPIException;
import com.github.javaparser.ast.Node;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.ast.Locations;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-26
 */
@Log4j2
@ToString
public class VcsContainer {

    @Getter
    private Path projectPath;

    private Optional<Set<Path>> addedFiles;

    @Getter
    private Map<Path, String> firstCommitAuthorsByFile;

    VcsContainer(Path projectPath) {
        this.projectPath = projectPath;
        try {
            GitLoader loader = new GitLoader().projectPath(projectPath).openAndLoad();
            addedFiles = new AddedFileCollector().collectSinceTime(loader, CONFIG.getGiveUpResultAddedSinceTime());
//            firstCommitAuthorsByFile = new FirstCommitAuthorCollector().loader(loader).collect();
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
        Collection<T> result = Lists.newLinkedList(nodes);
        result.removeIf(node -> !contain(node));
        return result;
    }

//    public String getFirstCommitAuthors(Path path) {
//        return firstCommitAuthorsByFile.get(path);
//    }

}
