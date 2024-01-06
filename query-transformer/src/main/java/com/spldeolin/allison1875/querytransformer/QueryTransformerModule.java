package com.spldeolin.allison1875.querytransformer;

import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.util.ValidateUtils;
import lombok.ToString;

/**
 * @author Deolin 2020-12-09
 */
@ToString
public class QueryTransformerModule extends Allison1875Module {

    private final QueryTransformerConfig queryTransformerConfig;

    public QueryTransformerModule(QueryTransformerConfig queryTransformerConfig) {
        this.queryTransformerConfig = queryTransformerConfig;
    }

    @Override
    public Class<? extends Allison1875MainService> declareMainService() {
        return QueryTransformer.class;
    }

    @Override
    protected void configure() {
        ValidateUtils.ensureValid(queryTransformerConfig);
        bind(QueryTransformerConfig.class).toInstance(queryTransformerConfig);
    }

}