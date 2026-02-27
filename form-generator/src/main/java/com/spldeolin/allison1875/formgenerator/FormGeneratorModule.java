package com.spldeolin.allison1875.formgenerator;

import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
import com.spldeolin.allison1875.docanalyzer.config.DocAnalyzerConfig;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorServiceLayerExpansionServiceImpl;
import com.spldeolin.allison1875.handlertransformer.config.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerExpansionService;
import com.spldeolin.allison1875.persistencegenerator.config.PersistenceGeneratorConfig;
import lombok.ToString;

/**
 * @author Deolin 2023-05-05
 */
@ToString
public class FormGeneratorModule extends Allison1875Module {

    private final CommonConfig commonConfig;

    private final FormGeneratorConfig formGeneratorConfig;

    private final PersistenceGeneratorConfig persistenceGeneratorConfig;

    private final HandlerTransformerConfig handlerTransformerConfig;

    private final DocAnalyzerConfig docAnalyzerConfig;

    private final CompileFacade compileFacade;

    public FormGeneratorModule(CommonConfig commonConfig, FormGeneratorConfig formGeneratorConfig,
            PersistenceGeneratorConfig persistenceGeneratorConfig, HandlerTransformerConfig handlerTransformerConfig,
            DocAnalyzerConfig docAnalyzerConfig, CompileFacade compileFacade) {
        this.commonConfig = commonConfig;
        this.formGeneratorConfig = formGeneratorConfig;
        this.persistenceGeneratorConfig = persistenceGeneratorConfig;
        this.handlerTransformerConfig = handlerTransformerConfig;
        this.docAnalyzerConfig = docAnalyzerConfig;
        this.compileFacade = compileFacade;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return FormGenerator.class;
    }

    @Override
    protected void configure() {
        bind(ServiceLayerExpansionService.class).toInstance(new FormGeneratorServiceLayerExpansionServiceImpl());
        bind(CommonConfig.class).toInstance(commonConfig);
        bind(FormGeneratorConfig.class).toInstance(formGeneratorConfig);
        bind(PersistenceGeneratorConfig.class).toInstance(persistenceGeneratorConfig);
        bind(HandlerTransformerConfig.class).toInstance(handlerTransformerConfig);
        bind(DocAnalyzerConfig.class).toInstance(docAnalyzerConfig);
        bind(CompileFacade.class).toInstance(compileFacade);
        if (commonConfig.getIsDataModuleWithoutLombok()) {
            bind(DataModelService.class).toInstance(new DataModelServiceNoLombokImpl());
        } else {
            bind(DataModelService.class).toInstance(new DataModelServiceImpl());
        }
    }

}