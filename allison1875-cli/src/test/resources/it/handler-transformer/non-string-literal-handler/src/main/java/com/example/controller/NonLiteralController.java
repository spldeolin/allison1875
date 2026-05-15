package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 非字符串handler测试 — handler初始值为方法调用
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/nonliteral")
public class NonLiteralController {

    private String getUrl() {
        return "/some-url";
    }

    {
        String handler = getUrl();
        String desc = "非字符串handler测试";
        class Req {
            String name;
        }
    }

}
