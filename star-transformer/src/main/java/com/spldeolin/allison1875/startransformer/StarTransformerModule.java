package com.spldeolin.allison1875.startransformer;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.startransformer.processor.StarTransformer;
import lombok.ToString;

/**
 * @author Deolin 2023-05-05
 */
@ToString
public class StarTransformerModule extends Allison1875Module {

    private final StarTransformerConfig starTransformerConfig;

    public StarTransformerModule(StarTransformerConfig starTransformerConfig) {
        this.starTransformerConfig = starTransformerConfig;
    }

    @Override
    public Class<? extends Allison1875MainService> provideMainProcessorType() {
        return StarTransformer.class;
    }

    @Override
    protected void configure() {
        ValidateUtils.ensureValid(starTransformerConfig);
        bind(StarTransformerConfig.class).toInstance(starTransformerConfig);
    }

}