package com.example.dto.req;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 创建订单请求
 *
 * @author test-author 2026-04-29
 */
@Data
public class CreateOrderReq {

    /**
     * 收货地址
     */
    @NotBlank
    private String shippingAddress;

    /**
     * 买家备注
     */
    private String buyerNote;

    /**
     * 订单项列表
     */
    @NotEmpty
    @Valid
    private List<OrderItemReq> items;

}
