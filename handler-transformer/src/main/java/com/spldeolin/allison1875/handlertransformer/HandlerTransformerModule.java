package com.spldeolin.allison1875.handlertransformer;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.handlertransformer.processor.HandlerTransformer;

/**
 * @author Deolin 2020-12-07
 */
public class HandlerTransformerModule extends Allison1875Module {

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(HandlerTransformer.class);
    }

}