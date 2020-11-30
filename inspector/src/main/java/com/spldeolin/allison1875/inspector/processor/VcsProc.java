package com.spldeolin.allison1875.inspector.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.util.TimeUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-11-30
 */
@Log4j2
class VcsProc {

    private final Path projectPath;

    @Getter
    private Boolean isAllTarget;

    @Getter
    private Set<Path> addedFiles;

    VcsProc(Path projectPath) {
        this.projectPath = projectPath;
    }

    VcsProc process() {
        try {
            Git git = Git.open(projectPath.toFile());
            List<RevCommit> commits = Lists.newArrayList(git.log().call());

            Date sinceDate = TimeUtils.toDate(Inspector.CONFIG.get().getTargetFileSince());

            if (commits.size() < 2) {
                log.warn("commits.size()={}", commits.size());
                isAllTarget = true;
                return this;
            }

            // 最早的commit
            RevCommit farthestCommit = commits.get(commits.size() - 1);
            if (farthestCommit.getAuthorIdent().getWhen().after(sinceDate)) {
                // 最早的commit都晚于指定时间，所有commit都晚于指定时间，所有代码都是新代码
                log.warn("farthest commit [{}] is after since date [{}].", farthestCommit.getAuthorIdent().getWhen(),
                        sinceDate);
                isAllTarget = true;
                return this;
            }

            // 最近的commit
            RevCommit recentCommit = commits.get(0);
            if (recentCommit.getAuthorIdent().getWhen().before(sinceDate)) {
                // 最近的commit都早于指定时间，所有commit都早于指定时间，所有代码都是老代码
                log.warn("recent commit [{}] is before since date [{}].", recentCommit.getAuthorIdent().getWhen(),
                        sinceDate);
                isAllTarget = false;
                addedFiles = Sets.newHashSet();
                return this;
            }

            // 找到第一个早于指定时间的commit
            RevCommit recentCommitInRange = null;
            for (RevCommit commit : commits) {
                if (commit.getAuthorIdent().getWhen().before(sinceDate)) {
                    recentCommitInRange = commit;
                    break;
                }
            }
            isAllTarget = false;
            addedFiles = listAddTypePath(git, recentCommit, recentCommitInRange);

        } catch (Exception e) {
            log.error(e);
        }
        return this;
    }


    private Set<Path> listAddTypePath(Git git, RevCommit newCommit, RevCommit oldCommit)
            throws GitAPIException, IOException {
        String projectPath = git.getRepository().getWorkTree().getPath();
        AbstractTreeIterator oldTree = getAbstractTreeIterator(oldCommit, git.getRepository());
        AbstractTreeIterator newTree = getAbstractTreeIterator(newCommit, git.getRepository());
        List<DiffEntry> diffs = git.diff().setOldTree(oldTree).setNewTree(newTree).call();
        Set<Path> result = Sets.newHashSet();
        for (DiffEntry diff : diffs) {
            if (DiffEntry.ChangeType.ADD == diff.getChangeType()) {
                result.add(Paths.get(projectPath, diff.getNewPath()));
            }
        }
        return result;
    }

    private AbstractTreeIterator getAbstractTreeIterator(RevCommit commit, Repository repository) throws IOException {
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        RevWalk revWalk = new RevWalk(repository);
        try {
            RevTree revTree = revWalk.parseTree(commit.getTree().getId());
            treeParser.reset(repository.newObjectReader(), revTree.getId());
        } finally {
            revWalk.dispose();
        }
        return treeParser;
    }

}