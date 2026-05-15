package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    {
        String handler = "/ping";
        String desc = "健康检查";
    }

}
