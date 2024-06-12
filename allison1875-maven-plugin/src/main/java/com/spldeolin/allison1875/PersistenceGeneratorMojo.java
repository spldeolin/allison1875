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
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.MavenProjectBuiltAstForest;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.interceptor.ValidInterceptor;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-10
 */
@Mojo(name = "persistence-generator", requiresDependencyResolution = ResolutionScope.RUNTIME)
@Slf4j
public class PersistenceGeneratorMojo extends AbstractMojo {

    @Parameter
    private String basePackage;

    @Parameter(defaultValue = "${project}")
    public MavenProject project;

    @Parameter(defaultValue = "com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule")
    private String persistenceGeneratorModuleQualifier;

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

            CommonConfig commonConfig = new CommonConfig();
            commonConfig.setReqDtoPackage(basePackage + ".javabean.req");
            commonConfig.setRespDtoPackage(basePackage + ".javabean.resp");
            commonConfig.setServicePackage(basePackage + ".service");
            commonConfig.setServiceImplPackage(basePackage + ".service.impl");
            commonConfig.setMapperPackage(basePackage + ".mapper");
            commonConfig.setEntityPackage(basePackage + ".entity");
            commonConfig.setDesignPackage(basePackage + ".design");
            commonConfig.setCondPackage(basePackage + ".javabean.cond");
            commonConfig.setRecordPackage(basePackage + ".javabean.record");
            commonConfig.setWholeDtoPackage(basePackage + ".javabean");
            commonConfig.setMapperXmlDirectoryPaths(Lists.newArrayList("src/main/resources/mapper"));
            commonConfig.setAuthor("Deolin");
            commonConfig.setIsJavabeanSerializable(false);
            commonConfig.setIsJavabeanCloneable(false);
            PersistenceGeneratorConfig config = new PersistenceGeneratorConfig();
            config.setCommonConfig(commonConfig);
            config.setJdbcUrl("jdbc:mysql://localhost:3306");
            config.setUserName("root");
            config.setPassword("root");
            config.setSchema("beginningmind");
            config.setTables(Lists.newArrayList());
            config.setEnableGenerateDesign(true);
            config.setIsEntityEndWithEntity(true);
            config.setDeletedSql(null);
            config.setNotDeletedSql(null);
            config.setEnableNoModifyAnnounce(true);
            config.setEnableLotNoAnnounce(false);
            config.setEntityExistenceResolution(FileExistenceResolutionEnum.OVERWRITE);

            log.info("persistenceGeneratorConfig={}", JsonUtils.toJsonPrettily(config));

            ClassLoader classLoader;
            PersistenceGeneratorModule persistenceGeneratorModule;
            try {
                classLoader = getClassLoader(project);
                persistenceGeneratorModule = (PersistenceGeneratorModule) classLoader.loadClass(
                                persistenceGeneratorModuleQualifier).getConstructor(PersistenceGeneratorConfig.class)
                        .newInstance(config);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            log.info(persistenceGeneratorModule.declareMainService().getName());

            log.info(project.getBasedir().toString());
            log.info(findRootProject(project).toString());

            // register interceptor
            List<Module> guiceModules = Lists.newArrayList(persistenceGeneratorModule,
                    new ValidInterceptor().toGuiceModule());

            // create ioc container
            Injector injector = Guice.createInjector(guiceModules);


            // valid
            List<InvalidDto> invalids = persistenceGeneratorModule.validConfigs();
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
            injector.getInstance(persistenceGeneratorModule.declareMainService()).process(astForest);
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
