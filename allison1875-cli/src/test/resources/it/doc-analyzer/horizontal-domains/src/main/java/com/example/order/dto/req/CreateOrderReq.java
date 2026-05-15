package com.example.order.dto.req;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class CreateOrderReq {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 数量
     */
    private Integer quantity;

}
