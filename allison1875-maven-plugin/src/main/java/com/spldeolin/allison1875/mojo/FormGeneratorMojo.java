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
import com.google.common.base.Joiner;
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
            String javaVersion = commonConfig.getJavaVersion();
            log.info("使用编译版本: {}", javaVersion);

            executeMojo(
                    plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"), version("3.11.0")),
                    goal("compile"),
                    configuration(element(name("source"), javaVersion), element(name("target"), javaVersion),
                            element(name("encoding"), "UTF-8"),
                            element(name("compilerArgs"), element(name("arg"), "-parameters"),
                                    element(name("arg"), "-Xlint:unchecked"))),
                    executionEnvironment(project, mavenSession, pluginManager));
        } catch (Exception e) {
            log.error("Maven 编译执行失败", e);
            throw new Exception("Maven 编译失败", e);
        }
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
        elements.add(commonElement);

        // 2. 传递 queryTransformer 配置
        Element queryTransformerElement = buildQueryTransformerConfigElement();
        elements.add(queryTransformerElement);

        log.debug("构建的 QueryTransformer 配置元素数量: {}", elements.size());

        return elements.toArray(new Element[0]);
    }

    /**
     * 构建 common 配置元素
     */
    private Element buildCommonConfigElement() {
        java.util.List<Element> commonElements = new java.util.ArrayList<>();
        commonElements.add(element(name("basePackage"), commonConfig.getBasePackage()));
        commonElements.add(element(name("controllerPackage"), commonConfig.getControllerPackage()));
        commonElements.add(element(name("reqDTOPackage"), commonConfig.getReqDTOPackage()));
        commonElements.add(element(name("respDTOPackage"), commonConfig.getRespDTOPackage()));
        commonElements.add(element(name("enumPackage"), commonConfig.getEnumPackage()));
        commonElements.add(element(name("servicePackage"), commonConfig.getServicePackage()));
        commonElements.add(element(name("serviceImplPackage"), commonConfig.getServiceImplPackage()));
        commonElements.add(element(name("mapperPackage"), commonConfig.getMapperPackage()));
        commonElements.add(element(name("entityPackage"), commonConfig.getEntityPackage()));
        commonElements.add(element(name("designPackage"), commonConfig.getDesignPackage()));
        commonElements.add(element(name("paramDTOPackage"), commonConfig.getParamDTOPackage()));
        commonElements.add(element(name("recordDTOPackage"), commonConfig.getRecordDTOPackage()));
        commonElements.add(element(name("wholeDTOPackage"), commonConfig.getWholeDTOPackage()));
        commonElements.add(element(name("mapperXmlDirs"), Joiner.on(",").join(commonConfig.getMapperXmlDirs())));
        commonElements.add(element(name("author"), commonConfig.getAuthor()));
        commonElements.add(
                element(name("isDataModelSerializable"), String.valueOf(commonConfig.getIsDataModelSerializable())));
        commonElements.add(
                element(name("isDataModelCloneable"), String.valueOf(commonConfig.getIsDataModelCloneable())));
        commonElements.add(element(name("isDataModuleWithoutLombok"),
                String.valueOf(commonConfig.getIsDataModuleWithoutLombok())));
        commonElements.add(
                element(name("enableNoModifyAnnounce"), String.valueOf(commonConfig.getEnableNoModifyAnnounce())));
        commonElements.add(element(name("enableLotNoAnnounce"), String.valueOf(commonConfig.getEnableLotNoAnnounce())));
        commonElements.add(
                element(name("enableJavaxMoveToJakarta"), String.valueOf(commonConfig.getEnableJavaxMoveToJakarta())));
        Element[] commonArray = commonElements.toArray(new Element[0]);
        return element(name("commonConfig"), commonArray);
    }

    /**
     * 构建 queryTransformer 配置元素
     */
    private Element buildQueryTransformerConfigElement() {
        java.util.List<Element> qtElements = new java.util.ArrayList<>();
        if (queryTransformerConfig.getPersistenceSourcePath() != null) {
            qtElements.add(element(name("persistenceSourcePath"),
                    String.valueOf(queryTransformerConfig.getPersistenceSourcePath())));
        }
        qtElements.add(element(name("module"), String.valueOf(queryTransformerConfig.getModule())));
        Element[] qtArray = qtElements.toArray(new Element[0]);
        return element(name("queryTransformerConfig"), qtArray);
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
