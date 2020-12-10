package com.spldeolin.allison1875.base.ancestor;

import java.util.Set;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-10
 */
@Log4j2
public abstract class Allison1875Module extends AbstractModule {

    public abstract Allison1875MainProcessor getMainProcessor(Injector injector);

    @Getter
    private final Set<Class<?>> supportValidationTypes = Sets.newHashSet(BaseConfig.class);

    protected void addSupportValidationType(Class<?> type) {
        supportValidationTypes.add(type);
    }

}