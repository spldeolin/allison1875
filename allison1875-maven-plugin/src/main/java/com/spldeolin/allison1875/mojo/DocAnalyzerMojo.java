package com.spldeolin.allison1875.mojo;

import java.util.stream.Collectors;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.enums.ToolEnum;

/**
 * @author Deolin 2024-06-10
 */
@Mojo(name = "doc-analyzer", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
public class DocAnalyzerMojo extends Allison1875Mojo {

    @Override
    protected ToolEnum getTool() {
        return ToolEnum.DOC_ANALYZER;
    }

    @Override
    protected void resolveBasedirPaths(Config config) {
        if (config.getDependencyDirsOrJavaFilePath() != null) {
            config.setDependencyDirsOrJavaFilePath(
                    config.getDependencyDirsOrJavaFilePath().stream().map(super::getCanonicalFileRelativeToBasedir)
                            .collect(Collectors.toList()));
        }
        if (config.getMarkdownDir() != null) {
            config.setMarkdownDir(getCanonicalFileRelativeToBasedir(config.getMarkdownDir()));
        }
        if (config.getDslDir() != null) {
            config.setDslDir(getCanonicalFileRelativeToBasedir(config.getDslDir()));
        }
    }

}
