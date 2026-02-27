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
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.config.DocAnalyzerConfig;
import com.spldeolin.allison1875.formgenerator.CompileFacade;
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

    @Parameter(alias = "docAnalyzer")
    private final DocAnalyzerMojoConfig docAnalyzerConfig = new DocAnalyzerMojoConfig();

    @Parameter(alias = "queryTransformer")
    private final QueryTransformerMojoConfig queryTransformerConfig = new QueryTransformerMojoConfig();

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException {
        // 1. 执行 FormGenerator 本身的逻辑
        log.info("第1步: 执行 FormGenerator");
        super.execute();
        log.info("FormGenerator 执行完成");

        // 2. 执行 Maven 编译
        log.info("第2步: 执行 Maven 编译");
        executeCompile();
        log.info("Maven 编译执行完成");

        // 3. 执行 QueryTransformerMojo
        log.info("第3步: 执行 QueryTransformer");
        executeQueryTransformer();
        log.info("QueryTransformer 执行完成");
    }

    /**
     * 执行 Maven 编译
     */
    private void executeCompile() throws MojoExecutionException {
        String javaVersion = commonConfig.getJavaVersion();
        log.info("使用编译版本: {}", javaVersion);

        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"), version("3.11.0")),
                goal("compile"),
                configuration(element(name("source"), javaVersion), element(name("target"), javaVersion),
                        element(name("encoding"), "UTF-8"),
                        element(name("compilerArgs"), element(name("arg"), "-parameters"),
                                element(name("arg"), "-Xlint:unchecked"))),
                executionEnvironment(project, mavenSession, pluginManager));
    }

    /**
     * 执行 QueryTransformerMojo
     */
    private void executeQueryTransformer() throws MojoExecutionException {
        log.info("准备执行 QueryTransformer，构建配置参数");

        // 构建完整的配置传递给 query-transformer
        Element[] configElements = buildQueryTransformerConfiguration();

        // 使用 MojoExecutor 执行 allison1875:query-transformer
        executeMojo(plugin(groupId("com.spldeolin.allison1875"), artifactId("allison1875-maven-plugin"),
                        version("13.0-SNAPSHOT")), goal("query-transformer"), configuration(configElements),
                executionEnvironment(project, mavenSession, pluginManager));
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
        if (queryTransformerConfig.getPersistenceSourcePath() != null) {
            queryTransformerConfig.setPersistenceSourcePath(
                    super.getCanonicalFileRelativeToBasedir(queryTransformerConfig.getPersistenceSourcePath()));
        }
        log.info("formGeneratorMojoConfig={}", JsonUtils.toJsonPrettily(formGeneratorConfig));

        CompileFacade compileFacade = (astForest, javaVersion) -> {
            try {
                executeCompile();
            } catch (MojoExecutionException e) {
                throw new Allison1875Exception(e);
            }
        };

        // 1. 加载各 config 指定的 module 实例
        Module persistenceModule = loadModule(classLoader, persistenceGeneratorConfig.getModule(), commonConfig,
                persistenceGeneratorConfig);
        Module handlerModule = loadModule(classLoader, handlerTransformerConfig.getModule(), commonConfig,
                handlerTransformerConfig);
        Module docModule = loadModule(classLoader, docAnalyzerConfig.getModule(), commonConfig, docAnalyzerConfig);
        Module queryModule = loadModule(classLoader, queryTransformerConfig.getModule(), commonConfig,
                queryTransformerConfig);

        // 2. 加载 FormGeneratorModule
        Allison1875Module formGeneratorModule = (Allison1875Module) classLoader.loadClass(
                        formGeneratorConfig.getModule())
                .getConstructor(CommonConfig.class, FormGeneratorConfig.class, PersistenceGeneratorConfig.class,
                        HandlerTransformerConfig.class, DocAnalyzerConfig.class, CompileFacade.class)
                .newInstance(commonConfig, formGeneratorConfig, persistenceGeneratorConfig, handlerTransformerConfig,
                        docAnalyzerConfig, compileFacade);

        // 3. 合并：子 module 依次 override（后者覆盖前者），最后 FormGenerator 覆盖全部，bind 冲突以 FormGenerator 为准
        Module combined = Modules.override(persistenceModule).with(handlerModule);
        combined = Modules.override(combined).with(docModule);
        combined = Modules.override(combined).with(queryModule);
        combined = Modules.override(combined).with(formGeneratorModule);

        final Module finalCombined = combined;
        return new Allison1875Module() {
            @Override
            public Class<? extends Allison1875MainService> declareMainService() {
                return formGeneratorModule.declareMainService();
            }

            @Override
            protected void configure() {
                install(finalCombined);
            }
        };
    }

    /**
     * 通过反射加载并实例化指定 module 类
     */
    private Module loadModule(ClassLoader classLoader, String moduleClassName, CommonConfig commonConfig, Object config)
            throws Exception {
        Class<?> moduleClass = classLoader.loadClass(moduleClassName);
        log.info("load module: {}", moduleClassName);
        return (Module) moduleClass.getConstructor(CommonConfig.class, config.getClass().getSuperclass())
                .newInstance(commonConfig, config);
    }

}
