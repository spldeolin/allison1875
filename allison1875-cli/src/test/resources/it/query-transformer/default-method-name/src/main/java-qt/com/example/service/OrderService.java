package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import java.util.List;

public class OrderService {

    /**
     * 无参 select() → 默认方法名 queryTOrder
     */
    public void defaultSelect() {
        TOrderDesign.select().orderNo.list();
    }

    /**
     * 无参 update() → 默认方法名 updateTOrder
     */
    public void defaultUpdate(String orderNo, Long id) {
        TOrderDesign.update().orderNo(orderNo).where().id.eq(id).over();
    }

    /**
     * 无参 delete() → 默认方法名 deleteTOrder
     */
    public void defaultDelete(Long id) {
        TOrderDesign.delete().where().id.eq(id).over();
    }
}