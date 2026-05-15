package com.example.controller;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateOrderReq;
import com.example.dto.req.UpdateOrderReq;
import com.example.dto.resp.OrderDetailResp;

/**
 * 订单管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * 查询订单列表
     *
     * 支持按状态筛选
     */
    @GetMapping
    public List<OrderDetailResp> listOrders(@RequestParam(required = false) String status) {
        return null;
    }

    /**
     * 根据ID查询订单详情
     */
    @GetMapping("/{orderId}")
    public OrderDetailResp getOrder(@PathVariable Long orderId) {
        return null;
    }

    /**
     * 创建订单
     */
    @PostMapping
    public OrderDetailResp createOrder(@RequestBody CreateOrderReq req) {
        return null;
    }

    /**
     * 更新订单
     */
    @PutMapping("/{orderId}")
    public OrderDetailResp updateOrder(@PathVariable Long orderId, @RequestBody UpdateOrderReq req) {
        return null;
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable Long orderId) {
    }

}
