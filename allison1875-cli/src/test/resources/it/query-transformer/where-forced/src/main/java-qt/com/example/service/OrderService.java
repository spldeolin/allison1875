package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
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

    /**
     * whereEvenNull + ne：强制条件 !=
     */
    public void queryByNeForced(Byte status) {
        List<TOrderEntity> result = TOrderDesign.select("queryByNeForced").whereEvenNull().status.ne(status).list();
    }

    /**
     * whereEvenNull + gt：强制条件 >
     */
    public void queryByGtForced(BigDecimal minAmount) {
        List<TOrderEntity> result = TOrderDesign.select("queryByGtForced").whereEvenNull().amount.gt(minAmount).list();
    }

    /**
     * whereEvenNull + ge：强制条件 >=
     */
    public void queryByGeForced(BigDecimal minAmount) {
        List<TOrderEntity> result = TOrderDesign.select("queryByGeForced").whereEvenNull().amount.ge(minAmount).list();
    }

    /**
     * whereEvenNull + lt：强制条件 <
     */
    public void queryByLtForced(BigDecimal maxAmount) {
        List<TOrderEntity> result = TOrderDesign.select("queryByLtForced").whereEvenNull().amount.lt(maxAmount).list();
    }

    /**
     * whereEvenNull + le：强制条件 <=
     */
    public void queryByLeForced(BigDecimal maxAmount) {
        List<TOrderEntity> result = TOrderDesign.select("queryByLeForced").whereEvenNull().amount.le(maxAmount).list();
    }

    /**
     * whereEvenNull + like：强制条件 LIKE
     */
    public void queryByLikeForced(String keyword) {
        List<TOrderEntity> result = TOrderDesign.select("queryByLikeForced").whereEvenNull().orderNo.like(keyword).list();
    }

    /**
     * whereEvenNull + in：强制条件 IN
     */
    public void queryByInForced(List<Byte> statusList) {
        List<TOrderEntity> result = TOrderDesign.select("queryByInForced").whereEvenNull().status.in(statusList).list();
    }

    /**
     * whereEvenNull + nin：强制条件 NOT IN
     */
    public void queryByNinForced(List<Byte> excludeStatuses) {
        List<TOrderEntity> result = TOrderDesign.select("queryByNinForced").whereEvenNull().status.nin(excludeStatuses).list();
    }
}
