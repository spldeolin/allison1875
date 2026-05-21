package com.example.service;

import com.example.design.TOrderDesign;
import com.spldeolin.allison1875.support.StarSchema;

/**
 * 订单装配服务 - 包含星型链DSL，用于star-transformer集成测试。
 *
 * @author test-author 2026-05-20
 */
public class OrderAssembleService {

    public void assemble(Long orderId) {
        StarSchema.cft(TOrderDesign.id, orderId).over();
    }

}
