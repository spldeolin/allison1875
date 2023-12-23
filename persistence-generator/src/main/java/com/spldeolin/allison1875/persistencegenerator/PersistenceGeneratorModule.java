package com.spldeolin.allison1875.persistencegenerator;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.persistencegenerator.processor.PersistenceGenerator;
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
        ValidateUtils.ensureValid(persistenceGeneratorConfig);
        bind(PersistenceGeneratorConfig.class).toInstance(persistenceGeneratorConfig);
    }

    @Override
    public Class<? extends Allison1875MainService> provideMainProcessorType() {
        return PersistenceGenerator.class;
    }

}