package com.spldeolin.allison1875.handlertransformer;

import java.util.Set;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.handlertransformer.handle.CreateHandlerHandle;
import com.spldeolin.allison1875.handlertransformer.handle.CreateServiceMethodHandle;
import com.spldeolin.allison1875.handlertransformer.handle.DefaultCreateHandlerHandle;
import com.spldeolin.allison1875.handlertransformer.handle.DefaultCreateServiceMethodHandle;
import com.spldeolin.allison1875.handlertransformer.handle.DefaultFieldHandle;
import com.spldeolin.allison1875.handlertransformer.handle.FieldHandle;
import com.spldeolin.allison1875.handlertransformer.processor.HandlerTransformer;

/**
 * @author Deolin 2020-12-07
 */
public class HandlerTransformerModule extends Allison1875Module {

    @Override
    protected Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return HandlerTransformer.class;
    }

    @Override
    protected Set<Class<?>> provideConfigTypes() {
        return Sets.newHashSet(HandlerTransformerConfig.class);
    }

    @Override
    protected void configure() {
        bind(CreateServiceMethodHandle.class).toInstance(new DefaultCreateServiceMethodHandle());
        bind(CreateHandlerHandle.class).toInstance(new DefaultCreateHandlerHandle());
        bind(FieldHandle.class).toInstance(new DefaultFieldHandle());
    }

}