package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import java.math.BigDecimal;
import java.util.List;

public class OrderService {

    /**
     * 4个where条件，触发 ParamDTO 生成（阈值 > 3）
     */
    public void listByMultipleConditions(String orderNo, Long userId, Byte status, BigDecimal amount) {
        List<TOrderEntity> result = TOrderDesign.select("listByMultipleConditions").where().orderNo.eq(orderNo).userId.eq(userId).status.eq(status).amount.ge(amount).list();
    }
}
