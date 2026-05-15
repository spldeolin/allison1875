package com.example.shop.dto.resp;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class ProductResp {

    /**
     * ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 价格
     */
    private java.math.BigDecimal price;

}
