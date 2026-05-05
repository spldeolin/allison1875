package com.example.dto.req;

/**
 * 更新订单请求
 *
 * @author test-author 2026-04-29
 */
public class UpdateOrderReq {

    /**
     * 收货地址
     */
    private String shippingAddress;

    /**
     * 买家备注
     */
    private String buyerNote;

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

}
