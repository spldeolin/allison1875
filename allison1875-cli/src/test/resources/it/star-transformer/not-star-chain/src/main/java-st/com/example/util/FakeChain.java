package com.example.util;

/**
 * 假链辅助类，用于验证 star-transformer 不会误转换非 StarSchema 根的 {@code .over()} 调用。
 *
 * <p>该类模拟一个"看起来像链式调用，但并非 StarSchema"的场景，验证 detectStarChains 能正确过滤。
 *
 * @author test-author 2026-05-20
 */
public class FakeChain {

    /** 静态实例，用于测试 FieldAccessExpr scope → finalNameExprRecursively 返回 false 的分支 */
    public static final FakeChain INSTANCE = new FakeChain();

    public FakeChain foo() {
        return this;
    }

    public void over() {
    }

}
