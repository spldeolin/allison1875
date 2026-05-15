package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * 列出用户
     */
    @GetMapping("/list")
    public String listUsers() {
        return "ok";
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/detail")
    public String getUserDetail() {
        return "ok";
    }

}
