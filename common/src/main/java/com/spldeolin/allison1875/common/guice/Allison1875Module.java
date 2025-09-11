package com.spldeolin.allison1875.common.guice;

import com.google.inject.AbstractModule;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-12-10
 */
@Slf4j
public abstract class Allison1875Module extends AbstractModule {

    public abstract Class<? extends Allison1875MainService> declareMainService();

    @Override
    protected void configure() {
    }

}