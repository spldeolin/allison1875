package com.example.dto.req;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建商品请求
 *
 * @author test-author 2026-04-29
 */
public class CreateProductReq {

    /**
     * 商品名称
     */
    @NotBlank
    private String productName;

    /**
     * 价格（分）
     */
    @NotNull
    private Long price;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

}
