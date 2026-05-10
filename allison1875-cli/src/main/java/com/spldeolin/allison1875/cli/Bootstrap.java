package com.spldeolin.allison1875.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import com.spldeolin.allison1875.cli.config.Allison1875ModuleConfig;
import com.spldeolin.allison1875.cli.util.MavenProjectClassLoaderUtils;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.DefaultAstForest;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainConfig;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yanshaowei01 2026-05-10
 */
@Slf4j
public class Bootstrap {

    public static void main(String[] args) {
        // 打印banner
        Allison1875.hello();

        // 解析CLI参数
        CliArgs cliArgs = parseArgs(args);
        log.info("toolName={} domainName={} configFile={}", cliArgs.toolName, cliArgs.domainName, cliArgs.configFile);

        // 读取.allison1875配置文件并反序列化，打印参数和配置
        Allison1875ModuleConfig config = loadConfig(cliArgs.configFile);
        log.info("config={}", config);

        // 解析domain配置和sourceRoot
        DomainConfig domainConfig = resolveDomain(config, cliArgs.domainName);
        resolveSourceRoots(domainConfig);
        log.info("domain={}", domainConfig);

        // 根据工具名确定sourceRoot和对应的module目录
        Path sourceRoot = getSourceRootByTool(cliArgs.toolName, domainConfig);
        String modulePath = getModulePathByTool(cliArgs.toolName, domainConfig);
        log.info("sourceRoot={} modulePath={}", sourceRoot, modulePath);

        // 构造ClassLoader
        ClassLoader classLoader = MavenProjectClassLoaderUtils.buildClassLoader(new File(modulePath));

        // 构造allison1875 module
        Allison1875Module allison1875Module = buildAllison1875Module(cliArgs.toolName, config);

        // 构造AstForest
        AstForest astForest = buildAstForest(classLoader, sourceRoot);

        // 执行allison1875
        Allison1875.letsGo(allison1875Module, astForest, domainConfig);
    }

    /**
     * 解析CLI参数，提取 --tool、--domain、--config
     */
    private static CliArgs parseArgs(String[] args) {
        String toolName = null;
        String domainName = null;
        String configFilePath = null;
        for (String arg : args) {
            if (arg.startsWith("--tool=")) {
                toolName = arg.substring("--tool=".length());
            } else if (arg.startsWith("--domain=")) {
                domainName = arg.substring("--domain=".length());
            } else if (arg.startsWith("--config=")) {
                configFilePath = arg.substring("--config=".length());
            }
        }
        if (toolName == null || toolName.isEmpty()) {
            throw new Allison1875Exception("必须通过 --tool=<toolName> 指定工具名（如 doc-analyzer）");
        }
        if (configFilePath == null || configFilePath.isEmpty()) {
            throw new Allison1875Exception("必须通过 --config=<path> 指定 .allison1875.yml 配置文件路径");
        }
        CliArgs cliArgs = new CliArgs();
        cliArgs.toolName = toolName;
        cliArgs.domainName = domainName;
        cliArgs.configFile = configFilePath;
        return cliArgs;
    }

