package com.example.service;

import com.example.design.TOrderDesign;
import java.util.List;

public class OrderService {

    /**
     * RIGHT JOIN：订单右外连接用户
     */
    public void listByRightJoin() {
        TOrderDesign.select("listByRightJoin").rightJoin().TUserEntity.userName.on().id.eq(TOrderDesign.userId).list();
    }

    /**
     * INNER JOIN：订单内连接用户
     */
    public void listByInnerJoin() {
        TOrderDesign.select("listByInnerJoin").innerJoin().TUserEntity.userName.on().id.eq(TOrderDesign.userId).list();
    }

    /**
     * OUTER JOIN：订单全外连接用户
     */
    public void listByOuterJoin() {
        TOrderDesign.select("listByOuterJoin").outerJoin().TUserEntity.userName.on().id.eq(TOrderDesign.userId).list();
    }
}