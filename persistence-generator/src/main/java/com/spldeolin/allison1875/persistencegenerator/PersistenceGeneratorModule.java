package com.spldeolin.allison1875.persistencegenerator;

import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.util.ValidUtils;
import lombok.ToString;

/**
 * @author Deolin 2020-12-08
 */
@ToString
public class PersistenceGeneratorModule extends Allison1875Module {

    private final PersistenceGeneratorConfig persistenceGeneratorConfig;

    public PersistenceGeneratorModule(PersistenceGeneratorConfig persistenceGeneratorConfig) {
        this.persistenceGeneratorConfig = persistenceGeneratorConfig;
    }

    @Override
    protected void configure() {
        ValidUtils.ensureValid(persistenceGeneratorConfig);
        bind(PersistenceGeneratorConfig.class).toInstance(persistenceGeneratorConfig);
    }

    @Override
    public Class<? extends Allison1875MainService> declareMainService() {
        return PersistenceGenerator.class;
    }

}