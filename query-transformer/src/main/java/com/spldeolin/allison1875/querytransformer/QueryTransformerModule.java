package com.spldeolin.allison1875.querytransformer;

import java.util.Set;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.querytransformer.processor.QueryTransformer;

/**
 * @author Deolin 2020-12-09
 */
public class QueryTransformerModule extends Allison1875Module {

    @Override
    protected Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return QueryTransformer.class;
    }

    @Override
    protected Set<Class<?>> provideConfigTypes() {
        return Sets.newHashSet(QueryTransformerConfig.class);
    }

}