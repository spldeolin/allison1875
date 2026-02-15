package com.spldeolin.allison1875.mojo;

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
