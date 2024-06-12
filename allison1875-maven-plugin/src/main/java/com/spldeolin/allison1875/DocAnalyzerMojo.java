package com.spldeolin.allison1875;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.MavenProjectBuiltAstForest;
import com.spldeolin.allison1875.common.interceptor.ValidInterceptor;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule;
import com.spldeolin.allison1875.docanalyzer.enums.FlushToEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-10
 */
@Mojo(name = "doc-analyzer", requiresDependencyResolution = ResolutionScope.TEST)
@Slf4j
public class DocAnalyzerMojo extends AbstractMojo {

    @Parameter
    private String basePackage;

    @Parameter(defaultValue = "${project}")
    public MavenProject project;

    @Parameter
    private String globalUrlPrefix;

    @Parameter
    private String flushTo;

    @Parameter
    private String yapiUrl;

    @Parameter
    private String yapiToken;

    @Parameter
    private String markdownDirectoryPath;

    @Parameter(defaultValue = "false")
    private Boolean enableCurl;

    @Parameter(defaultValue = "false")
    private Boolean enableResponseBodySample;

    @Parameter(defaultValue = "true")
    private Boolean enableNoModifyAnnounce;

    @Parameter(defaultValue = "true")
    private Boolean enableLotNoAnnounce;

    @Parameter(defaultValue = "com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule")
    private String docAnalyzerModuleQualifier;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String basePackage = this.basePackage;
        if (basePackage == null) {
            basePackage = project.getGroupId();
        }
        log.info("basePackage={}", basePackage);
        log.info("basedir={} pomFile={}", project.getBasedir(), project.getFile());
        List<File> sourceRoots = project.getCompileSourceRoots().stream().map(File::new).collect(Collectors.toList());
        log.info("sourceRoots={}", sourceRoots);
        List<File> resourceRoots = project.getResources().stream().map(r -> new File(r.getDirectory()))
                .collect(Collectors.toList());
        log.info("resourceRoots={}", resourceRoots);

        for (File sourceRoot : sourceRoots) {
            log.info("sourceRoot={}", sourceRoot);

            DocAnalyzerConfig config = new DocAnalyzerConfig();
            config.setDependencyProjectDirectories(Lists.newArrayList());
            config.setGlobalUrlPrefix(MoreObjects.firstNonNull(globalUrlPrefix, ""));
            FlushToEnum flushTo = FlushToEnum.valueOf(this.flushTo);
            config.setFlushTo(flushTo);
            config.setYapiUrl(yapiUrl);
            config.setYapiToken(yapiToken);
            config.setMarkdownDirectoryPath(markdownDirectoryPath);
            config.setEnableCurl(enableCurl);
            config.setEnableResponseBodySample(enableResponseBodySample);
            config.setEnableNoModifyAnnounce(enableNoModifyAnnounce);
            config.setEnableLotNoAnnounce(enableLotNoAnnounce);
            log.info("docAnalyzerConfig={}", JsonUtils.toJsonPrettily(config));

            ClassLoader classLoader;
            DocAnalyzerModule docAnalyzerModule;
            try {
                classLoader = getClassLoader(project);
                docAnalyzerModule = (DocAnalyzerModule) classLoader.loadClass(docAnalyzerModuleQualifier)
                        .getConstructor(DocAnalyzerConfig.class).newInstance(config);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            log.info(docAnalyzerModule.declareMainService().getName());

            log.info(project.getBasedir().toString());
            log.info(findRootProject(project).toString());

            // register interceptor
            List<Module> guiceModules = Lists.newArrayList(docAnalyzerModule, new ValidInterceptor().toGuiceModule());

            // create ioc container
            Injector injector = Guice.createInjector(guiceModules);


            // valid
            List<InvalidDto> invalids = docAnalyzerModule.validConfigs();
            if (CollectionUtils.isNotEmpty(invalids)) {
                for (InvalidDto invalid : invalids) {
                    log.error(String.format(
                            "Allison 1875 fail to work cause invalid config, path=%s, reason=%s, value=%s",
                            invalid.getPath(), invalid.getReason(), invalid.getValue()));
                }
                System.exit(-9);
            }

            log.info("ass2");

            AstForest astForest = new MavenProjectBuiltAstForest(classLoader, sourceRoot, resourceRoots);
            injector.getInstance(docAnalyzerModule.declareMainService()).process(astForest);
        }
    }

    private ClassLoader getClassLoader(MavenProject project) {
        try {
            List<String> classpathElements = project.getCompileClasspathElements();
            log.info("classpathElements={}", classpathElements);
            classpathElements.add(project.getBuild().getOutputDirectory());
            classpathElements.add(project.getBuild().getTestOutputDirectory());
            URL[] urls = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); ++i) {
                urls[i] = new File(classpathElements.get(i)).toURL();
            }
            return new URLClassLoader(urls, this.getClass().getClassLoader());
        } catch (Exception e) {
            log.debug("Couldn't get the classloader.");
            return this.getClass().getClassLoader();
        }
    }

    private File findRootProject(MavenProject project) {
        if (project.getParent() == null || project.getParent().getBasedir() == null) {
            return project.getBasedir();
        }
        return findRootProject(project.getParent());
    }

}
