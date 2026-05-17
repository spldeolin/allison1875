package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import java.math.BigDecimal;
import java.util.List;

public class OrderService {

    /**
     * page + 2 个 WHERE 条件触发 PAGE 降低阈值生成 ParamDTO
     */
    public void pageByStatusAndAmount(Byte status, BigDecimal minAmount, Integer pageNo, Integer pageSize) {
        List<TOrderEntity> result = TOrderDesign.select("pageByStatusAndAmount").where().status.eq(status).amount.ge(minAmount).page(pageNo, pageSize);
    }
}