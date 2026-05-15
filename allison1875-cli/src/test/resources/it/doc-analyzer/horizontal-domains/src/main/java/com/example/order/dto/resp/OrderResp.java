package com.example.order.dto.resp;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class OrderResp {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 数量
     */
    private Integer quantity;

}
