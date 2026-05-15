package com.example.service;

import com.example.design.TOrderDesign;
import java.util.List;

public class OrderService {

    /**
     * LEFT JOIN：订单关联用户，选择用户名
     */
    public void listOrderWithUserName() {
        TOrderDesign.select("listOrderWithUserName").leftJoin().TUserEntity.userName.on().id.eq(TOrderDesign.userId).list();
    }
}
