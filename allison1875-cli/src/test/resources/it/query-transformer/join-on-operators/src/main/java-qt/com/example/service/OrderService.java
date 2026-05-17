package com.example.service;

import com.example.design.TOrderDesign;
import java.util.List;

public class OrderService {

    /**
     * ON 条件使用 ne（!=）
     */
    public void listByOnNe() {
        TOrderDesign.select("listByOnNe").leftJoin().TUserEntity.userName.on().id.ne(TOrderDesign.userId).list();
    }

    /**
     * ON 条件使用 gt（>）
     */
    public void listByOnGt(Long userId) {
        TOrderDesign.select("listByOnGt").leftJoin().TUserEntity.userName.on().id.gt(userId).list();
    }

    /**
     * ON 条件使用 in
     */
    public void listByOnIn(List<Long> userIds) {
        TOrderDesign.select("listByOnIn").leftJoin().TUserEntity.userName.on().id.in(userIds).list();
    }
}