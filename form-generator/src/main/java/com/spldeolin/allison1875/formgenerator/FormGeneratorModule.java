package com.spldeolin.allison1875.formgenerator;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.guice.Allison1875Game;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorMapperLayerExpansionServiceImpl;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorServiceLayerExpansionServiceImpl;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerExpansionService;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerExpansionService;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-05-05
 */
@Slf4j
@ToString
public class FormGeneratorModule extends Allison1875Module {

    private final Config config;

    public FormGeneratorModule(Config config) {
        this.config = config;
    }

    @Override
    public final Class<? extends Allison1875Game> declareMainService() {
        return FormGenerator.class;
    }

    @Override
    protected void configure() {
        Module combined = loadModule(config.getPersistenceGeneratorModule(), config);
        combined = Modules.override(combined).with(loadModule(config.getHandlerTransformerModule(), config));
        combined = Modules.override(combined).with(loadModule(config.getDocAnalyzerModule(), config));
        combined = Modules.override(combined).with(loadModule(config.getQueryTransformerModule(), config));
        install(Modules.override(combined).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ServiceLayerExpansionService.class)
                        .toInstance(new FormGeneratorServiceLayerExpansionServiceImpl());
                bind(MapperLayerExpansionService.class)
                        .toInstance(new FormGeneratorMapperLayerExpansionServiceImpl());
                bind(Config.class).toInstance(config);
                if (config.getIsDataModelWithoutLombok()) {
                    bind(DataModelService.class).toInstance(new DataModelServiceNoLombokImpl());
                } else {
                    bind(DataModelService.class).toInstance(new DataModelServiceImpl());
                }
            }
        }));
    }

    private static Module loadModule(String moduleClassName, Config config) {
        try {
            log.info("load module: {}", moduleClassName);
            return (Module) Class.forName(moduleClassName).getConstructor(Config.class).newInstance(config);
        } catch (Exception e) {
            throw new Allison1875Exception("加载模块失败: " + moduleClassName, e);
        }
    }

}
