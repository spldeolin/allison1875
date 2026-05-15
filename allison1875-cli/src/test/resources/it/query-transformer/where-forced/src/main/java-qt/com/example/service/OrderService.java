package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import java.util.List;

public class OrderService {

    /**
     * whereEvenNull + eq：强制条件，不包裹 <if test>
     */
    public void findByIdForced(Long id) {
        TOrderEntity result = TOrderDesign.select("findByIdForced").whereEvenNull().id.eq(id).one();
    }

    /**
     * whereEvenNull + 多条件组合
     */
    public void listByStatusAndUserIdForced(Byte status, Long userId) {
        List<TOrderEntity> result = TOrderDesign.select("listByStatusAndUserIdForced").whereEvenNull().status.eq(status).userId.eq(userId).list();
    }

    /**
     * whereEvenNull 用于 DELETE
     */
    public void deleteByIdForced(Long id) {
        int rows = TOrderDesign.delete("deleteByIdForced").whereEvenNull().id.eq(id).over();
    }
}
