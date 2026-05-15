package com.example.dto.req;

import lombok.Data;

/**
 * 更新订单请求
 *
 * @author test-author 2026-04-29
 */
@Data
public class UpdateOrderReq {

    /**
     * 收货地址
     */
    private String shippingAddress;

    /**
     * 买家备注
     */
    private String buyerNote;

}
