package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.resp.IntegerSubtypeResp;

/**
 * 整数类型测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/integers")
public class IntegerSubtypeController {

    /**
     * 获取整数类型示例
     */
    @GetMapping
    public IntegerSubtypeResp getSample() {
        return null;
    }

}
