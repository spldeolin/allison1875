package com.example.service;

/**
 * 已有的CreateOrderService（会被重命名触发antiDuplication）
 *
 * @author test-author 2026-04-29
 */
public interface CreateOrderService {

    /** 已有的查询方法（占位符，触发文件名冲突） */
    String queryOrder(Long orderId);

}
