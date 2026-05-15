package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import java.util.List;

public class OrderService {

    /**
     * 单字段 ASC 排序
     */
    public void listOrderByCreatedAtAsc() {
        List<TOrderEntity> result = TOrderDesign.select("listOrderByCreatedAtAsc").order().createdAt.asc().list();
    }

    /**
     * 单字段 DESC 排序
     */
    public void listOrderByAmountDesc() {
        List<TOrderEntity> result = TOrderDesign.select("listOrderByAmountDesc").order().amount.desc().list();
    }

    /**
     * 多字段排序（status ASC + createdAt DESC）
     */
    public void listOrderByMultiFields() {
        List<TOrderEntity> result = TOrderDesign.select("listOrderByMultiFields").order().status.asc().createdAt.desc().list();
    }

    /**
     * WHERE + ORDER BY 组合
     */
    public void listByUserIdOrderByCreatedAt(Long userId) {
        List<TOrderEntity> result = TOrderDesign.select("listByUserIdOrderByCreatedAt").where().userId.eq(userId).order().createdAt.desc().list();
    }
}
