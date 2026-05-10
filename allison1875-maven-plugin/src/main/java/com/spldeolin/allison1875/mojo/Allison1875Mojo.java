package com.spldeolin.allison1875.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
import com.spldeolin.allison1875.common.config.DomainConfig;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
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

    /**
     * 指定要处理的业务领域名称，当domains列表中仅有一个时可省略
     */
    @Parameter(property = "domain")
    private String domain;

    @Override
    public void execute() throws MojoExecutionException {
        // 检查是否在parent module或单模块工程中执行
        checkParentModule();

        // 为整个maven project拍摄快照
        FileSystemSnapshot fileSnapshot = FileSnapshotUtils.createSnapshot(project.getBasedir());

        try {
            // banner
            Allison1875.hello();

            // 构造guice module
            MojoConfig config = initParam();
            ClassLoader classLoader = getClassLoader(project);

            // 解析domain
            DomainConfig domainConfig = resolveDomain(config);
            log.info("domain={}", domainConfig.getName());

            // 解析domain中各层的sourceRoot
            resolveSourceRoots(domainConfig);

            // 构造guice module
            Allison1875Module allison1875Module = newAllison1875Module(config, classLoader);

            // 收集所有sourceRoot并构造AstForest
            Set<File> allSourceRoots = collectSourceRoots(domainConfig);
            File primarySourceRoot = getPrimarySourceRoot();
            log.info("allSourceRoots={}", allSourceRoots);
            AstForest astForest = new MavenProjectBuiltAstForest(classLoader, primarySourceRoot);
            Allison1875.letsGo(allison1875Module, astForest, domainConfig);

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

    /**
     * 检查当前module是否允许执行Allison 1875。
     */
    private void checkParentModule() throws MojoExecutionException {
        // 没有本地聚合parent → 允许执行；有本地聚合parent → 说明是被聚合的子module，不允许执行。
        // POM继承中的远程parent（如spring-boot-starter-parent）的basedir为null，不算本地聚合parent。
        boolean hasLocalAggregatorParent = project.getParent() != null && project.getParent().getBasedir() != null;
        if (hasLocalAggregatorParent) {
            throw new MojoExecutionException("Allison 1875 必须在parent module或单模块工程中执行，当前module存在本地聚合parent: "
                    + project.getParent().getId());
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

        log.info("config={}", JsonUtils.toJsonPrettily(config));
        return config;
    }

    /**
     * 根据 -Ddomain 参数解析出目标 DomainConfig。
     * 仅有一个domain时，-Ddomain可省略；多个domain时，-Ddomain必须指定。
     */
    private DomainConfig resolveDomain(MojoConfig config) {
        List<DomainConfig> domains = config.getDomains();
        if (domains == null || domains.isEmpty()) {
            throw new Allison1875Exception("配置文件中未定义任何domain");
        }
        if (domain == null || domain.isEmpty()) {
            if (domains.size() == 1) {
                return domains.get(0);
            }
            throw new Allison1875Exception("配置文件中定义了多个domain，必须通过 -Ddomain=<name> 指定要处理的业务领域。可选值: "
                    + domains.stream().map(DomainConfig::getName).collect(Collectors.joining(", ")));
        }
        return domains.stream().filter(d -> domain.equals(d.getName())).findFirst().orElseThrow(
                () -> new Allison1875Exception(
                        "未找到名为 '" + domain + "' 的domain。可选值: " + domains.stream().map(DomainConfig::getName)
                                .collect(Collectors.joining(", "))));
    }

    /**
     * 解析 DomainConfig 中各 *Module 字段到对应的 *SourceRoot 路径，
     * 同时将 mapperXmlDirs 转换为绝对路径。
     */
    private void resolveSourceRoots(DomainConfig domainConfig) {
        File basedir = project.getBasedir();

        domainConfig.setControllerSourceRoot(resolveModuleSourceRoot(basedir, domainConfig.getControllerModule()));
        domainConfig.setDtoSourceRoot(resolveModuleSourceRoot(basedir, domainConfig.getDtoModule()));
        domainConfig.setEnumSourceRoot(resolveModuleSourceRoot(basedir, domainConfig.getEnumModule()));
        domainConfig.setServiceSourceRoot(resolveModuleSourceRoot(basedir, domainConfig.getServiceModule()));
        domainConfig.setServiceImplSourceRoot(resolveModuleSourceRoot(basedir, domainConfig.getServiceImplModule()));
        domainConfig.setPersistenceSourceRoot(resolveModuleSourceRoot(basedir, domainConfig.getPersistenceModule()));

        // 将mapperXmlDirs转换为基于持久层module basedir的绝对路径
        File persistenceBasedir = resolvePersistenceBasedir(basedir, domainConfig.getPersistenceModule());
        domainConfig.setMapperXmlDirs(domainConfig.getMapperXmlDirs().stream()
                .map(dir -> getCanonicalFile(persistenceBasedir.toPath().resolve(dir.toPath()).toFile()))
                .collect(Collectors.toList()));
        log.info("resolved mapperXmlDirs={}", domainConfig.getMapperXmlDirs());
    }

    /**
     * 将 *Module 路径解析为 sourceRoot 的绝对路径。
     * 如果 modulePath 为 null，则使用当前执行module的 src/main/java。
     */
    private Path resolveModuleSourceRoot(File basedir, String modulePath) {
        if (modulePath == null || modulePath.isEmpty()) {
            return getCanonicalFile(new File(basedir, "src/main/java")).toPath();
        }
        return getCanonicalFile(new File(basedir, modulePath + "/src/main/java")).toPath();
    }

    /**
     * 解析持久层module的basedir
     */
    private File resolvePersistenceBasedir(File basedir, String persistenceModule) {
        if (persistenceModule == null || persistenceModule.isEmpty()) {
            return basedir;
        }
        return getCanonicalFile(new File(basedir, persistenceModule));
    }

    /**
     * 收集 DomainConfig 中所有不重复的 sourceRoot
     */
    private Set<File> collectSourceRoots(DomainConfig domainConfig) {
        Set<File> roots = new LinkedHashSet<>();
        addIfNotNull(roots, domainConfig.getControllerSourceRoot());
        addIfNotNull(roots, domainConfig.getDtoSourceRoot());
        addIfNotNull(roots, domainConfig.getEnumSourceRoot());
        addIfNotNull(roots, domainConfig.getServiceSourceRoot());
        addIfNotNull(roots, domainConfig.getServiceImplSourceRoot());
        addIfNotNull(roots, domainConfig.getPersistenceSourceRoot());
        return roots;
    }

    private void addIfNotNull(Set<File> roots, Path path) {
        if (path != null) {
            roots.add(path.toFile());
        }
    }

    /**
     * 获取当前执行module的主sourceRoot
     */
    private File getPrimarySourceRoot() {
        List<String> compileSourceRoots = project.getCompileSourceRoots();
        for (String root : compileSourceRoots) {
            if (!root.endsWith("generated-sources/annotations")) {
                return getCanonicalFile(new File(root));
            }
        }
        return getCanonicalFile(new File(project.getBasedir(), "src/main/java"));
    }

    public abstract Allison1875Module newAllison1875Module(MojoConfig config, ClassLoader classLoader) throws Exception;

    private ClassLoader getClassLoader(MavenProject project) throws Exception {
        List<String> classpathElements = new ArrayList<>(project.getCompileClasspathElements());
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

    private File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            log.warn("fail to getCanonicalFile, file={}", file, e);
            return file;
        }
    }

}
