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
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainConfig;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.enums.ToolEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
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

    public static void prepareDomain(Config config, String domainName) {
        DomainConfig domainConfig = resolveDomain(config, domainName);
        log.info("targetDomain={}", JsonUtils.toJson(domainConfig));
        domainConfig = resolveSourceRoots(domainConfig);
        DomainContext.set(domainConfig);
    }

    public static void letsGo(ToolEnum tool, Config config, String domainName) {
        prepareDomain(config, domainName);

        Allison1875Module allison1875Module = buildSimpleModule(tool, config);

        List<Module> guiceModules = Lists.newArrayList(allison1875Module);

        Injector injector;
        try {
            injector = Guice.createInjector(guiceModules);
        } catch (CreationException e) {
            if (e.getCause() instanceof Allison1875Exception) {
                throw (Allison1875Exception) e.getCause();
            }
            throw e;
        }

        try {
            injector.getInstance(allison1875Module.declareMainService()).play();
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
    private static DomainConfig resolveSourceRoots(DomainConfig domainConfig) {
        Path persistenceModule = Paths.get(domainConfig.getPersistenceModule());
        return domainConfig.toBuilder()
                .controllerSourceRoot(Paths.get(domainConfig.getControllerModule(), "src/main/java"))
                .dtoSourceRoot(Paths.get(domainConfig.getDtoModule(), "src/main/java"))
                .enumSourceRoot(Paths.get(domainConfig.getEnumModule(), "src/main/java"))
                .serviceSourceRoot(Paths.get(domainConfig.getServiceModule(), "src/main/java"))
                .serviceImplSourceRoot(Paths.get(domainConfig.getServiceImplModule(), "src/main/java"))
                .persistenceSourceRoot(Paths.get(domainConfig.getPersistenceModule(), "src/main/java"))
                .mapperXmlDirs(domainConfig.getMapperXmlDirs().stream()
                        .map(dir -> persistenceModule.resolve(dir.toPath()).toFile())
                        .collect(Collectors.toList()))
                .build();
    }

    private static Allison1875Module buildSimpleModule(ToolEnum tool, Config config) {
        String moduleClassName = tool.getModuleClassNameGetter().apply(config);
        log.info("allison1875Model={}", moduleClassName);
        try {
            return (Allison1875Module) Class.forName(moduleClassName).getConstructor(Config.class).newInstance(config);
        } catch (Exception e) {
            throw new Allison1875Exception("构造Allison1875Module失败: " + moduleClassName, e);
        }
    }

}
