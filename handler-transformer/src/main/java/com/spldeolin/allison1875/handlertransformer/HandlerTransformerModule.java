package com.spldeolin.allison1875.handlertransformer;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.util.ValidateUtils;
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
    public Class<? extends Allison1875MainService> provideMainProcessorType() {
        return HandlerTransformer.class;
    }

    @Override
    protected void configure() {
        ValidateUtils.ensureValid(handlerTransformerConfig);
        bind(HandlerTransformerConfig.class).toInstance(handlerTransformerConfig);
    }

}