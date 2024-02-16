package com.spldeolin.allison1875.querytransformer;

import java.util.List;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
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
    public final Class<? extends Allison1875MainService> declareMainService() {
        return QueryTransformer.class;
    }

    @Override
    public List<InvalidDto> validConfigs() {
        return queryTransformerConfig.invalidSelf();
    }

    @Override
    protected void configure() {
        bind(QueryTransformerConfig.class).toInstance(queryTransformerConfig);
    }

}