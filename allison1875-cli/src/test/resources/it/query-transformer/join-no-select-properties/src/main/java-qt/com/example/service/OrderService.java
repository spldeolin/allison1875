package com.example.service;

import com.example.design.TOrderDesign;
import java.util.List;

public class OrderService {

    /**
     * JOIN + 不指定主表 select 属性 → 全列表 + t1. 前缀
     */
    public void listAllColumns() {
        TOrderDesign.select("listAllColumns").leftJoin().TUserEntity.userName.on().id.eq(TOrderDesign.userId).list();
    }
}