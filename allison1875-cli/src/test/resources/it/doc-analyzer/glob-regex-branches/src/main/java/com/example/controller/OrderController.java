package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * 列出订单
     */
    @GetMapping("/list")
    public String listOrders() {
        return "ok";
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail")
    public String getOrderDetail() {
        return "ok";
    }

}
