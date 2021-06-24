package com.spldeolin.allison1875.base.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @author Deolin 2020-12-10
 */
public class GuiceUtils {

    private static final ThreadLocal<Injector> injectorContext = new ThreadLocal<>();

    private GuiceUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static Injector createInjector(Module... modules) {
        Injector injector = injectorContext.get();
        if (injector == null) {
            injector = Guice.createInjector(modules);
            injectorContext.set(injector);
        }
        return injector;
    }

    public static Injector getInjector() {
        return injectorContext.get();
    }

    public static <T> T getComponent(Class<T> type) {
        return getInjector().getInstance(type);
    }

}