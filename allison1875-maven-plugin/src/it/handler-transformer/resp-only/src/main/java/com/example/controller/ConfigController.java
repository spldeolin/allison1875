package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    {
        String handler = "/get-config";
        String desc = "获取系统配置";
        class Resp {
            /** 应用名称 */
            String appName;
            /** 版本号 */
            String version;
            /** 是否维护中 */
            Boolean maintenance;
        }
    }

}
