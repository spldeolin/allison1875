package com.spldeolin.allison1875.gadget;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.gadget.processor.LineCounter;

/**
 * @author Deolin 2020-12-07
 */
public class LineCounterModule extends Allison1875Module {

    {
        addSupportValidationType(LineCounterConfig.class);
    }

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(LineCounter.class);
    }

}