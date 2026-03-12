package com.spldeolin.allison1875.persistencegenerator;

import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
import lombok.ToString;

/**
 * @author Deolin 2020-12-08
 */
@ToString
public class PersistenceGeneratorModule extends Allison1875Module {

    private final Config config;

    public PersistenceGeneratorModule(Config config) {
        this.config = config;
    }

    @Override
    public final Class<? extends Allison1875MainService> declareMainService() {
        return PersistenceGenerator.class;
    }

    @Override
    protected void configure() {
        bind(Config.class).toInstance(config);
        if (config.getIsDataModuleWithoutLombok()) {
            bind(DataModelService.class).toInstance(new DataModelServiceNoLombokImpl());
        } else {
            bind(DataModelService.class).toInstance(new DataModelServiceImpl());
        }
    }

}