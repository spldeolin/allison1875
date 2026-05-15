package com.example.service;

import com.example.design.TOrderDesign;
import java.util.List;

public class OrderService {

    /**
     * 指定单个属性：只查 orderNo
     */
    public void listOrderNos() {
        TOrderDesign.select("listOrderNos").orderNo.list();
    }

    /**
     * 指定多个属性：查 orderNo + userId + amount
     */
    public void listOrderSummaries() {
        TOrderDesign.select("listOrderSummaries").orderNo.userId.amount.list();
    }

    /**
     * 指定多个属性 + where 条件
     */
    public void listOrderSummariesByStatus(Byte status) {
        TOrderDesign.select("listOrderSummariesByStatus").orderNo.userId.amount.where().status.eq(status).list();
    }
}
