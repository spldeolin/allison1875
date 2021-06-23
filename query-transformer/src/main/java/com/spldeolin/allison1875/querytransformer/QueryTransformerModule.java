package com.spldeolin.allison1875.querytransformer;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.querytransformer.processor.QueryTransformer;
import lombok.ToString;

/**
 * @author Deolin 2020-12-09
 */
@ToString
public final class QueryTransformerModule extends Allison1875Module {

    private final QueryTransformerConfig queryTransformerConfig;

    public QueryTransformerModule(QueryTransformerConfig queryTransformerConfig) {
        this.queryTransformerConfig = queryTransformerConfig;
    }

    @Override
    public Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return QueryTransformer.class;
    }

    @Override
    protected void configure() {
        ValidateUtils.ensureValid(queryTransformerConfig);
        bind(QueryTransformerConfig.class).toInstance(queryTransformerConfig);
    }

}