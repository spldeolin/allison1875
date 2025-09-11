package com.spldeolin.allison1875.common.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * @author Deolin 2025-09-11
 */
public class ValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        // 注册Singleton校验监听器
        bindListener(Matchers.any(), new ValidSingletonListener());

        // 注册方法切面
        bindInterceptor(Matchers.any(), Matchers.any(), new ValidMethodArgsInterceptor());
    }

}

