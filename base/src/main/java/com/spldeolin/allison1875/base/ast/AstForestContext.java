package com.spldeolin.allison1875.base.ast;

/**
 * @author Deolin 2020-08-29
 */
public class AstForestContext {

    private static final ThreadLocal<AstForest> context = new ThreadLocal<>();

    public static void setCurrent(AstForest astForset) {
        if (context.get() != null) {
            throw new IllegalStateException("已在当前上下文中设置了AstForest，无法覆盖");
        }
        context.set(astForset);
    }

    public static AstForest getCurrent() {
        AstForest astForest = context.get();
        if (astForest == null) {
            throw new IllegalStateException("未在当前上下文中设置AstForest");
        }
        return astForest;
    }

}