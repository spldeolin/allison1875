package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateOrderReq;
import com.example.dto.resp.OrderResp;

/**
 * 订单管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * 创建订单
     */
    @PostMapping
    public OrderResp createOrder(@RequestBody CreateOrderReq req) {
        return null;
    }

}
