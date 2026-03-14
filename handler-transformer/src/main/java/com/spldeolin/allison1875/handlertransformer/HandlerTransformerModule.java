package com.spldeolin.allison1875.handlertransformer;

import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerExpansionService;
import com.spldeolin.allison1875.handlertransformer.service.impl.HandlerTransformerServiceLayerExpansionServiceImpl;
import lombok.ToString;

/**
 * @author Deolin 2020-12-07
 */
@ToString
public class HandlerTransformerModule extends Allison1875Module {

    private final Config config;

    public HandlerTransformerModule(Config config) {
        this.config = config;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return HandlerTransformer.class;
    }

    @Override
    protected void configure() {
        bind(Config.class).toInstance(config);
        bind(ServiceLayerExpansionService.class).toInstance(new HandlerTransformerServiceLayerExpansionServiceImpl());
        if (config.getIsDataModelWithoutLombok()) {
            bind(DataModelService.class).toInstance(new DataModelServiceNoLombokImpl());
        } else {
            bind(DataModelService.class).toInstance(new DataModelServiceImpl());
        }
    }

}