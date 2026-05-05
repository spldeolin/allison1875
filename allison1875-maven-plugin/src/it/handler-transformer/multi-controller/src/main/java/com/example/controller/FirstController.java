package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第一个Controller
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/first")
public class FirstController {

    {
        String handler = "/do-first";
        String desc = "第一个操作";
        class Req {
            /** 参数A */
            String paramA;
        }
        class Resp {
            /** 结果A */
            String resultA;
        }
    }

}
