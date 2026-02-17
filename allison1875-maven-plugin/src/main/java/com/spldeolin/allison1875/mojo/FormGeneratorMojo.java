package com.spldeolin.allison1875.mojo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.Element;
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
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.formgenerator.FormGeneratorConfig;
import com.spldeolin.allison1875.handlertransformer.config.HandlerTransformerConfig;
import com.spldeolin.allison1875.persistencegenerator.config.PersistenceGeneratorConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Form Generator Mojo - 支持链式执行
 * 执行顺序：FormGenerator -> Maven Compile -> QueryTransformer
 *
 * @author Deolin 2024-06-14
 */
@Mojo(name = "form-generator", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
@Slf4j
public class FormGeneratorMojo extends Allison1875Mojo {

    @Parameter(alias = "formGenerator")
    private final FormGeneratorMojoConfig formGeneratorConfig = new FormGeneratorMojoConfig();

    @Parameter(alias = "persistenceGenerator")
    private final PersistenceGeneratorMojoConfig persistenceGeneratorConfig = new PersistenceGeneratorMojoConfig();

    @Parameter(alias = "handlerTransformer")
    private final HandlerTransformerMojoConfig handlerTransformerConfig = new HandlerTransformerMojoConfig();

    @Parameter(alias = "queryTransformer")
    private final QueryTransformerMojoConfig queryTransformerConfig = new QueryTransformerMojoConfig();

    /**
     * 是否启用链式执行（默认启用）
     * 如果设置为 false，则只执行 FormGenerator 本身
     */
    @Parameter(property = "enableChainExecution", defaultValue = "true")
    private boolean enableChainExecution;

    /**
     * 编译时使用的 Java 源码版本
     * 如果不指定，会自动检测项目的 Java 版本
     */
    @Parameter(property = "maven.compiler.source")
    private String compilerSource;

    /**
     * 编译时使用的 Java 目标版本
     * 如果不指定，会自动检测项目的 Java 版本
     */
    @Parameter(property = "maven.compiler.target")
    private String compilerTarget;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            log.info("=== 开始执行 FormGeneratorMojo (链式执行: {}) ===", enableChainExecution);

            // 1. 执行 FormGenerator 本身的逻辑
            log.info("第1步: 执行 FormGenerator");
            super.execute();
            log.info("FormGenerator 执行完成");

            // 如果不启用链式执行，直接返回
            if (!enableChainExecution) {
                log.info("链式执行已禁用，FormGenerator 单独执行完成");
                return;
            }

            // 2. 执行 Maven 编译
            log.info("第2步: 执行 Maven 编译");
            executeCompile();
            log.info("Maven 编译执行完成");

            // 3. 执行 QueryTransformerMojo
            log.info("第3步: 执行 QueryTransformer");
            executeQueryTransformer();
            log.info("QueryTransformer 执行完成");

            log.info("=== FormGeneratorMojo 链式执行全部完成 ===");

        } catch (Exception e) {
            log.error("FormGeneratorMojo 链式执行失败", e);
            throw new MojoExecutionException("FormGeneratorMojo 链式执行失败", e);
        }
    }

    /**
     * 执行 Maven 编译
     */
    private void executeCompile() throws Exception {
        try {
            // 检测和设置 Java 版本
            String[] javaVersions = detectJavaVersion();
            String sourceVersion = "21";
            String targetVersion = "21";

            log.info("检测到的编译版本配置: source={}, target={}", sourceVersion, targetVersion);

            // 构建编译配置
            Element[] configElements = buildCompilerConfiguration(sourceVersion, targetVersion);

            executeMojo(
                    plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"), version("3.11.0")),
                    goal("compile"), configuration(configElements),
                    executionEnvironment(project, mavenSession, pluginManager));
        } catch (Exception e) {
            log.error("Maven 编译执行失败", e);
            throw new Exception("Maven 编译失败", e);
        }
    }

    /**
     * 检测项目的 Java 版本配置
     *
     * @return [source, target] 版本数组
     */
    private String[] detectJavaVersion() {
        String sourceVersion = null;
        String targetVersion = null;

        // 1. 优先使用命令行参数
        if (compilerSource != null) {
            sourceVersion = compilerSource;
        }
        if (compilerTarget != null) {
            targetVersion = compilerTarget;
        }

        // 2. 从项目属性中获取
        if (sourceVersion == null) {
            sourceVersion = project.getProperties().getProperty("maven.compiler.source");
        }
        if (targetVersion == null) {
            targetVersion = project.getProperties().getProperty("maven.compiler.target");
        }

        // 3. 检测运行时 JVM 版本
        if (sourceVersion == null || targetVersion == null) {
            String jvmVersion = detectJvmVersion();
            if (sourceVersion == null) {
                sourceVersion = jvmVersion;
            }
            if (targetVersion == null) {
                targetVersion = jvmVersion;
            }
        }

        // 4. 最后的默认值
        if (sourceVersion == null) {
            sourceVersion = "11";  // 默认使用 Java 11，支持大部分现代特性
        }
        if (targetVersion == null) {
            targetVersion = sourceVersion;
        }

        // 5. 版本标准化
        sourceVersion = normalizeJavaVersion(sourceVersion);
        targetVersion = normalizeJavaVersion(targetVersion);

        log.info("最终使用的 Java 版本: source={}, target={}", sourceVersion, targetVersion);

        return new String[]{sourceVersion, targetVersion};
    }

    /**
     * 检测当前 JVM 版本
     */
    private String detectJvmVersion() {
        String javaVersion = System.getProperty("java.version");
        log.debug("检测到的 JVM 版本: {}", javaVersion);

        // 解析版本号
        if (javaVersion.startsWith("1.")) {
            // Java 8 及以下版本格式: 1.8.0_xxx
            return javaVersion.substring(2, 3);
        } else {
            // Java 9+ 版本格式: 11.0.1, 17.0.2, 21.0.1
            int dotIndex = javaVersion.indexOf('.');
            if (dotIndex > 0) {
                return javaVersion.substring(0, dotIndex);
            } else {
                return javaVersion;
            }
        }
    }

    /**
     * 标准化 Java 版本字符串
     */
    private String normalizeJavaVersion(String version) {
        if (version == null) {
            return "11";
        }

        // 移除可能的前缀和后缀
        version = version.trim();
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }

        // 提取主版本号
        int dotIndex = version.indexOf('.');
        if (dotIndex > 0) {
            version = version.substring(0, dotIndex);
        }

        // 验证版本号
        try {
            int versionNum = Integer.parseInt(version);
            if (versionNum < 8) {
                log.warn("检测到的 Java 版本过低: {}, 使用默认版本 11", version);
                return "11";
            }
            return String.valueOf(versionNum);
        } catch (NumberFormatException e) {
            log.warn("无法解析 Java 版本: {}, 使用默认版本 11", version);
            return "11";
        }
    }

    /**
     * 构建编译器配置
     */
    private Element[] buildCompilerConfiguration(String sourceVersion, String targetVersion) {
        java.util.List<Element> elements = new java.util.ArrayList<>();

        // 基础配置
        elements.add(element(name("source"), sourceVersion));
        elements.add(element(name("target"), targetVersion));
        elements.add(element(name("encoding"), "UTF-8"));

        // 根据版本添加特殊配置
        int sourceVersionNum = Integer.parseInt(sourceVersion);

        // 编译器参数
        java.util.List<Element> compilerArgs = new java.util.ArrayList<>();

        // Java 14+ 支持 record 关键字
        if (sourceVersionNum >= 14) {
            log.info("检测到 Java {}, 启用 record 语法支持", sourceVersion);
            // record 在 Java 14 是预览特性，Java 16 成为正式特性
            if (sourceVersionNum == 14 || sourceVersionNum == 15) {
                compilerArgs.add(element(name("arg"), "--enable-preview"));
                log.info("为 Java {} 启用预览特性支持", sourceVersion);
            }
        }

        // Java 21+ 特殊支持
        if (sourceVersionNum >= 21) {
            log.info("检测到 Java {}, 启用现代语法支持", sourceVersion);
            // 可以添加 Java 21+ 特有的编译器参数
        }

        // 通用编译器参数
        compilerArgs.add(element(name("arg"), "-parameters")); // 保留参数名
        compilerArgs.add(element(name("arg"), "-Xlint:unchecked")); // 显示未检查的警告

        // 如果有编译器参数，添加到配置中
        Element[] argsArray = compilerArgs.toArray(new Element[0]);
        elements.add(element(name("compilerArgs"), argsArray));

        log.debug("编译器配置: source={}, target={}, args={}", sourceVersion, targetVersion, compilerArgs.size());

        return elements.toArray(new Element[0]);
    }

    /**
     * 执行 QueryTransformerMojo
     */
    private void executeQueryTransformer() throws Exception {
        try {
            log.info("准备执行 QueryTransformer，构建配置参数");

            // 构建完整的配置传递给 query-transformer
            Element[] configElements = buildQueryTransformerConfiguration();

            // 使用 MojoExecutor 执行 allison1875:query-transformer
            executeMojo(plugin(groupId("com.spldeolin.allison1875"), artifactId("allison1875-maven-plugin"),
                            version("13.0-SNAPSHOT")), goal("query-transformer"), configuration(configElements),
                    executionEnvironment(project, mavenSession, pluginManager));
        } catch (Exception e) {
            log.error("QueryTransformer 执行失败", e);
            throw new Exception("QueryTransformer 执行失败", e);
        }
    }

    /**
     * 构建传递给 QueryTransformer 的完整配置
     */
    private Element[] buildQueryTransformerConfiguration() {
        java.util.List<Element> elements = new java.util.ArrayList<>();

        // 1. 传递 common 配置
        Element commonElement = buildCommonConfigElement();
        if (commonElement != null) {
            elements.add(commonElement);
        }

        // 2. 传递 queryTransformer 配置
        Element queryTransformerElement = buildQueryTransformerConfigElement();
        if (queryTransformerElement != null) {
            elements.add(queryTransformerElement);
        }

        log.debug("构建的 QueryTransformer 配置元素数量: {}", elements.size());

        return elements.toArray(new Element[0]);
    }

    /**
     * 构建 common 配置元素
     */
    private Element buildCommonConfigElement() {
        java.util.List<Element> commonElements = new java.util.ArrayList<>();

        // 基础包配置 - 通过项目信息重新计算（与父类逻辑保持一致）
        String basePackage = project.getGroupId();
        if (basePackage != null) {
            commonElements.add(element(name("basePackage"), basePackage));

            // 根据 basePackage 推断其他包名（与父类逻辑保持一致）
            commonElements.add(element(name("controllerPackage"), basePackage + ".controller"));
            commonElements.add(element(name("reqDTOPackage"), basePackage + ".dto.req"));
            commonElements.add(element(name("respDTOPackage"), basePackage + ".dto.resp"));
            commonElements.add(element(name("enumPackage"), basePackage + ".enums"));
            commonElements.add(element(name("servicePackage"), basePackage + ".service"));
            commonElements.add(element(name("serviceImplPackage"), basePackage + ".service.impl"));
            commonElements.add(element(name("mapperPackage"), basePackage + ".mapper"));
            commonElements.add(element(name("entityPackage"), basePackage + ".entity"));
            commonElements.add(element(name("designPackage"), basePackage + ".design"));
            commonElements.add(element(name("paramDTOPackage"), basePackage + ".dto.param"));
            commonElements.add(element(name("recordDTOPackage"), basePackage + ".dto.record"));
            commonElements.add(element(name("wholeDTOPackage"), basePackage + ".dto"));

            log.debug("构建 common 配置，basePackage: {}", basePackage);
        }

        // Mapper XML 目录配置
        commonElements.add(element(name("mapperXmlDirs"), element(name("mapperXmlDir"), "src/main/resources/mapper")));

        if (commonElements.isEmpty()) {
            return null;
        }

        Element[] commonArray = commonElements.toArray(new Element[0]);
        return element(name("common"), commonArray);
    }

    /**
     * 构建 queryTransformer 配置元素
     */
    private Element buildQueryTransformerConfigElement() {
        java.util.List<Element> qtElements = new java.util.ArrayList<>();

        // 从当前 FormGeneratorMojo 的 queryTransformerConfig 获取配置
        if (queryTransformerConfig != null) {
            // 持久层源码路径
            if (queryTransformerConfig.getPersistenceSourcePath() != null) {
                qtElements.add(element(name("persistenceSourcePath"),
                        String.valueOf(queryTransformerConfig.getPersistenceSourcePath())));
            } else {
                // 默认值
                qtElements.add(element(name("persistenceSourcePath"), "src/main/java"));
            }

            // 模块类名
            if (queryTransformerConfig.getModule() != null) {
                qtElements.add(element(name("module"), queryTransformerConfig.getModule()));
            } else {
                // 默认模块
                qtElements.add(
                        element(name("module"), "com.spldeolin.allison1875.querytransformer.QueryTransformerModule"));
            }

            // 其他可能的 QueryTransformer 特有配置
            // 可以根据 QueryTransformerConfig 的具体字段来添加
        } else {
            // 如果没有配置，提供默认值
            qtElements.add(element(name("persistenceSourcePath"), "src/main/java"));
            qtElements.add(
                    element(name("module"), "com.spldeolin.allison1875.querytransformer.QueryTransformerModule"));
        }

        if (qtElements.isEmpty()) {
            return null;
        }

        Element[] qtArray = qtElements.toArray(new Element[0]);
        return element(name("queryTransformer"), qtArray);
    }

    @Override
    public Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader) throws Exception {
        // 对config对象中的文件路径进行相对basedir的处理
        formGeneratorConfig.setDslPath(super.getCanonicalFileRelativeToBasedir(formGeneratorConfig.getDslPath()));
        log.info("formGeneratorMojoConfig={}", JsonUtils.toJsonPrettily(formGeneratorConfig));
        log.info("new module instance for {}", formGeneratorConfig.getModule());
        return (Allison1875Module) classLoader.loadClass(formGeneratorConfig.getModule())
                .getConstructor(CommonConfig.class, FormGeneratorConfig.class, PersistenceGeneratorConfig.class,
                        HandlerTransformerConfig.class)
                .newInstance(commonConfig, formGeneratorConfig, persistenceGeneratorConfig, handlerTransformerConfig);
    }

}
