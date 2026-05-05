package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import java.util.List;
import java.util.Map;

public class OrderService {

    /**
     * mapByUserId：按用户ID分组为Map
     */
    public void mapByUserId() {
        Map<Long, TOrderEntity> result = TOrderDesign.select("mapByUserId").mapByUserId();
    }

    /**
     * groupByStatus：按订单状态分组为Map<Key, List>
     */
    public void groupByStatus() {
        Map<Byte, List<TOrderEntity>> result = TOrderDesign.select("groupByStatus").groupByStatus();
    }

    /**
     * mapByUserId + where条件
     */
    public void mapByUserIdWithCondition(Byte status) {
        Map<Long, TOrderEntity> result = TOrderDesign.select("mapByUserIdWithCondition").where().status.eq(status).mapByUserId();
    }
}
