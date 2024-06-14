package com.spldeolin.allison1875;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.MavenProjectBuiltAstForest;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.interceptor.ValidInterceptor;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-13
 */
@Slf4j
public abstract class Allison1875Mojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${project.groupId}", required = true)
    protected String basePackage;

    @Parameter
    private CommonConfig commonConfig;

    @Override
    public void execute() throws MojoExecutionException {
        Allison1875.allison1875();
        initParam();

        ClassLoader classLoader;
        Allison1875Module allison1875Module;
        try {
            classLoader = getClassLoader(project);
            allison1875Module = newAllison1875Module(commonConfig, classLoader);
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }

        // valid config
        List<InvalidDto> invalids = allison1875Module.validConfigs();
        if (CollectionUtils.isNotEmpty(invalids)) {
            throw new MojoExecutionException(
                    "Allison 1875 fail to work cause invalid config\ninvalids=" + JsonUtils.toJsonPrettily(invalids));
        }

        // register interceptor
        List<Module> guiceModules = Lists.newArrayList(allison1875Module, new ValidInterceptor().toGuiceModule());

        // create ioc container
        Injector injector = Guice.createInjector(guiceModules);

        List<File> sourceRoots = project.getCompileSourceRoots().stream().map(File::new).collect(Collectors.toList());
        log.info("sourceRoots={}", sourceRoots);
        List<File> resourceRoots = project.getResources().stream().map(r -> new File(r.getDirectory()))
                .collect(Collectors.toList());
        log.info("resourceRoots={}", resourceRoots);

        for (File sourceRoot : sourceRoots) {
            AstForest astForest = new MavenProjectBuiltAstForest(classLoader, sourceRoot, resourceRoots);
            injector.getInstance(allison1875Module.declareMainService()).process(astForest);
        }
    }

    private void initParam() {
        log.info("project={}", project);
        log.info("basePackage={}", basePackage);
        commonConfig = MoreObjects.firstNonNull(commonConfig, new CommonConfig());
        commonConfig.setReqDtoPackage(
                MoreObjects.firstNonNull(commonConfig.getReqDtoPackage(), basePackage + ".javabean.req"));
        commonConfig.setRespDtoPackage(
                MoreObjects.firstNonNull(commonConfig.getRespDtoPackage(), basePackage + ".javabean.resp"));
        commonConfig.setServicePackage(
                MoreObjects.firstNonNull(commonConfig.getServicePackage(), basePackage + ".service"));
        commonConfig.setServiceImplPackage(
                MoreObjects.firstNonNull(commonConfig.getServiceImplPackage(), basePackage + ".service.impl"));
        commonConfig.setMapperPackage(
                MoreObjects.firstNonNull(commonConfig.getMapperPackage(), basePackage + ".mapper"));
        commonConfig.setEntityPackage(
                MoreObjects.firstNonNull(commonConfig.getEntityPackage(), basePackage + ".entity"));
        commonConfig.setDesignPackage(
                MoreObjects.firstNonNull(commonConfig.getDesignPackage(), basePackage + ".design"));
        commonConfig.setCondPackage(
                MoreObjects.firstNonNull(commonConfig.getCondPackage(), basePackage + ".javabean.cond"));
        commonConfig.setRecordPackage(
                MoreObjects.firstNonNull(commonConfig.getRecordPackage(), basePackage + ".javabean.record"));
        commonConfig.setWholeDtoPackage(
                MoreObjects.firstNonNull(commonConfig.getWholeDtoPackage(), basePackage + ".javabean"));
        commonConfig.setMapperXmlDirectories(MoreObjects.firstNonNull(commonConfig.getMapperXmlDirectories(),
                Lists.newArrayList("../resources/mapper")));
        commonConfig.setAuthor(MoreObjects.firstNonNull(commonConfig.getAuthor(), "Allison 1875"));
        commonConfig.setIsJavabeanSerializable(
                MoreObjects.firstNonNull(commonConfig.getIsJavabeanSerializable(), false));
        commonConfig.setIsJavabeanCloneable(MoreObjects.firstNonNull(commonConfig.getIsJavabeanCloneable(), false));
        commonConfig.setEnableNoModifyAnnounce(
                MoreObjects.firstNonNull(commonConfig.getEnableNoModifyAnnounce(), true));
        commonConfig.setEnableLotNoAnnounce(MoreObjects.firstNonNull(commonConfig.getEnableLotNoAnnounce(), true));
        log.info("commonConfig={}", JsonUtils.toJsonPrettily(commonConfig));
    }

    public abstract Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader)
            throws Exception;

    private ClassLoader getClassLoader(MavenProject project) throws Exception {
        List<String> classpathElements = project.getCompileClasspathElements();
        log.info("classpathElements={}", JsonUtils.toJson(classpathElements));
        classpathElements.add(project.getBuild().getOutputDirectory());
        classpathElements.add(project.getBuild().getTestOutputDirectory());
        URL[] urls = new URL[classpathElements.size()];
        for (int i = 0; i < classpathElements.size(); ++i) {
            urls[i] = new File(classpathElements.get(i)).toURL();
        }
        return new URLClassLoader(urls, this.getClass().getClassLoader());
    }

}
