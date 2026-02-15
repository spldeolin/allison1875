package com.spldeolin.allison1875.formgenerator;

import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
import com.spldeolin.allison1875.handlertransformer.config.HandlerTransformerConfig;
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

    public FormGeneratorModule(CommonConfig commonConfig, FormGeneratorConfig formGeneratorConfig,
            PersistenceGeneratorConfig persistenceGeneratorConfig, HandlerTransformerConfig handlerTransformerConfig) {
        this.commonConfig = commonConfig;
        this.formGeneratorConfig = formGeneratorConfig;
        this.persistenceGeneratorConfig = persistenceGeneratorConfig;
        this.handlerTransformerConfig = handlerTransformerConfig;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return FormGenerator.class;
    }

    @Override
    protected void configure() {
        bind(CommonConfig.class).toInstance(commonConfig);
        bind(FormGeneratorConfig.class).toInstance(formGeneratorConfig);
        bind(PersistenceGeneratorConfig.class).toInstance(persistenceGeneratorConfig);
        bind(HandlerTransformerConfig.class).toInstance(handlerTransformerConfig);
        if (commonConfig.getIsDataModuleWithoutLombok()) {
            bind(DataModelService.class).toInstance(new DataModelServiceNoLombokImpl());
        } else {
            bind(DataModelService.class).toInstance(new DataModelServiceImpl());
        }

        // 绑定 ItemService<ItemDef> 到 PrimaryItemServiceImpl（分发器）
//        bind(new com.google.inject.TypeLiteral<ItemService<ItemDef>>() {
//        }).to(PrimaryItemServiceImpl.class);
    }

}