package com.spldeolin.allison1875.gadget;

import java.util.List;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
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
    public Class<? extends Allison1875MainService> declareMainService() {
        return LineCounter.class;
    }

    @Override
    public List<InvalidDto> validConfigs() {
        return lineCounterConfig.invalidSelf();
    }

    @Override
    protected void configure() {
        bind(LineCounterConfig.class).toInstance(lineCounterConfig);
    }

}