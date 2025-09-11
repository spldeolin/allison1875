package com.spldeolin.allison1875.handlertransformer;

import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
import com.spldeolin.allison1875.handlertransformer.config.HandlerTransformerConfig;
import lombok.ToString;

/**
 * @author Deolin 2020-12-07
 */
@ToString
public class HandlerTransformerModule extends Allison1875Module {

    private final CommonConfig commonConfig;

    private final HandlerTransformerConfig handlerTransformerConfig;

    public HandlerTransformerModule(CommonConfig commonConfig, HandlerTransformerConfig handlerTransformerConfig) {
        this.commonConfig = commonConfig;
        this.handlerTransformerConfig = handlerTransformerConfig;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return HandlerTransformer.class;
    }

    @Override
    protected void configure() {
        bind(CommonConfig.class).toInstance(commonConfig);
        bind(HandlerTransformerConfig.class).toInstance(handlerTransformerConfig);
        if (commonConfig.getIsDataModuleWithoutLombok()) {
            bind(DataModelService.class).toInstance(new DataModelServiceNoLombokImpl());
        } else {
            bind(DataModelService.class).toInstance(new DataModelServiceImpl());
        }
    }

}