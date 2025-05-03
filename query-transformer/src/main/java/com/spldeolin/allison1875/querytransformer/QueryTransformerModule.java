package com.spldeolin.allison1875.querytransformer;

import java.util.List;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.dto.InvalidDTO;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
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
    public List<InvalidDTO> validConfigs() {
        List<InvalidDTO> invalids = commonConfig.invalidSelf();
        invalids.addAll(queryTransformerConfig.invalidSelf());
        return invalids;
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