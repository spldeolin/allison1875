package com.example.service;

import com.example.design.TOrderDesign;
import java.util.List;

public class OrderService {

    /**
     * LEFT JOIN + 多 ON 条件（使用 open()/close() 触发多条件编码）
     */
    public void listByMultiOn() {
        TOrderDesign.select("listByMultiOn").leftJoin().TUserEntity.userName.email.on().open().id.eq(TOrderDesign.userId).createdAt.eq(TOrderDesign.createdAt).close().list();
    }
}