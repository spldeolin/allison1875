package com.spldeolin.allison1875.gadget;

import java.util.Set;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.gadget.processor.BakFileCleaner;

/**
 * @author Deolin 2020-12-07
 */
public class BakFileCleanerModule extends Allison1875Module {

    @Override
    protected Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return BakFileCleaner.class;
    }

    @Override
    protected Set<Class<?>> provideConfigTypes() {
        return Sets.newHashSet();
    }

}