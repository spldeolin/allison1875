package com.spldeolin.allison1875.si;


import static com.spldeolin.allison1875.si.StatuteInspectorConfig.CONFIG;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.collection.vcs.StaticGitAddedFileContainer;
import com.spldeolin.allison1875.base.util.Locations;
import lombok.extern.log4j.Log4j2;

/**
 * 报告出未commit的被修改的文件
 *
 * @author Deolin 2020-02-16
 */
@Log4j2
class UncommittedModifiedFileReporter {

    public static void main(String[] args) {
        new UncommittedModifiedFileReporter().process();
    }

    private void process() {
        try (Git git = Git.open(CONFIG.getProjectPath().toFile())) {
            String projectPath = git.getRepository().getWorkTree().getPath();
            Collection<CompilationUnit> modifiedCus = Lists.newLinkedList();
            for (DiffEntry diff : git.diff().call()) {
                if (DiffEntry.ChangeType.MODIFY == diff.getChangeType()) {
                    Path path = Paths.get(projectPath, diff.getNewPath());
                    CompilationUnit cu = StaticAstContainer.getCompilationUnit(path);
                    modifiedCus.add(cu);
                }
            }

            StaticGitAddedFileContainer.removeIfNotContain(modifiedCus).forEach(cu -> {
                StringBuilder author = new StringBuilder(64);
                cu.getPrimaryType().ifPresent(pt -> pt.getJavadoc().ifPresent(javadoc -> {
                    javadoc.getBlockTags().stream().filter(tag -> tag.getType() == Type.AUTHOR).forEach(
                            tag -> tag.getContent().getElements()
                                    .forEach(ele -> author.append(ele.toText()).append("|")));
                }));
                if (author.length() == 0) {
                    author.append("unknown author");
                } else {
                    author.deleteCharAt(author.length() - 1);
                }

                log.info("This code source is modified, path: [{}], author: [{}].", Locations.getRelativePath(cu),
                        author);
            });

        } catch (IOException | GitAPIException e) {
            log.error("UncommitModifiedFileReporter#process failed.", e);
            throw new RuntimeException();
        }
    }

}
