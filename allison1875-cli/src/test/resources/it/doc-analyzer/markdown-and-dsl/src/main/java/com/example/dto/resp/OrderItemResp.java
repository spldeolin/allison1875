package com.example.dto.resp;

import lombok.Data;

/**
 * 订单项响应
 *
 * @author test-author 2026-04-29
 */
@Data
public class OrderItemResp {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 单价（分）
     */
    private Long unitPrice;

}
