package com.spldeolin.allison1875.common.ast;

import com.google.common.base.Preconditions;

/**
 * @author Deolin 2024-11-24
 */
public class AstForestContext {

    private static final ThreadLocal<AstForest> ctx = new ThreadLocal<>();

    public static void set(AstForest astForest) {
        Preconditions.checkNotNull(astForest);
        ctx.set(astForest);
    }

    public static AstForest get() {
        return ctx.get();
    }

    public static boolean isEmpty() {
        return ctx.get() == null;
    }

    public static void remove() {
        ctx.remove();
    }

}
