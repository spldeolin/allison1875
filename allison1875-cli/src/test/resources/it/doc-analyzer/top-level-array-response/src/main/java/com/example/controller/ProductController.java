package com.example.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
     * 获取所有商品（直接返回List&lt;ProductResp&gt;，根节点为ArraySchema）
     */
    @GetMapping
    public List<ProductResp> getAllProducts() {
        return null;
    }

}
