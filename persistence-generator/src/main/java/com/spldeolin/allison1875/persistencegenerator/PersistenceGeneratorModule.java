package com.spldeolin.allison1875.persistencegenerator;

import java.util.Set;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.persistencegenerator.processor.PersistenceGenerator;

/**
 * @author Deolin 2020-12-08
 */
public class PersistenceGeneratorModule extends Allison1875Module {

    @Override
    protected void configure() {
    }

    @Override
    protected Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return PersistenceGenerator.class;
    }

    @Override
    protected Set<Class<?>> provideConfigTypes() {
        return Sets.newHashSet(PersistenceGeneratorConfig.class);
    }

}