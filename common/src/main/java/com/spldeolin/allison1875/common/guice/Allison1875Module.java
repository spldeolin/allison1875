package com.spldeolin.allison1875.common.guice;

import java.util.List;
import com.google.inject.AbstractModule;
import com.spldeolin.allison1875.common.dto.InvalidDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-12-10
 */
@Slf4j
public abstract class Allison1875Module extends AbstractModule {

    public abstract Class<? extends Allison1875MainService> declareMainService();

    public abstract List<InvalidDTO> validConfigs();

    @Override
    protected void configure() {
    }

}