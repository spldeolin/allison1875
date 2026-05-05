package com.example.controller;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    {
        String handler = "/create-product";
        String desc = "创建商品";
        class Req {
            /** 商品名称 */
            @NotBlank
            String productName;
            /** 价格 */
            @NotNull
            Long price;
        }
        class Resp {
            /** 商品ID */
            Long productId;
        }
    }

    {
        String handler = "/delete-product";
        String desc = "删除商品";
        class Req {
            /** 商品ID */
            @NotNull
            Long productId;
        }
    }

    {
        String handler = "/get-product-detail";
        String desc = "查询商品详情";
        class Resp {
            /** 商品名称 */
            String productName;
            /** 价格 */
            Long price;
            /** 库存 */
            Integer stock;
        }
    }

}
