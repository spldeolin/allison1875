package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /**
     * 列出商品
     */
    @GetMapping("/list")
    public String listProducts() {
        return "ok";
    }

}
