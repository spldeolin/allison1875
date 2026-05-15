package com.example.dto.req;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单项
 *
 * @author test-author 2026-04-29
 */
@Data
public class OrderItemReq {

    /**
     * 商品ID
     */
    @NotNull
    private Long productId;

    /**
     * 购买数量
     */
    @NotNull
    @Min(1)
    private Integer quantity;

}
