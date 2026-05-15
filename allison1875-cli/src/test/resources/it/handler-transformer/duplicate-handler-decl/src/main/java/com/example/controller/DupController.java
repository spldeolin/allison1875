package com.example.controller;

import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 重复声明测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/dup")
public class DupController {

    {
        String handler = "/first-url";
        String h = "/second-url";
        String desc = "重复handler声明测试";
        class Req {
            /** 字段 */
            @NotBlank
            String field;
        }
    }

}
