package com.spldeolin.allison1875.mojo;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.formgenerator.CompileFacade;
import com.spldeolin.allison1875.mojo.facade.CompileFacadeImpl;
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

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public Allison1875Module newAllison1875Module(MojoConfig config, ClassLoader classLoader) throws Exception {
        // 对config对象中的文件路径进行相对basedir的处理
        if (config.getDslPath() != null) {
            config.setDslPath(super.getCanonicalFileRelativeToBasedir(config.getDslPath()));
        }
        if (config.getPersistenceSourcePath() != null) {
            config.setPersistenceSourcePath(super.getCanonicalFileRelativeToBasedir(config.getPersistenceSourcePath()));
        }

        // 构造CompileFacade实现类
        CompileFacade compileFacade = new CompileFacadeImpl(project, mavenSession, pluginManager);

        // 1. 加载各 config 指定的 module 实例
        Module persistenceModule = loadModule(classLoader, config.getPersistenceGeneratorModule(), config);
        Module handlerModule = loadModule(classLoader, config.getHandlerTransformerModule(), config);
        Module docModule = loadModule(classLoader, config.getDocAnalyzerModule(), config);
        Module queryModule = loadModule(classLoader, config.getQueryTransformerModule(), config);

        // 2. 加载 FormGeneratorModule（需要 CompileFacade，单独处理）
        Allison1875Module formGeneratorModule = (Allison1875Module) classLoader.loadClass(
                        config.getFormGeneratorModule()).getConstructor(Config.class, CompileFacade.class)
                .newInstance(config, compileFacade);

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
     * 通过反射加载并实例化指定 module 类（统一使用 Config 单参数构造器）
     */
    private Module loadModule(ClassLoader classLoader, String moduleClassName, MojoConfig config) throws Exception {
        Class<?> moduleClass = classLoader.loadClass(moduleClassName);
        log.info("load module: {}", moduleClassName);
        return (Module) moduleClass.getConstructor(Config.class).newInstance(config);
    }

}
