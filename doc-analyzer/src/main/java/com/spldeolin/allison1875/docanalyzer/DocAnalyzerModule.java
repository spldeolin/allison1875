package com.spldeolin.allison1875.docanalyzer;

import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875Game;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-12-06
 */
@Slf4j
public class DocAnalyzerModule extends Allison1875Module {

    private final Config config;

    public DocAnalyzerModule(Config config) {
        this.config = config;
    }

    @Override
    public final Class<? extends Allison1875Game> declareMainService() {
        return DocAnalyzer.class;
    }

    @Override
    protected void configure() {
        bind(Config.class).toInstance(config);
        if (config.getIsDataModelWithoutLombok()) {
            bind(DataModelService.class).toInstance(new DataModelServiceNoLombokImpl());
        } else {
            bind(DataModelService.class).toInstance(new DataModelServiceImpl());
        }
    }

}