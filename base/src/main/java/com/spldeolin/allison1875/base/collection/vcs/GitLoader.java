package com.spldeolin.allison1875.base.collection.vcs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-02-26
 */
@Accessors(fluent = true)
public class GitLoader {

    @Setter
    @Getter
    private Path projectPath;

    @Getter
    private Git git;

    @Getter
    private Repository repo;

    @Getter
    private List<RevCommit> commits;

    public GitLoader openAndLoad() throws IOException, GitAPIException {
        git = Git.open(projectPath.toFile());
        repo = git.getRepository();
        commits = Lists.newLinkedList(git.log().call());
        return this;
    }

    public void close() {
        if (git != null) {
            git.close();
        }
    }

}
