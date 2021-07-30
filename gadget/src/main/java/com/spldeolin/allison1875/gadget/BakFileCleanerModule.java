package com.spldeolin.allison1875.gadget;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.gadget.processor.BakFileCleaner;
import lombok.ToString;

/**
 * @author Deolin 2020-12-07
 */
@ToString
public class BakFileCleanerModule extends Allison1875Module {

    @Override
    protected void configure() {
    }

    @Override
    public Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return BakFileCleaner.class;
    }

}