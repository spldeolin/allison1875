package com.spldeolin.allison1875.appgenerator;

import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875Game;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import lombok.ToString;

/**
 * @author Deolin 2026-05-24
 */
@ToString
public class AppGeneratorModule extends Allison1875Module {

    private final Config config;

    public AppGeneratorModule(Config config) {
        this.config = config;
    }

    @Override
    public Class<? extends Allison1875Game> declareMainService() {
        return AppGenerator.class;
    }

    @Override
    protected void configure() {
        bind(Config.class).toInstance(config);
    }

}
