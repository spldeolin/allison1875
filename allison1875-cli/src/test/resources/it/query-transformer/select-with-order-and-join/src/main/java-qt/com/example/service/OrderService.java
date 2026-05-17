package com.example.service;

import com.example.design.TOrderDesign;
import java.util.List;

public class OrderService {

    /**
     * JOIN + select 属性 + ORDER BY 组合
     */
    public void listOrderWithUserOrderById() {
        TOrderDesign.select("listOrderWithUserOrderById").orderNo.amount.leftJoin().TUserEntity.userName.on().id.eq(TOrderDesign.userId).order().id.asc().list();
    }
}