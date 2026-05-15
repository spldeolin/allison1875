package com.example.dto.resp;

import lombok.Data;

/**
 * 商品响应
 *
 * @author test-author 2026-04-29
 */
@Data
public class ProductResp {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 价格（分）
     */
    private Long price;

}
