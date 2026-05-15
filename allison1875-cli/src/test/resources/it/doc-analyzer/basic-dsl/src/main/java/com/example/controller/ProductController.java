package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateProductReq;
import com.example.dto.resp.ProductResp;

/**
 * 商品管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /**
     * 根据ID查询商品
     */
    @GetMapping("/{id}")
    public ProductResp getProduct(@PathVariable Long id) {
        return null;
    }

    /**
     * 创建商品
     */
    @PostMapping
    public ProductResp createProduct(@RequestBody CreateProductReq req) {
        return null;
    }

}
