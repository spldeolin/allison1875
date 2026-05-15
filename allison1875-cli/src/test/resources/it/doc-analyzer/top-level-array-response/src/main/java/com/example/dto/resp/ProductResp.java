package com.example.dto.resp;

import java.math.BigDecimal;
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
    private String id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

}
