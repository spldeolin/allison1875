package com.spldeolin.allison1875.common.config;

import com.google.common.base.Preconditions;

/**
 * 当前正在处理的业务领域配置的ThreadLocal上下文
 *
 * @author Deolin 2026-04-28
 */
public class DomainContext {

    private static final ThreadLocal<DomainConfig> ctx = new ThreadLocal<>();

    public static void set(DomainConfig domainConfig) {
        Preconditions.checkNotNull(domainConfig);
        ctx.set(domainConfig);
    }

    public static DomainConfig get() {
        return ctx.get();
    }

    public static void remove() {
        ctx.remove();
    }

}
