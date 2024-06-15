package com.spldeolin.allison1875;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.google.common.base.MoreObjects;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-14
 */
@Mojo(name = "query-transformer", requiresDependencyResolution = ResolutionScope.TEST)
@Slf4j
public class QueryTransformerMojo extends Allison1875Mojo {

    @Parameter(defaultValue = "com.spldeolin.allison1875.querytransformer.QueryTransformerModule")
    private String queryTransformerModuleQualifier;

    @Parameter
    private QueryTransformerConfig queryTransformerConfig;

    @Override
    public Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader) throws Exception {
        queryTransformerConfig = MoreObjects.firstNonNull(queryTransformerConfig, new QueryTransformerConfig());
        log.info("queryTransformerConfig={}", JsonUtils.toJsonPrettily(queryTransformerConfig));

        return (Allison1875Module) classLoader.loadClass(queryTransformerModuleQualifier)
                .getConstructor(CommonConfig.class, QueryTransformerConfig.class)
                .newInstance(commonConfig, queryTransformerConfig);
    }

}
