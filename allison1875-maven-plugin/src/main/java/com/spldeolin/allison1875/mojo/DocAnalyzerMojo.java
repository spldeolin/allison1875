package com.spldeolin.allison1875.mojo;

import java.util.stream.Collectors;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-10
 */
@Mojo(name = "doc-analyzer", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
@Slf4j
public class DocAnalyzerMojo extends Allison1875Mojo {

    @Override
    public Allison1875Module newAllison1875Module(MojoConfig config, ClassLoader classLoader) throws Exception {
        // 对config对象中的文件路径进行相对basedir的处理
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
        log.info("docAnalyzerModule={}", config.getDocAnalyzerModule());
        return (Allison1875Module) classLoader.loadClass(config.getDocAnalyzerModule()).getConstructor(Config.class)
                .newInstance(config);
    }

}
