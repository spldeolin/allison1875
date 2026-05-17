package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import java.util.List;

public class OrderService {

    /**
     * 赋值链：DSL 赋值给变量触发 isAssigned=true
     */
    public void listAssigned(Byte status) {
        List<TOrderEntity> result = TOrderDesign.select("listAssigned").where().status.eq(status).list();
    }
}