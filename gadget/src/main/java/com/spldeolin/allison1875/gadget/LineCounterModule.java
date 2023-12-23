package com.spldeolin.allison1875.gadget;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.gadget.processor.LineCounter;
import lombok.ToString;

/**
 * @author Deolin 2020-12-07
 */
@ToString
public class LineCounterModule extends Allison1875Module {

    private final LineCounterConfig lineCounterConfig;

    public LineCounterModule(LineCounterConfig lineCounterConfig) {
        this.lineCounterConfig = lineCounterConfig;
    }

    @Override
    protected void configure() {
        ValidateUtils.ensureValid(lineCounterConfig);
        bind(LineCounterConfig.class).toInstance(lineCounterConfig);
    }

    @Override
    public Class<? extends Allison1875MainService> provideMainProcessorType() {
        return LineCounter.class;
    }

}