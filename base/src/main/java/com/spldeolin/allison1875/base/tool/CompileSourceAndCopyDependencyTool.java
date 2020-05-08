package com.spldeolin.allison1875.base.tool;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.extern.log4j.Log4j2;

/**
 * 这是一个独立运行的Tool
 * 这个Tool的作用是在对目标项目下的每个pom文件执行mvn compile 命令和mvn dependency:copy-dependencies
 * 从而获取到module的源码路径、编译后classpath路径、额外jar文件的路径
 * 最后生成yaml片段并打印以提供给base-config.yml使用
 *
 * @author Deolin 2020-04-19
 */
@Log4j2
public class CompileSourceAndCopyDependencyTool {

    /**
     * classpath相对于项目根目录的相对路径（默认均为target/classes）
     */
    private static final Path classpathRelativeToModulePath = Paths.get("target/classes");

    /**
     * sourceRoot相对于项目跟路径的相对路径（默认均为src/main/java）
     */
    private static final Path sourceRootRelativeToModulePath = Paths.get("src/main/java");

    private static final Path javaHome = Paths.get("/Library/Java/JavaVirtualMachines/jdk1.8.0_251.jdk/Contents/Home");

    private static final Path mavenHome = Paths.get("/usr/local/Cellar/maven/3.6.3_1");

    /**
     * Maven全局配置setting.xml的路径
     */
    private static final Path mavenGlobalSettingXml = Paths.get("");

    /**
     * mvn dependency:copy-dependencies 命令会将jar拷贝到这个目录下
     */
    private static final Path externalJarsBasePath = Paths.get("/Users/deolin/Documents/project-repo/external-jars");

    public static void main(String[] args) {
        ParserCollectionStrategy collectionStrategy = new ParserCollectionStrategy();
        StringBuilder report = new StringBuilder(1024);
        BaseConfig.getInstace().getProjectPaths().forEach(projectPath -> {
            List<File> poms = Lists.newArrayList();
            collectionStrategy.collect(projectPath).getSourceRoots().forEach(sourceRoot -> {
                String path = sourceRoot.getRoot().toString();
                if (path.endsWith(sourceRootRelativeToModulePath.toString())) {
                    path = path.replace(sourceRootRelativeToModulePath.toString(), "");
                    poms.add(Paths.get(path, "pom.xml").toFile()); // check exist
                }
            });

            List<File> excludeParent = poms;
            if (poms.size() > 1) {
                poms.sort(Comparator.comparingInt(o -> o.getParent().length()));
                excludeParent = poms.subList(1, poms.size());
            }

            for (File pom : excludeParent) {
                Path pomPath = pom.toPath();
                Path modulePath = pom.getParentFile().toPath();
                try {
                    log.info("CompileSourceAndCopyDependencyTool.invokePom({})", pomPath);
                    String externalJarsPath = invokePom(pomPath);
                    report.append("\r\n  - sourceCodePath: ");
                    report.append(modulePath.resolve(sourceRootRelativeToModulePath));
                    report.append("\r\n    classesPath: ");
                    report.append(modulePath.resolve(classpathRelativeToModulePath));
                    report.append("\r\n    externalJarsPath: ");
                    report.append(modulePath.resolve(externalJarsPath));
                } catch (MavenInvocationException e) {
                    log.error("CompileSourceAndCopyDependencyTool.invokePom({})", pomPath, e);
                }
            }
        });

        log.info(report);
    }

    private static String invokePom(Path pomPath) throws MavenInvocationException {
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(mavenHome.toFile());
        invoker.setOutputHandler(new PrintStreamHandler() {
            @Override
            public void consumeLine(String line) {
                line = nullToEmpty(line);
//                if (line.startsWith("[ERROR]")) {
                log.info(line);
//                }
            }
        });

        log.info("invoke compile for [{}]", BaseConfig.getInstace().getCommonPart().relativize(pomPath));
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomPath.toFile());
        request.setJavaHome(javaHome.toFile());
        request.setGlobalSettingsFile(mavenGlobalSettingXml.toFile());
        request.setGoals(Lists.newArrayList("clean compile"));
        invoker.execute(request);

        log.info("invoke copy dependencies for [{}]", BaseConfig.getInstace().getCommonPart().relativize(pomPath));
        String externalJarPath = externalJarsBasePath + pomPath.getParent().toString();
        request.setGoals(Lists.newArrayList("dependency:copy-dependencies"));
        Properties properties = new Properties();
        properties.setProperty("outputAbsoluteArtifactFilename", "true");
        properties.setProperty("includeScope", "runtime");
        properties.setProperty("outputDirectory", externalJarPath);
        request.setProperties(properties);
        invoker.execute(request);

        log.info("invoke download sources for [{}]", BaseConfig.getInstace().getCommonPart().relativize(pomPath));
        request.setGoals(Lists.newArrayList("dependency:sources"));
        request.setProperties(properties);
        invoker.execute(request);

        return externalJarPath;
    }

    private static String nullToEmpty(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

}
