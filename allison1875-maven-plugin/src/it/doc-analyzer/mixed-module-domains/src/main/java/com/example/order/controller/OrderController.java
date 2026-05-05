package com.example.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.order.dto.req.PlaceOrderReq;
import com.example.order.dto.resp.OrderResp;

/**
 * 订单管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * 下单
     */
    @PostMapping
    public OrderResp placeOrder(@RequestBody PlaceOrderReq req) {
        return null;
    }

}
