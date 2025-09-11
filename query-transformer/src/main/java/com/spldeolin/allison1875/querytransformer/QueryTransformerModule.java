package com.spldeolin.allison1875.querytransformer;

import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
import com.spldeolin.allison1875.querytransformer.config.QueryTransformerConfig;
import lombok.ToString;

/**
 * @author Deolin 2020-12-09
 */
@ToString
public class QueryTransformerModule extends Allison1875Module {

    private final CommonConfig commonConfig;

    private final QueryTransformerConfig queryTransformerConfig;

    public QueryTransformerModule(CommonConfig commonConfig, QueryTransformerConfig queryTransformerConfig) {
        super();
        this.commonConfig = commonConfig;
        this.queryTransformerConfig = queryTransformerConfig;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return QueryTransformer.class;
    }

    @Override
    protected void configure() {
        bind(CommonConfig.class).toInstance(commonConfig);
        bind(QueryTransformerConfig.class).toInstance(queryTransformerConfig);
        if (commonConfig.getIsDataModuleWithoutLombok()) {
            bind(DataModelService.class).toInstance(new DataModelServiceNoLombokImpl());
        } else {
            bind(DataModelService.class).toInstance(new DataModelServiceImpl());
        }
    }

}