package com.spldeolin.allison1875.common.guice;

import java.lang.reflect.Method;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;

/**
 * @author Deolin 2025-09-11
 */
public class ValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        // 注册Singleton校验监听器
        bindListener(Matchers.any(), new ValidSingletonListener());

        // 注册方法切面，排除synthetic方法（如泛型擦除产生的bridge方法）
        bindInterceptor(Matchers.any(), new AbstractMatcher<Method>() {
            @Override
            public boolean matches(Method method) {
                return !method.isSynthetic();
            }
        }, new ValidMethodArgsInterceptor());
    }

}

