package com.spldeolin.allison1875.querytransformer;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.querytransformer.processor.QueryTransformer;

/**
 * @author Deolin 2020-12-09
 */
public class QueryTransformerModule extends Allison1875Module {

    {
        addSupportValidationType(QueryTransformerConfig.class);
    }

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(QueryTransformer.class);
    }

}