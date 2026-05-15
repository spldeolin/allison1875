package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回退HTTP方法
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/fallback")
public class FallbackController {

    /**
     * 通用入口（裸@RequestMapping无method属性，combinedVerbs回退为所有HTTP方法）
     */
    @RequestMapping("/entry")
    public String entry() {
        return "ok";
    }

}
