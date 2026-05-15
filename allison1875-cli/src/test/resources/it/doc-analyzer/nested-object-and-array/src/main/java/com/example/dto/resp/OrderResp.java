package com.example.dto.resp;

import java.util.List;
import com.example.dto.common.CustomerDTO;
import com.example.dto.common.OrderItemDTO;
import lombok.Data;

/**
 * 订单响应
 *
 * @author test-author 2026-04-29
 */
@Data
public class OrderResp {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 客户信息（嵌套对象）
     */
    private CustomerDTO customer;

    /**
     * 订单明细（嵌套对象数组）
     */
    private List<OrderItemDTO> items;

}
