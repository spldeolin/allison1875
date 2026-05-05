package com.example.order.dto.resp;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class OrderResp {

    /**
     * 订单号
     */
    private String orderNo;

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
