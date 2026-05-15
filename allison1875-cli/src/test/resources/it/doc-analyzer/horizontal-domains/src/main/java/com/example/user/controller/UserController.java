package com.example.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.user.dto.req.CreateUserReq;
import com.example.user.dto.resp.UserResp;

/**
 * 用户管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * 创建用户
     */
    @PostMapping
    public UserResp createUser(@RequestBody CreateUserReq req) {
        return null;
    }

    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    public String listUsers() {
        return "ok";
    }

}
