package com.spldeolin.allison1875.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.util.FileSnapshotUtils;
import com.spldeolin.allison1875.common.util.FileSnapshotUtils.FileSystemSnapshot;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.mojo.ast.MavenProjectBuiltAstForest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-13
 */
@Slf4j
public abstract class Allison1875Mojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * configYml文件的路径（相对于项目basedir），默认为 .allison1875.yml
     */
    @Parameter(defaultValue = ".allison1875.yml")
    private String configYmlPath;

    @Override
    public void execute() throws MojoExecutionException {
        // 为整个maven project拍摄快照
        FileSystemSnapshot fileSnapshot = FileSnapshotUtils.createSnapshot(project.getBasedir());

        try {
            // banner
            Allison1875.hello();

            // 构造guice module
            MojoConfig config = initParam();
            ClassLoader classLoader = getClassLoader(project);
            Allison1875Module allison1875Module = newAllison1875Module(config, classLoader);

            // 构造AstForest，执行Allison1875
            List<File> sourceRoots = project.getCompileSourceRoots().stream().map(File::new)
                    .collect(Collectors.toList());
            log.info("sourceRoots={}", sourceRoots);
            for (File sourceRoot : sourceRoots) {
                if (sourceRoot.toString().endsWith("generated-sources/annotations")) {
                    continue;
                }
                AstForest astForest = new MavenProjectBuiltAstForest(classLoader, sourceRoot);
                Allison1875.letsGo(allison1875Module, astForest);
            }

            // 成功时清理快照
            fileSnapshot.cleanup();
        } catch (Throwable e) {

            // 任何异常回滚整个maven project（mvnDebug模式下不回滚，以便调试问题）
            if (!isMavenDebugMode()) {
                FileSnapshotUtils.rollback(fileSnapshot);
            } else {
                log.warn("mvnDebug模式，跳过项目文件快照回滚");
            }
            throw new MojoExecutionException(e);
        }
    }

    private MojoConfig initParam() throws IOException {
        log.info("project={}", project);
        log.info("basedir={}", project.getBasedir());

        // 从 YAML 文件反序列化 MojoConfig
        File configFile = getCanonicalFileRelativeToBasedir(new File(configYmlPath));
        log.info("configYmlPath={}", configFile);
        Yaml yaml = new Yaml(new Constructor(MojoConfig.class, new LoaderOptions()));
        MojoConfig config;
        try (FileInputStream fis = new FileInputStream(configFile)) {
            config = yaml.load(fis);
        }

        // 将 mapperXmlDirs 转换为相对于 basedir 的绝对路径
        config.setMapperXmlDirs(config.getMapperXmlDirs().stream().map(this::getCanonicalFileRelativeToBasedir)
                        .collect(Collectors.toList()));
        log.info("config={}", JsonUtils.toJsonPrettily(config));
        return config;
    }

    public abstract Allison1875Module newAllison1875Module(MojoConfig config, ClassLoader classLoader) throws Exception;

    private ClassLoader getClassLoader(MavenProject project) throws Exception {
        List<String> classpathElements = project.getCompileClasspathElements();
        log.debug("classpathElements={}", JsonUtils.toJson(classpathElements));
        classpathElements.add(project.getBuild().getOutputDirectory());
        classpathElements.add(project.getBuild().getTestOutputDirectory());
        URL[] urls = new URL[classpathElements.size()];
        for (int i = 0; i < classpathElements.size(); ++i) {
            urls[i] = new File(classpathElements.get(i)).toURL();
        }
        return new URLClassLoader(urls, this.getClass().getClassLoader());
    }

    /**
     * 检测当前Maven是否通过mvnDebug执行
     * <p>
     * mvnDebug会在JVM启动参数中添加JDWP（Java Debug Wire Protocol）相关参数，
     * 如 -agentlib:jdwp=... 或 -Xrunjdwp:...
     *
     * @return true表示当前为mvnDebug模式
     */
    private boolean isMavenDebugMode() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.contains("jdwp")) {
                return true;
            }
        }
        return false;
    }

    protected File getCanonicalFileRelativeToBasedir(File file) {
        try {
            return project.getBasedir().toPath().resolve(file.toPath()).toFile().getCanonicalFile();
        } catch (IOException e) {
            log.warn("fail to getCanonicalFile, basedir={} file={}", project.getBasedir(), file, e);
            return file;
        }
    }

}
