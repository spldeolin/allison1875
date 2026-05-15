package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 跳过测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/skip")
public class SkipController {

    {
        // 故意不声明 handler 变量，只有 desc
        String desc = "这个 init 块缺少 handler 变量";
        class Req {
            String name;
        }
    }

}
