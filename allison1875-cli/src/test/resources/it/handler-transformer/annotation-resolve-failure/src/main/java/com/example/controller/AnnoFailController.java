package com.example.controller;

import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注解兼容性测试 — Controller 上仅有 @RestController 和 @RequestMapping 注解
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/annofail")
public class AnnoFailController {

    {
        String handler = "/create-task";
        String desc = "创建任务";
        class Req {
            /** 任务名 */
            @NotBlank
            String taskName;
        }
        class Resp {
            /** 任务ID */
            Long taskId;
        }
    }

}
