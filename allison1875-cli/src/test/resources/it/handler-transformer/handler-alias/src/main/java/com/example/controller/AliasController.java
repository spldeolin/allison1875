package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 别名测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/alias")
public class AliasController {

    {
        String h = "/do-something";
        String d = "执行操作";
        class Req {
            /** 操作码 */
            String code;
        }
        class Resp {
            /** 结果 */
            Boolean success;
        }
    }

}
