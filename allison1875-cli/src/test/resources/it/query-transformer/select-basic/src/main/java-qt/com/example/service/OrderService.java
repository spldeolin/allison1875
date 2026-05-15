package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import java.util.List;

public class OrderService {

    public void findById(Long id) {
        TOrderEntity order = TOrderDesign.select("findById").where().id.eq(id).one();
    }

    public void listAll() {
        List<TOrderEntity> orders = TOrderDesign.select("listAll").list();
    }

    public void countByUserId(Long userId) {
        int count = TOrderDesign.select("countByUserId").where().userId.eq(userId).count();
    }
}
