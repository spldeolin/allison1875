package com.spldeolin.allison1875.persistencegenerator;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.persistencegenerator.handle.DefaultGenerateEntityFieldHandle;
import com.spldeolin.allison1875.persistencegenerator.handle.DefaultGenerateQueryDesignFieldHandle;
import com.spldeolin.allison1875.persistencegenerator.handle.GenerateEntityFieldHandle;
import com.spldeolin.allison1875.persistencegenerator.handle.GenerateQueryDesignFieldHandle;
import com.spldeolin.allison1875.persistencegenerator.processor.PersistenceGenerator;

/**
 * @author Deolin 2020-12-08
 */
public class PersistenceGeneratorModule extends Allison1875Module {

    @Override
    protected void configure() {
        bind(GenerateEntityFieldHandle.class).to(DefaultGenerateEntityFieldHandle.class);
        bind(GenerateQueryDesignFieldHandle.class).to(DefaultGenerateQueryDesignFieldHandle.class);
    }

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(PersistenceGenerator.class);
    }

}