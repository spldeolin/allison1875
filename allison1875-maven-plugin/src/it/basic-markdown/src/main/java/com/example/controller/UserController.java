package com.example.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateUserReq;
import com.example.dto.resp.UserResp;

/**
 * 用户管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * 查询用户列表
     *
     * 根据关键字搜索用户
     */
    @GetMapping
    public List<UserResp> listUsers(@RequestParam(required = false) String keyword) {
        return null;
    }

    /**
     * 创建用户
     */
    @PostMapping
    public UserResp createUser(@RequestBody CreateUserReq req) {
        return null;
    }

}
