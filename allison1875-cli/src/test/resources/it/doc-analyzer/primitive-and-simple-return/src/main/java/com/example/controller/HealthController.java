package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * 返回纯字符串
     *
     * @return 健康状态
     */
    @GetMapping("/status")
    public String getStatus() {
        return "OK";
    }

    /**
     * 返回计数
     *
     * @return 在线用户数
     */
    @GetMapping("/count")
    public int getCount() {
        return 0;
    }

    /**
     * 返回是否健康
     *
     * @return 是否正常
     */
    @GetMapping("/alive")
    public boolean isAlive() {
        return true;
    }

}
