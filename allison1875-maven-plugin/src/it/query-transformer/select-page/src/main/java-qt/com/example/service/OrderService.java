package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import java.util.List;

public class OrderService {

    /**
     * 无条件分页查询
     */
    public void pageAll(Integer pageNo, Integer pageSize) {
        List<TOrderEntity> result = TOrderDesign.select("pageAll").page(pageNo, pageSize);
    }

    /**
     * 带条件的分页查询
     */
    public void pageByUserId(Long userId, Integer pageNo, Integer pageSize) {
        List<TOrderEntity> result = TOrderDesign.select("pageByUserId").where().userId.eq(userId).page(pageNo, pageSize);
    }
}
