package com.example.dto.req;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 创建订单请求
 *
 * @author test-author 2026-04-29
 */
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

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getBuyerNote() {
        return buyerNote;
    }

    public void setBuyerNote(String buyerNote) {
        this.buyerNote = buyerNote;
    }

    public List<OrderItemReq> getItems() {
        return items;
    }

    public void setItems(List<OrderItemReq> items) {
        this.items = items;
    }

}
