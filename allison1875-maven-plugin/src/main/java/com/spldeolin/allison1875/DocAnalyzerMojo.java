package com.spldeolin.allison1875;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.enums.FlushToEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-10
 */
@Mojo(name = "doc-analyzer", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
@Slf4j
public class DocAnalyzerMojo extends Allison1875Mojo {

    @Parameter(alias = "docAnalyzer")
    private DocAnalyzerMojoConfig docAnalyzerConfig;

    @Override
    public Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader) throws Exception {
        docAnalyzerConfig = MoreObjects.firstNonNull(docAnalyzerConfig, new DocAnalyzerMojoConfig());
        List<File> dependencyProjectDirectories = MoreObjects.firstNonNull(docAnalyzerConfig.getDependencyProjectDirs(),
                Lists.newArrayList());
        docAnalyzerConfig.setDependencyProjectDirs(dependencyProjectDirectories.stream()
                .map(f -> project.getBasedir().toPath().resolve(f.toPath()).toFile()).collect(Collectors.toList()));
        docAnalyzerConfig.setGlobalUrlPrefix(MoreObjects.firstNonNull(docAnalyzerConfig.getGlobalUrlPrefix(), ""));
        docAnalyzerConfig.setFlushTo(
                MoreObjects.firstNonNull(docAnalyzerConfig.getFlushTo(), FlushToEnum.LOCAL_MARKDOWN));
        docAnalyzerConfig.setMarkdownDir(project.getBasedir().toPath()
                .resolve(MoreObjects.firstNonNull(docAnalyzerConfig.getMarkdownDir(), new File("api-docs")).toPath())
                .toFile());
        docAnalyzerConfig.setEnableCurl(MoreObjects.firstNonNull(docAnalyzerConfig.getEnableCurl(), false));
        docAnalyzerConfig.setEnableResponseBodySample(
                MoreObjects.firstNonNull(docAnalyzerConfig.getEnableResponseBodySample(), false));
        log.info("docAnalyzer={}", JsonUtils.toJsonPrettily(docAnalyzerConfig));

        log.info("new instance for {}", docAnalyzerConfig.getModule());
        return (Allison1875Module) classLoader.loadClass(docAnalyzerConfig.getModule())
                .getConstructor(CommonConfig.class, DocAnalyzerConfig.class)
                .newInstance(commonConfig, docAnalyzerConfig);
    }

}
