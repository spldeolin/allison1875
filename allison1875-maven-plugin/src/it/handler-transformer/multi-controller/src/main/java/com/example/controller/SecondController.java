package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第二个Controller
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/second")
public class SecondController {

    {
        String handler = "/do-second";
        String desc = "第二个操作";
        class Req {
            /** 参数B */
            String paramB;
        }
        class Resp {
            /** 结果B */
            String resultB;
        }
    }

}
