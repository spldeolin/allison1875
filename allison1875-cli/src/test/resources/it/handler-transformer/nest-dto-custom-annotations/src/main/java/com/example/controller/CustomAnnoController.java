package com.example.controller;

import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 嵌套DTO自定义注解迁移测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/custom")
public class CustomAnnoController {

    {
        String handler = "/create-item";
        String desc = "创建项目";
        class Req {
            /** 项目名 */
            @NotNull
            String itemName;
            /** 详情（含 @Deprecated 自定义注解，应迁移到父DTO字段上） */
            @Deprecated
            class Detail {
                /** 描述 */
                String description;
                /** 优先级 */
                Integer priority;
            }
        }
    }

}
