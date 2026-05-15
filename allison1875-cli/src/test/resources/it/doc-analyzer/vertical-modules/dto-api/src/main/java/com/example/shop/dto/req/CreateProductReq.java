package com.example.shop.dto.req;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class CreateProductReq {

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 价格
     */
    private java.math.BigDecimal price;

    /**
     * 描述
     */
    private String description;

}
