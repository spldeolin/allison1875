package com.example.service;

import com.example.design.TOrderDesign;
import java.util.Map;

public class OrderService {

    /**
     * 单属性 + mapBy 终结 → Map<KeyType, SinglePropType>
     */
    public void mapByOrderNo() {
        TOrderDesign.select("mapByOrderNo").orderNo.mapByUserId();
    }
}