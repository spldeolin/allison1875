package com.spldeolin.allison1875.htex;

import java.util.Set;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.htex.handle.CreateHandlerHandle;
import com.spldeolin.allison1875.htex.handle.CreateServiceMethodHandle;
import com.spldeolin.allison1875.htex.handle.DefaultCreateHandlerHandle;
import com.spldeolin.allison1875.htex.handle.DefaultCreateServiceMethodHandle;
import com.spldeolin.allison1875.htex.processor.HandlerTransformer;

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
    }

}