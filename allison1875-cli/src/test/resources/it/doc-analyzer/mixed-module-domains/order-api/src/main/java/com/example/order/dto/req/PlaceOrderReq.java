package com.example.order.dto.req;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class PlaceOrderReq {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 收货地址
     */
    private String shippingAddress;

}
