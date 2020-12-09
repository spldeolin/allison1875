package com.spldeolin.allison1875.gadget;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.Allison1875;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.gadget.processor.LineCounter;

/**
 * @author Deolin 2020-12-07
 */
public class LineCounterModule extends Allison1875.Module {

    protected final LineCounterConfig lineCounterConfig;

    public LineCounterModule(LineCounterConfig lineCounterConfig) {
        this.lineCounterConfig = super.ensureValid(lineCounterConfig);
    }

    @Override
    protected void configure() {
        bind(LineCounterConfig.class).toInstance(lineCounterConfig);
    }

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(LineCounter.class);
    }

}