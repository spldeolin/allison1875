package com.example.dto.common;

import lombok.Data;

/**
 * 订单项DTO（第2层嵌套）
 *
 * @author test-author 2026-04-29
 */
@Data
public class OrderItemDTO {

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 商品详情（第3层嵌套）
     */
    private ItemDetailDTO detail;

}
