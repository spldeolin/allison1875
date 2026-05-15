package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 错误内部类名测试 — init块中内部类名为Input（非Req也非Resp），应抛异常
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/wrong")
public class WrongNameController {

    {
        String handler = "/do-something";
        String desc = "错误内部类名";
        class Input {
            String name;
        }
    }

}
