package com.example.service;

import com.example.design.TOrderDesign;
import java.util.List;

public class OrderService {

    /**
     * DELETE + IN 运算符 → <delete> 标签无 parameterType
     */
    public void deleteByIdIn(List<Long> ids) {
        TOrderDesign.delete("deleteByIdIn").where().id.in(ids).over();
    }
}