package com.spldeolin.allison1875.handlertransformer;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.Allison1875;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.handlertransformer.processor.HandlerTransformer;

/**
 * @author Deolin 2020-12-07
 */
public class HandlerTransformerModule extends Allison1875.Module {

    protected final HandlerTransformerConfig handlerTransformerConfig;

    public HandlerTransformerModule(HandlerTransformerConfig handlerTransformerConfig) {
        this.handlerTransformerConfig = super.ensureValid(handlerTransformerConfig);
    }

    @Override
    protected void configure() {
        bind(HandlerTransformerConfig.class).toInstance(handlerTransformerConfig);
    }

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(HandlerTransformer.class);
    }

}