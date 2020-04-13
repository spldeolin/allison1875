package com.spldeolin.allison1875.da.core.processor;

import java.io.IOException;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import com.spldeolin.allison1875.base.BaseConfig;
import com.spldeolin.allison1875.base.collection.vcs.GitLoader;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-04-13
 */
@Log4j2
public class GitDiscardProcessor {

    public void process() {
        GitLoader loader = new GitLoader().projectPath(BaseConfig.getInstace().getProjectPath());
        try {
            loader.openAndLoad().git().reset().setMode(ResetType.HARD).call();
        } catch (IOException | GitAPIException e) {
            log.error("Cannot discard modifications", e);
        } finally {
            loader.close();
        }
    }

}
