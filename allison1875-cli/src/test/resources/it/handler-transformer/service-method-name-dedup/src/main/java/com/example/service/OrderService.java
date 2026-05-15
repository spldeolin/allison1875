package com.example.service;

/**
 * 订单管理
 *
 * @author test-author 2026-04-29
 */
public interface OrderService {

    /** 已有的 createOrder 方法（将触发方法名去重） */
    void createOrder(Long productId);

}