    /**
     * 读取.allison1875配置文件并反序列化为Allison1875ModuleConfig
     */
    private static Allison1875ModuleConfig loadConfig(String configFilePath) {
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            throw new Allison1875Exception("配置文件不存在: " + configFile.getAbsolutePath());
        }
        Yaml yaml = new Yaml(new Constructor(Allison1875ModuleConfig.class, new LoaderOptions()));
        try (FileInputStream fis = new FileInputStream(configFile)) {
            return yaml.load(fis);
        } catch (IOException e) {
            throw new Allison1875Exception("读取配置文件失败: " + configFile.getAbsolutePath(), e);
        }
    }

    /**
     * 根据 domainName 参数解析出目标 DomainConfig。
     * 仅有一个domain时，domainName可省略；多个domain时，domainName必须指定。
     */
    private static DomainConfig resolveDomain(Allison1875ModuleConfig config, String domainName) {
        List<DomainConfig> domains = config.getDomains();
        if (domains == null || domains.isEmpty()) {
            throw new Allison1875Exception("配置文件中未定义任何domain");
        }
        if (domainName == null || domainName.isEmpty()) {
            if (domains.size() == 1) {
                return domains.get(0);
            }
            throw new Allison1875Exception("配置文件中定义了多个domain，必须通过 --domain=<name> 指定要处理的业务领域。可选值: "
                    + domains.stream().map(DomainConfig::getName).collect(Collectors.joining(", ")));
        }
        return domains.stream().filter(d -> domainName.equals(d.getName())).findFirst().orElseThrow(
                () -> new Allison1875Exception("未找到名为 '" + domainName + "' 的domain。可选值: " + domains.stream()
                        .map(DomainConfig::getName).collect(Collectors.joining(", "))));
    }

    /**
     * 解析 DomainConfig 中各 *Module 字段到对应的 *SourceRoot 路径（CLI模式下modulePath为绝对路径），
     * 同时将 mapperXmlDirs 转换为基于 persistenceModule 的绝对路径。
     */
    private static void resolveSourceRoots(DomainConfig domainConfig) {
        domainConfig.setControllerSourceRoot(Paths.get(domainConfig.getControllerModule(), "src/main/java"));
        domainConfig.setDtoSourceRoot(Paths.get(domainConfig.getDtoModule(), "src/main/java"));
        domainConfig.setEnumSourceRoot(Paths.get(domainConfig.getEnumModule(), "src/main/java"));
        domainConfig.setServiceSourceRoot(Paths.get(domainConfig.getServiceModule(), "src/main/java"));
        domainConfig.setServiceImplSourceRoot(Paths.get(domainConfig.getServiceImplModule(), "src/main/java"));
        domainConfig.setPersistenceSourceRoot(Paths.get(domainConfig.getPersistenceModule(), "src/main/java"));
        // 将mapperXmlDirs转换为基于持久层module的绝对路径
        Path persistenceModule = Paths.get(domainConfig.getPersistenceModule());
        domainConfig.setMapperXmlDirs(domainConfig.getMapperXmlDirs().stream()
                .map(dir -> persistenceModule.resolve(dir.toPath()).toFile())
                .collect(Collectors.toList()));
    }

    /**
     * 根据工具名获取对应的sourceRoot
     */
    private static Path getSourceRootByTool(String toolName, DomainConfig domainConfig) {
        switch (toolName) {
            case "doc-analyzer":
            case "handler-transformer":
            case "form-generator":
                return domainConfig.getControllerSourceRoot();
            case "persistence-generator":
                return domainConfig.getPersistenceSourceRoot();
            case "query-transformer":
            case "star-transformer":
                return domainConfig.getServiceImplSourceRoot();
            default:
                throw new Allison1875Exception("不支持的工具名: " + toolName
                        + "，可选值: doc-analyzer, handler-transformer, persistence-generator, query-transformer, "
                        + "star-transformer, form-generator");
        }
    }

    /**
     * 根据工具名获取对应的module目录路径（用于构建ClassLoader）
     */
    private static String getModulePathByTool(String toolName, DomainConfig domainConfig) {
        switch (toolName) {
            case "doc-analyzer":
            case "handler-transformer":
            case "form-generator":
                return domainConfig.getControllerModule();
            case "persistence-generator":
                return domainConfig.getPersistenceModule();
            case "query-transformer":
            case "star-transformer":
                return domainConfig.getServiceImplModule();
            default:
                throw new Allison1875Exception("不支持的工具名: " + toolName);
        }
    }

    /**
     * 根据工具名构造对应的Allison1875Module实例
     */
    private static Allison1875Module buildAllison1875Module(String toolName, Allison1875ModuleConfig config) {
        String moduleClassName = getModuleClassName(toolName, config);
        log.info("moduleClassName={}", moduleClassName);
        try {
            return (Allison1875Module) Class.forName(moduleClassName).getConstructor(Config.class).newInstance(config);
        } catch (Exception e) {
            throw new Allison1875Exception("构造Allison1875Module失败: " + moduleClassName, e);
        }
    }

    /**
     * 根据工具名从配置中获取对应的Module类全限定名
     */
    private static String getModuleClassName(String toolName, Allison1875ModuleConfig config) {
        switch (toolName) {
            case "doc-analyzer":
                return config.getDocAnalyzerModule();
            case "handler-transformer":
                return config.getHandlerTransformerModule();
            case "persistence-generator":
                return config.getPersistenceGeneratorModule();
            case "query-transformer":
                return config.getQueryTransformerModule();
            case "star-transformer":
                return config.getStarTransformerModule();
            case "form-generator":
                return config.getFormGeneratorModule();
            default:
                throw new Allison1875Exception("不支持的工具名: " + toolName);
        }
    }

    /**
     * 构造AstForest
     */
    private static AstForest buildAstForest(ClassLoader classLoader, Path sourceRoot) {
        DefaultAstForest astForest = new DefaultAstForest(classLoader, sourceRoot.toFile());
        AstForestContext.set(astForest);
        return astForest;
    }

    /**
     * CLI参数
     */
    private static class CliArgs {

        String toolName;

        String domainName;

        String configFile;

    }

}
