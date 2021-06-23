package com.spldeolin.allison1875.base.ancestor;

import com.google.inject.AbstractModule;
import lombok.ToString;

/**
 * @author Deolin 2020-12-10
 */
@ToString
public abstract class Allison1875Module extends AbstractModule {

    public abstract Class<? extends Allison1875MainProcessor> provideMainProcessorType();

    @Override
    protected void configure() {
    }

}