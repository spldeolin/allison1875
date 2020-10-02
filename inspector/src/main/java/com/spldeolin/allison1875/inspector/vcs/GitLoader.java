package com.spldeolin.allison1875.inspector.vcs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import com.google.common.collect.Lists;

/**
 * @author Deolin 2020-02-26
 */
public class GitLoader {

    private Path projectPath;

    private Git git;

    private Repository repo;

    private List<RevCommit> commits;

    public GitLoader openAndLoad() throws IOException, GitAPIException {
        git = Git.open(projectPath.toFile());
        repo = git.getRepository();
        commits = Lists.newArrayList(git.log().call());
        return this;
    }

    public void close() {
        if (git != null) {
            git.close();
        }
    }

    public Path projectPath() {
        return this.projectPath;
    }

    public Git git() {
        return this.git;
    }

    public Repository repo() {
        return this.repo;
    }

    public List<RevCommit> commits() {
        return this.commits;
    }

    public GitLoader projectPath(Path projectPath) {
        this.projectPath = projectPath;
        return this;
    }

}
