package com.spldeolin.allison1875;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.google.common.base.MoreObjects;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-14
 */
@Mojo(name = "handler-transformer", requiresDependencyResolution = ResolutionScope.TEST)
@Slf4j
public class HandlerTransformerMojo extends Allison1875Mojo {

    @Parameter(defaultValue = "com.spldeolin.allison1875.handlertransformer.HandlerTransformerModule")
    private String handlerTransformerModuleQualifier;

    @Parameter
    private HandlerTransformerConfig handlerTransformerConfig;

    @Override
    public Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader) throws Exception {
        handlerTransformerConfig = MoreObjects.firstNonNull(handlerTransformerConfig, new HandlerTransformerConfig());
        handlerTransformerConfig.setCommonConfig(commonConfig);
        log.info("handlerTransformerConfig={}", JsonUtils.toJsonPrettily(handlerTransformerConfig));

        return (Allison1875Module) classLoader.loadClass(handlerTransformerModuleQualifier)
                .getConstructor(HandlerTransformerConfig.class).newInstance(handlerTransformerConfig);
    }

}
