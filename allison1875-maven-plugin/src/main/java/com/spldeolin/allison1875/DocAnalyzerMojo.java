package com.spldeolin.allison1875;

import java.io.File;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
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

    @Parameter(defaultValue = "com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule")
    private String docAnalyzerModuleQualifier;

    @Parameter
    private DocAnalyzerConfig docAnalyzerConfig;

    @Override
    public Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader) throws Exception {
        docAnalyzerConfig = MoreObjects.firstNonNull(docAnalyzerConfig, new DocAnalyzerConfig());
        docAnalyzerConfig.setRootProjectDirectory(MoreObjects.firstNonNull(docAnalyzerConfig.getRootProjectDirectory(),
                findRootProject(project).getPath()));
        docAnalyzerConfig.setDependencyProjectDirectories(
                MoreObjects.firstNonNull(docAnalyzerConfig.getDependencyProjectDirectories(), Lists.newArrayList()));
        docAnalyzerConfig.setGlobalUrlPrefix(MoreObjects.firstNonNull(docAnalyzerConfig.getGlobalUrlPrefix(), ""));
        docAnalyzerConfig.setFlushTo(
                MoreObjects.firstNonNull(docAnalyzerConfig.getFlushTo(), FlushToEnum.LOCAL_MARKDOWN));
        docAnalyzerConfig.setMarkdownDirectory(MoreObjects.firstNonNull(docAnalyzerConfig.getMarkdownDirectory(),
                project.getBasedir().toPath().resolve("api-docs").toString()));
        docAnalyzerConfig.setEnableCurl(MoreObjects.firstNonNull(docAnalyzerConfig.getEnableCurl(), false));
        docAnalyzerConfig.setEnableResponseBodySample(
                MoreObjects.firstNonNull(docAnalyzerConfig.getEnableResponseBodySample(), false));
        log.info("docAnalyzerConfig={}", JsonUtils.toJsonPrettily(docAnalyzerConfig));

        log.info("new instance for {}", docAnalyzerModuleQualifier);
        return (Allison1875Module) classLoader.loadClass(docAnalyzerModuleQualifier)
                .getConstructor(CommonConfig.class, DocAnalyzerConfig.class)
                .newInstance(commonConfig, docAnalyzerConfig);
    }

    private File findRootProject(MavenProject project) {
        if (project.getParent() == null || project.getParent().getBasedir() == null) {
            return project.getBasedir();
        }
        return findRootProject(project.getParent());
    }

}