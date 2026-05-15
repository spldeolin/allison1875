package com.example.dto.req;

import java.util.List;
import com.example.dto.common.AddressDTO;
import com.example.dto.common.CustomerDTO;
import com.example.dto.common.OrderItemDTO;
import lombok.Data;

/**
 * 创建订单请求（3层嵌套：CustomerDTO → AddressDTO, List&lt;OrderItemDTO&gt; → ItemDetailDTO）
 *
 * @author test-author 2026-04-29
 */
@Data
public class CreateOrderReq {

    /**
     * 客户信息（嵌套对象）
     */
    private CustomerDTO customer;

    /**
     * 订单明细列表（嵌套对象数组）
     */
    private List<OrderItemDTO> items;

    /**
     * 收货地址（共享DTO引用）
     */
    private AddressDTO shippingAddress;

}
