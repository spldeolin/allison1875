package com.spldeolin.allison1875.gadget;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.Allison1875;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.gadget.processor.BakFileCleaner;

/**
 * @author Deolin 2020-12-07
 */
public class BakFileCleanerModule extends Allison1875.Module {

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(BakFileCleaner.class);
    }

}