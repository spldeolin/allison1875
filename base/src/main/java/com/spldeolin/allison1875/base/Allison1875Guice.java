package com.spldeolin.allison1875.base;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;

/**
 * @author Deolin 2020-12-06
 */
public class Allison1875Guice {

    public static void launch(Class<?> primaryClass, Module guiceModule) {
        Injector injector = Guice.createInjector(guiceModule);
        Allison1875MainProcessor<?, ?> mainProcessor = guiceModule.getMainProcessor(injector);

        AstForest astForest = new AstForest(primaryClass);
        mainProcessor.process(astForest);
    }

    public static abstract class Module extends AbstractModule {

        public abstract Allison1875MainProcessor<?, ?> getMainProcessor(Injector injector);

    }

}