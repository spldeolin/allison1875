package com.example.shop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.shop.dto.req.CreateProductReq;
import com.example.shop.dto.resp.ProductResp;

/**
 * 商品管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /**
     * 创建商品
     */
    @PostMapping
    public ProductResp createProduct(@RequestBody CreateProductReq req) {
        return null;
    }

    /**
     * 查询商品列表
     */
    @GetMapping("/list")
    public String listProducts() {
        return "ok";
    }

}
