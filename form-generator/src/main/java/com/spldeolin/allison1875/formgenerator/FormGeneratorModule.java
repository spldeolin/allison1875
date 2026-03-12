package com.spldeolin.allison1875.formgenerator;

import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorServiceLayerExpansionServiceImpl;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerExpansionService;
import lombok.ToString;

/**
 * @author Deolin 2023-05-05
 */
@ToString
public class FormGeneratorModule extends Allison1875Module {

    private final Config config;

    private final CompileFacade compileFacade;

    public FormGeneratorModule(Config config, CompileFacade compileFacade) {
        this.config = config;
        this.compileFacade = compileFacade;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return FormGenerator.class;
    }

    @Override
    protected void configure() {
        bind(ServiceLayerExpansionService.class).toInstance(new FormGeneratorServiceLayerExpansionServiceImpl());
        bind(Config.class).toInstance(config);
        bind(CompileFacade.class).toInstance(compileFacade);
        if (config.getIsDataModuleWithoutLombok()) {
            bind(DataModelService.class).toInstance(new DataModelServiceNoLombokImpl());
        } else {
            bind(DataModelService.class).toInstance(new DataModelServiceImpl());
        }
    }

}