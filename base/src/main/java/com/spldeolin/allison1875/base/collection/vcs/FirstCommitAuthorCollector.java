package com.spldeolin.allison1875.base.collection.vcs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.util.ast.Locations;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-26
 */
@Log4j2
@Accessors(fluent = true)
class FirstCommitAuthorCollector {

    @Setter
    private GitLoader loader;

    Map<Path, String> collect() throws GitAPIException, IOException {
        Map<Path, String> result = Maps.newHashMap();
        for (CompilationUnit cu : StaticAstContainer.getCompilationUnits()) {
            Path cuPath = Locations.getRelativePath(cu);
            List<RevCommit> commitHistories = findCommitHistoryForEachFile(cuPath.toString());
            if (commitHistories.size() > 0) {
                result.put(cuPath, Iterables.getLast(commitHistories).getAuthorIdent().getName());
            } else {
                log.warn(cuPath);
            }
        }
        return result;
    }

    private List<RevCommit> findCommitHistoryForEachFile(String fileRelativePath) throws GitAPIException, IOException {
        List<RevCommit> commits = Lists.newLinkedList();
//        RevCommit start = null;
//        do {
        Iterable<RevCommit> log = loader.git().log().addPath(fileRelativePath).call();
        for (RevCommit commit : log) {
            if (commits.contains(commit)) {
//                    start = null;
            } else {
//                    start = commit;
                commits.add(commit);
            }
        }
//            if (start == null) {
//                return commits;
//            }
//        } while ((fileRelativePath = getRenamedPath(start, fileRelativePath)) != null);
        return commits;
    }


    private String getRenamedPath(RevCommit start, String fileRelativePath) throws IOException, GitAPIException {
        Iterable<RevCommit> allCommitsLater = loader.git().log().add(start).call();
        for (RevCommit commit : allCommitsLater) {

            TreeWalk tw = new TreeWalk(loader.repo());
            tw.addTree(commit.getTree());
            tw.addTree(start.getTree());
            tw.setRecursive(true);
            RenameDetector rd = new RenameDetector(loader.repo());
            rd.addAll(DiffEntry.scan(tw));
            List<DiffEntry> files = rd.compute();
            for (DiffEntry diffEntry : files) {
                if ((diffEntry.getChangeType() == DiffEntry.ChangeType.RENAME
                        || diffEntry.getChangeType() == DiffEntry.ChangeType.COPY) && diffEntry.getNewPath()
                        .contains(fileRelativePath)) {
                    System.out.println("Found: " + diffEntry.toString() + " return " + diffEntry.getOldPath());
                    return diffEntry.getOldPath();
                }
            }
        }
        return null;
    }

}
