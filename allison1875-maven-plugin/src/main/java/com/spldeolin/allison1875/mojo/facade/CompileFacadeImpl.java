package com.spldeolin.allison1875.mojo.facade;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.formgenerator.CompileFacade;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yanshaowei01 2026-02-28
 */
@Slf4j
public class CompileFacadeImpl implements CompileFacade {

    private final MavenProject project;

    private final MavenSession mavenSession;

    private final BuildPluginManager pluginManager;

    public CompileFacadeImpl(MavenProject project, MavenSession mavenSession, BuildPluginManager pluginManager) {
        this.project = project;
        this.mavenSession = mavenSession;
        this.pluginManager = pluginManager;
    }

    @Override
    public void compile(AstForest astForest, String javaVersion) {
        try {
            log.info("开始编译：{}，使用编译版本: {}", project, javaVersion);
            executeMojo(
                    plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"), version("3.11.0")),
                    goal("compile"),
                    configuration(element(name("source"), javaVersion), element(name("target"), javaVersion),
                            element(name("encoding"), "UTF-8"),
                            element(name("compilerArgs"), element(name("arg"), "-parameters"),
                                    element(name("arg"), "-Xlint:unchecked"))),
                    executionEnvironment(project, mavenSession, pluginManager));
        } catch (MojoExecutionException e) {
            throw new Allison1875Exception(e);
        }
    }

}
