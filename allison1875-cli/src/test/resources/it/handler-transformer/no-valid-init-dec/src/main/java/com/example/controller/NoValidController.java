package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 无有效init块测试 — init块中只有desc和Req，缺少handler变量
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/novalid")
public class NoValidController {

    {
        // 只有 desc 和 Req，无 handler 变量
        String desc = "无handler变量的init块";
        class Req {
            String name;
        }
    }

}
