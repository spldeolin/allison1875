package com.spldeolin.allison1875.persistencegenerator;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.Allison1875;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.persistencegenerator.handle.DefaultGenerateEntityFieldHandle;
import com.spldeolin.allison1875.persistencegenerator.handle.DefaultGenerateQueryDesignFieldHandle;
import com.spldeolin.allison1875.persistencegenerator.handle.GenerateEntityFieldHandle;
import com.spldeolin.allison1875.persistencegenerator.handle.GenerateQueryDesignFieldHandle;
import com.spldeolin.allison1875.persistencegenerator.processor.PersistenceGenerator;

/**
 * @author Deolin 2020-12-08
 */
public class PersistenceGeneratorModule extends Allison1875.Module {

    private final PersistenceGeneratorConfig persistenceGeneratorConfig;

    public PersistenceGeneratorModule(PersistenceGeneratorConfig persistenceGeneratorConfig) {
        this.persistenceGeneratorConfig = super.ensureValid(persistenceGeneratorConfig);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(GenerateEntityFieldHandle.class).to(DefaultGenerateEntityFieldHandle.class);
        bind(GenerateQueryDesignFieldHandle.class).to(DefaultGenerateQueryDesignFieldHandle.class);
        bind(PersistenceGeneratorConfig.class).toInstance(persistenceGeneratorConfig);
    }

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(PersistenceGenerator.class);
    }

}