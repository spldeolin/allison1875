package com.example.service;

import com.example.design.TOrderDesign;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderService {

    /**
     * UPDATE：单字段更新 + 单条件
     */
    public void updateStatus(Byte status, Long id) {
        int rows = TOrderDesign.update("updateStatusById").status(status).where().id.eq(id).over();
    }

    /**
     * UPDATE：多字段更新 + 单条件
     */
    public void updateAmountAndRemark(BigDecimal amount, String remark, Long id) {
        int rows = TOrderDesign.update("updateAmountAndRemarkById").amount(amount).remark(remark).where().id.eq(id).over();
    }

    /**
     * DELETE：按ID删除
     */
    public void deleteById(Long id) {
        int rows = TOrderDesign.delete("deleteOrderById").where().id.eq(id).over();
    }

    /**
     * DELETE：按用户ID删除
     */
    public void deleteByUserId(Long userId) {
        int rows = TOrderDesign.delete("deleteByUserId").where().userId.eq(userId).over();
    }
}
