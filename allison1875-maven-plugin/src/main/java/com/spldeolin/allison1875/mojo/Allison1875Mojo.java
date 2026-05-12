package com.spldeolin.allison1875.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainConfig;
import com.spldeolin.allison1875.common.enums.ToolEnum;
import com.spldeolin.allison1875.common.util.FileSnapshotUtils;
import com.spldeolin.allison1875.common.util.FileSnapshotUtils.FileSystemSnapshot;
import com.spldeolin.allison1875.common.util.JsonUtils;
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

            // 加载配置
            Config config = loadConfig();

            // Mojo层特有的basedir路径解析
            resolveBasedirPaths(config);

            // 解析domain中各层的sourceRoot（Mojo模式下基于basedir的相对路径）
            resolveDomainSourceRootsForMojo(config);

            // 执行
            Allison1875.letsGo(getTool(), config, domain);

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
     * 返回当前Mojo对应的工具枚举
     */
    protected abstract ToolEnum getTool();

    /**
     * 供子类覆写：对config中需要基于basedir解析的文件路径字段进行处理。
     * 默认空实现。
     */
    protected void resolveBasedirPaths(Config config) {
    }

    /**
     * 检查当前module是否允许执行Allison 1875。
     */
    private void checkParentModule() throws MojoExecutionException {
        boolean hasLocalAggregatorParent = project.getParent() != null && project.getParent().getBasedir() != null;
        if (hasLocalAggregatorParent) {
            throw new MojoExecutionException("Allison 1875 必须在parent module或单模块工程中执行，当前module存在本地聚合parent: "
                    + project.getParent().getId());
        }
    }

    private Config loadConfig() throws IOException {
        log.info("project={}", project);
        log.info("basedir={}", project.getBasedir());

        File configFile = getCanonicalFileRelativeToBasedir(new File(configYmlPath));
        log.info("configYmlPath={}", configFile);
        Yaml yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
        Config config;
        try (FileInputStream fis = new FileInputStream(configFile)) {
            config = yaml.load(fis);
        }

        log.info("config={}", JsonUtils.toJsonPrettily(config));
        return config;
    }

    /**
     * Mojo模式下，DomainConfig中的*Module字段是相对basedir的相对路径，需要解析为绝对路径。
     * 因为 Allison1875.letsGo 内部的 resolveSourceRoots 直接 Paths.get(modulePath, "src/main/java")，
     * 所以这里把 *Module 从相对路径转为绝对路径。
     */
    private void resolveDomainSourceRootsForMojo(Config config) {
        File basedir = project.getBasedir();
        for (DomainConfig domainConfig : config.getDomains()) {
            domainConfig.setControllerModule(resolveModuleAbsolutePath(basedir, domainConfig.getControllerModule()));
            domainConfig.setDtoModule(resolveModuleAbsolutePath(basedir, domainConfig.getDtoModule()));
            domainConfig.setEnumModule(resolveModuleAbsolutePath(basedir, domainConfig.getEnumModule()));
            domainConfig.setServiceModule(resolveModuleAbsolutePath(basedir, domainConfig.getServiceModule()));
            domainConfig.setServiceImplModule(resolveModuleAbsolutePath(basedir, domainConfig.getServiceImplModule()));
            domainConfig.setPersistenceModule(resolveModuleAbsolutePath(basedir, domainConfig.getPersistenceModule()));
        }
    }

    /**
     * 将modulePath解析为绝对路径。
     * 如果modulePath为null或空，则使用basedir本身。
     */
    private String resolveModuleAbsolutePath(File basedir, String modulePath) {
        if (modulePath == null || modulePath.isEmpty()) {
            return getCanonicalFile(basedir).getAbsolutePath();
        }
        return getCanonicalFile(new File(basedir, modulePath)).getAbsolutePath();
    }

    /**
     * 检测当前Maven是否通过mvnDebug执行
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
