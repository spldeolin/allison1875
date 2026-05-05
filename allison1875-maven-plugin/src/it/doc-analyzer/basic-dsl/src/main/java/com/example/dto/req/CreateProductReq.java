package com.example.dto.req;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建商品请求
 *
 * @author test-author 2026-04-29
 */
@Data
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

}
