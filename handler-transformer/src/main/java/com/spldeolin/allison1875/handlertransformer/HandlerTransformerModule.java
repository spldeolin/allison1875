package com.spldeolin.allison1875.handlertransformer;

import java.util.List;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import lombok.ToString;

/**
 * @author Deolin 2020-12-07
 */
@ToString
public class HandlerTransformerModule extends Allison1875Module {

    private final HandlerTransformerConfig handlerTransformerConfig;

    public HandlerTransformerModule(HandlerTransformerConfig handlerTransformerConfig) {
        this.handlerTransformerConfig = handlerTransformerConfig;
    }

    @Override
    public Class<? extends Allison1875MainService> declareMainService() {
        return HandlerTransformer.class;
    }

    @Override
    public List<InvalidDto> validConfigs() {
        return handlerTransformerConfig.invalidSelf();
    }

    @Override
    protected void configure() {
        bind(HandlerTransformerConfig.class).toInstance(handlerTransformerConfig);
    }

}