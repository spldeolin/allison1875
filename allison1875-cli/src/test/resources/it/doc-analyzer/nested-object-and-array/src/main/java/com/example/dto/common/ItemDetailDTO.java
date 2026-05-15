package com.example.dto.common;

import lombok.Data;

/**
 * 商品详情DTO（第3层嵌套，最深层级）
 *
 * @author test-author 2026-04-29
 */
@Data
public class ItemDetailDTO {

    /**
     * SKU编码
     */
    private String sku;

    /**
     * 仓库名称
     */
    private String warehouse;

}
