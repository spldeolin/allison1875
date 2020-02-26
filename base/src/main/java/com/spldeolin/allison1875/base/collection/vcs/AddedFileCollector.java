package com.spldeolin.allison1875.base.collection.vcs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.util.Times;
import lombok.extern.log4j.Log4j2;

/**
 * 每个文件的初次添加者的收集器
 *
 * @author Deolin 2020-02-26
 */
@Log4j2
class AddedFileCollector {

    Optional<Set<Path>> collectSinceTime(GitLoader loader, LocalDateTime since) throws GitAPIException, IOException {
        if (since == null) {
            return Optional.empty();
        }
        Date sinceDate = Times.toDate(since);

        List<RevCommit> commits = loader.commits();
        if (commits.size() < 2) {
            log.warn("commits.size()={}", commits.size());
            return Optional.empty();
        }

        // 最近、最早的commit
        RevCommit recentCommit = commits.get(0);
        RevCommit farthestCommit = commits.get(commits.size() - 1);

        if (recentCommit.getAuthorIdent().getWhen().before(sinceDate)) {
            // 最近的commit都早于指定时间，所有commit都早于指定时间，所有代码都是老代码
            log.warn("recent commit [{}] is before since date [{}].", recentCommit.getAuthorIdent().getWhen(), since);
            return Optional.of(Sets.newHashSet());
        }
        if (farthestCommit.getAuthorIdent().getWhen().after(sinceDate)) {
            // 最早的commit都晚于指定时间，所有commit都晚于指定时间，所有代码都是新代码
            log.warn("farthest commit [{}] is after since date [{}].", farthestCommit.getAuthorIdent().getWhen(),
                    since);
            return Optional.empty();
        }

        // 找到第一个早于指定时间的commit
        RevCommit recentCommitInRange = null;
        for (RevCommit commit : commits) {
            if (commit.getAuthorIdent().getWhen().before(sinceDate)) {
                recentCommitInRange = commit;
                break;
            }
        }

        return Optional.of(listAddTypePath(loader.git(), recentCommit, recentCommitInRange));
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
