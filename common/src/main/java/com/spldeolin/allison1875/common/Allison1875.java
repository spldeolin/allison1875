package com.spldeolin.allison1875.common;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainConfig;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.enums.ToolEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.guice.ValidationModule;
import com.spldeolin.allison1875.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Allison1875
 *
 * @author Deolin 2020-12-06
 */
@Slf4j
public class Allison1875 {

    public static void hello() {
        try (Reader reader = Resources.asCharSource(Resources.getResource("allison1875-git.properties"),
                StandardCharsets.UTF_8).openStream()) {

            // read allison1875-banner.txt
            String banner = Resources.toString(Resources.getResource("allison1875-banner.txt"), StandardCharsets.UTF_8);

            // read allison1875-git.properties
            Properties properties = new Properties();
            properties.load(reader);
            banner = banner.replace("${commitId}", properties.getProperty("git.commit.id.abbrev"));

            // replace placeholders
            String version = properties.getProperty("git.build.version");
            banner = banner.replace("${buildVersion}", version);

            // print banner
            log.info(banner);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void letsGo(ToolEnum tool, Config config, String domainName) {
        // 解析domain
        DomainConfig domainConfig = resolveDomain(config, domainName);
        log.info("targetDomain={}", JsonUtils.toJson(domainConfig));

        // 解析domain中各层的sourceRoot
        resolveSourceRoots(domainConfig);

        // 设置DomainContext
        DomainContext.set(domainConfig);

        // 根据tool构造Allison1875Module
        Allison1875Module allison1875Module = buildModule(tool, config);

        // append built-in guice modules
        List<Module> guiceModules = Lists.newArrayList(allison1875Module, new ValidationModule());

        // create guice container
        Injector injector;
        try {
            injector = Guice.createInjector(guiceModules);
        } catch (CreationException e) {
            if (e.getCause() instanceof Allison1875Exception) {
                throw (Allison1875Exception) e.getCause();
            }
            throw e;
        }

        // process main service
        try {
            injector.getInstance(allison1875Module.declareMainService()).process();
        } catch (Throwable e) {
            log.error("main process failed", e);
            throw new Allison1875Exception(e);
        }
    }

    /**
     * 根据 domainName 参数解析出目标 DomainConfig。
     * 仅有一个domain时，domainName可省略（null或空）；多个domain时，domainName必须指定。
     */
    private static DomainConfig resolveDomain(Config config, String domainName) {
        List<DomainConfig> domains = config.getDomains();
        if (domains == null || domains.isEmpty()) {
            throw new Allison1875Exception("配置文件中未定义任何domain");
        }
        if (domainName == null || domainName.isEmpty()) {
            if (domains.size() == 1) {
                return domains.get(0);
            }
            throw new Allison1875Exception(
                    "配置文件中定义了多个domain，必须指定要处理的业务领域。可选值: " + domains.stream()
                            .map(DomainConfig::getName).collect(Collectors.joining(", ")));
        }
        return domains.stream().filter(d -> domainName.equals(d.getName())).findFirst().orElseThrow(
                () -> new Allison1875Exception("未找到名为 '" + domainName + "' 的domain。可选值: " + domains.stream()
                        .map(DomainConfig::getName).collect(Collectors.joining(", "))));
    }

    /**
     * 解析 DomainConfig 中各 *Module 字段到对应的 *SourceRoot 路径，
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
        domainConfig.setMapperXmlDirs(
                domainConfig.getMapperXmlDirs().stream().map(dir -> persistenceModule.resolve(dir.toPath()).toFile())
                        .collect(Collectors.toList()));
    }

    /**
     * 根据tool构造对应的Allison1875Module实例
     */
    private static Allison1875Module buildModule(ToolEnum tool, Config config) {
        if (tool.isComposite()) {
            return buildCompositeModule(tool, config);
        }
        return buildSimpleModule(tool, config);
    }

    /**
     * 构造简单的（非组合工具的）Allison1875Module
     */
    private static Allison1875Module buildSimpleModule(ToolEnum tool, Config config) {
        String moduleClassName = tool.getModuleClassNameGetter().apply(config);
        log.info("allison1875Model={}", moduleClassName);
        try {
            return (Allison1875Module) Class.forName(moduleClassName).getConstructor(Config.class).newInstance(config);
        } catch (Exception e) {
            throw new Allison1875Exception("构造Allison1875Module失败: " + moduleClassName, e);
        }
    }

    /**
     * 组合工具特殊处理：合并多个子工具Module（目前仅form-generator）
     */
    private static Allison1875Module buildCompositeModule(ToolEnum tool, Config config) {
        try {
            Module persistenceModule = loadModule(config.getPersistenceGeneratorModule(), config);
            Module handlerModule = loadModule(config.getHandlerTransformerModule(), config);
            Module docModule = loadModule(config.getDocAnalyzerModule(), config);
            Module queryModule = loadModule(config.getQueryTransformerModule(), config);

            String mainModuleClassName = tool.getModuleClassNameGetter().apply(config);
            Allison1875Module mainModule = (Allison1875Module) Class.forName(mainModuleClassName)
                    .getConstructor(Config.class).newInstance(config);

            // 子module依次override，最后FormGenerator覆盖全部
            Module combined = Modules.override(persistenceModule).with(handlerModule);
            combined = Modules.override(combined).with(docModule);
            combined = Modules.override(combined).with(queryModule);
            combined = Modules.override(combined).with(mainModule);

            final Module finalCombined = combined;
            return new Allison1875Module() {
                @Override
                public Class<? extends Allison1875MainService> declareMainService() {
                    return mainModule.declareMainService();
                }

                @Override
                protected void configure() {
                    install(finalCombined);
                }
            };
        } catch (Exception e) {
            throw new Allison1875Exception("构造组合工具Module失败: " + tool.getToolName(), e);
        }
    }

    /**
     * 通过反射加载并实例化指定module类（统一使用Config单参数构造器）
     */
    private static Module loadModule(String moduleClassName, Config config) throws Exception {
        Class<?> moduleClass = Class.forName(moduleClassName);
        log.info("load module: {}", moduleClassName);
        return (Module) moduleClass.getConstructor(Config.class).newInstance(config);
    }

}