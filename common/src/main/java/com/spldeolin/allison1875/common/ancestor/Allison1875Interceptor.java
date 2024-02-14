package com.spldeolin.allison1875.common.ancestor;

import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import com.google.inject.Module;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;

/**
 * @author Deolin 2024-02-14
 */
public abstract class Allison1875Interceptor implements MethodInterceptor {

    public Matcher<? super Class<?>> classMatcher() {
        return Matchers.any();
    }

    public Matcher<? super Method> methodMatcher() {
        return Matchers.any();
    }

    public Module toGuiceModule() {
        return binder -> binder.bindInterceptor(classMatcher(), methodMatcher(), this);
    }

}