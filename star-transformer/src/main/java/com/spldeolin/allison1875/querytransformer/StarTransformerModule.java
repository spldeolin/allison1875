package com.spldeolin.allison1875.querytransformer;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.querytransformer.processor.StarTransformer;
import lombok.ToString;

/**
 * @author Deolin 2023-05-05
 */
@ToString
public class StarTransformerModule extends Allison1875Module {

    private final StarTransformerConfig queryTransformerConfig;

    public StarTransformerModule(StarTransformerConfig queryTransformerConfig) {
        this.queryTransformerConfig = queryTransformerConfig;
    }

    @Override
    public Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return StarTransformer.class;
    }

    @Override
    protected void configure() {
        ValidateUtils.ensureValid(queryTransformerConfig);
        bind(StarTransformerConfig.class).toInstance(queryTransformerConfig);
    }

}