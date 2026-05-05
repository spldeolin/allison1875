package com.example.dto.req;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 订单项
 *
 * @author test-author 2026-04-29
 */
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
