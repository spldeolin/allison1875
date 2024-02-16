package com.spldeolin.allison1875.startransformer;

import java.util.List;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
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
    public final Class<? extends Allison1875MainService> declareMainService() {
        return StarTransformer.class;
    }

    @Override
    public List<InvalidDto> validConfigs() {
        return starTransformerConfig.invalidSelf();
    }

    @Override
    protected void configure() {
        bind(StarTransformerConfig.class).toInstance(starTransformerConfig);
    }

}