package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import java.util.List;

public class OrderService {

    /**
     * 软删除表 WHERE 自动加 is_deleted = 0
     */
    public void listAll() {
        TOrderDesign.select("listAll").list();
    }

    /**
     * WHERE 条件 + 软删除
     */
    public void listByStatus(Byte status) {
        List<TOrderEntity> result = TOrderDesign.select("listByStatus").where().status.eq(status).list();
    }
}