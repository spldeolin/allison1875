package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 3个内部类测试 — 应抛出异常
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/three")
public class ThreeCoidController {

    {
        String handler = "/do-something";
        String desc = "三个内部类";
        class Req {
            String name;
        }
        class Resp {
            Long id;
        }
        class Extra {
            String extraField;
        }
    }

}
