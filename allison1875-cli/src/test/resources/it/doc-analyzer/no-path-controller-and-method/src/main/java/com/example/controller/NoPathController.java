package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 无路径映射
 *
 * @author test-author 2026-04-29
 */
@RestController
public class NoPathController {

    /**
     * 默认首页（无@RequestMapping也无@GetMapping value，URL回退为 /）
     */
    @GetMapping
    public String index() {
        return "index";
    }

}
