package com.example.service;

import com.example.design.TOrderDesign;
import com.example.util.FakeChain;
import com.spldeolin.allison1875.support.StarSchema;

/**
 * 订单装配服务 - 含多种 .over() 调用，验证 star-transformer 的正负向分支。
 *
 * <p>同方法三条语句：
 * <ol>
 *   <li>FakeChain.INSTANCE.foo().over() — FieldAccessExpr scope → 保留不动</li>
 *   <li>fakeChain.foo().over() — NameExpr scope 非 StarSchema → 保留不动</li>
 *   <li>StarSchema cft over 链 — 正常星型链 → 展开为查询+装配代码</li>
 * </ol>
 *
 * @author test-author 2026-05-20
 */
public class OrderAssembleService {

    public void assemble(Long orderId) {
        FakeChain fakeChain = new FakeChain();

        // 语句1: FieldAccessExpr scope → 不应被 star-transformer 改写
        FakeChain.INSTANCE.foo().over();

        // 语句2: NameExpr scope 解析为非 StarSchema 类型 → 不应被改写
        fakeChain.foo().over();

        // 语句3: 正常 StarSchema 星型链 → 应被展开转换
        StarSchema.cft(TOrderDesign.id, orderId).over();
    }

}
