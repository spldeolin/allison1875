package com.example.dto.resp;

import java.util.List;
import lombok.Data;

/**
 * 订单详情响应
 *
 * @author test-author 2026-04-29
 */
@Data
public class OrderDetailResp {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 收货地址
     */
    private String shippingAddress;

    /**
     * 买家备注
     */
    private String buyerNote;

    /**
     * 订单项列表
     */
    private List<OrderItemResp> items;

}
