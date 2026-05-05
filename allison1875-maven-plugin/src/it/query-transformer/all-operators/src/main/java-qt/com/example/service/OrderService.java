package com.example.service;

import com.example.design.TOrderDesign;
import com.example.entity.TOrderEntity;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.List;

public class OrderService {

    /**
     * eq运算符：等于
     */
    public void queryByEq(Long id) {
        TOrderEntity result = TOrderDesign.select("queryByEq").where().id.eq(id).one();
    }

    /**
     * ne运算符：不等于
     */
    public void queryByNe(Byte status) {
        List<TOrderEntity> result = TOrderDesign.select("queryByNe").where().status.ne(status).list();
    }

    /**
     * gt运算符：大于
     */
    public void queryByGt(BigDecimal minAmount) {
        List<TOrderEntity> result = TOrderDesign.select("queryByGt").where().amount.gt(minAmount).list();
    }

    /**
     * ge运算符：大于等于
     */
    public void queryByGe(BigDecimal minAmount) {
        List<TOrderEntity> result = TOrderDesign.select("queryByGe").where().amount.ge(minAmount).list();
    }

    /**
     * lt运算符：小于
     */
    public void queryByLt(BigDecimal maxAmount) {
        List<TOrderEntity> result = TOrderDesign.select("queryByLt").where().amount.lt(maxAmount).list();
    }

    /**
     * le运算符：小于等于
     */
    public void queryByLe(BigDecimal maxAmount) {
        List<TOrderEntity> result = TOrderDesign.select("queryByLe").where().amount.le(maxAmount).list();
    }

    /**
     * like运算符：模糊匹配
     */
    public void queryByLike(String keyword) {
        List<TOrderEntity> result = TOrderDesign.select("queryByLike").where().orderNo.like(keyword).list();
    }

    /**
     * in运算符：IN列表
     */
    public void queryByIn(List<Byte> statusList) {
        List<TOrderEntity> result = TOrderDesign.select("queryByIn").where().status.in(statusList).list();
    }

    /**
     * nin运算符：NOT IN列表
     */
    public void queryByNin(List<Byte> excludeStatuses) {
        List<TOrderEntity> result = TOrderDesign.select("queryByNin").where().status.nin(excludeStatuses).list();
    }

    /**
     * notnull运算符：IS NOT NULL
     */
    public void queryByNotnull() {
        List<TOrderEntity> result = TOrderDesign.select("queryByNotnull").where().remark.notnull().list();
    }

    /**
     * isnull运算符：IS NULL
     */
    public void queryByIsnull() {
        List<TOrderEntity> result = TOrderDesign.select("queryByIsnull").where().remark.isnull().list();
    }
}
