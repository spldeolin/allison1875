package com.example.dto.resp;

import java.util.List;

/**
 * 订单详情响应
 *
 * @author test-author 2026-04-29
 */
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

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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

    public List<OrderItemResp> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResp> items) {
        this.items = items;
    }

}
