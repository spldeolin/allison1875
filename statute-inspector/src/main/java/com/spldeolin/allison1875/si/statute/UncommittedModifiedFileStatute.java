package com.spldeolin.allison1875.si.statute;


import static com.spldeolin.allison1875.si.StatuteInspectorConfig.CONFIG;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.collection.vcs.StaticGitAddedFileContainer;
import com.spldeolin.allison1875.si.dto.LawlessDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-16
 */
@Log4j2
public class UncommittedModifiedFileStatute implements Statute {

    @Override
    public Collection<LawlessDto> inspect(Collection<CompilationUnit> neverUsed) {
        Collection<LawlessDto> result = Lists.newLinkedList();

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
                LawlessDto vo = new LawlessDto(cu);
                result.add(vo);
            });

        } catch (IOException | GitAPIException e) {
            log.error("UncommitModifiedFileReporter#process failed.", e);
        }
        return result;
    }

}
