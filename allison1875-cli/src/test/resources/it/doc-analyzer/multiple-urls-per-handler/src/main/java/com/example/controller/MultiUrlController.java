package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.resp.UserResp;

/**
 * 多URL映射
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping({"/api/users", "/api/v2/users"})
public class MultiUrlController {

    /**
     * 查询用户（Controller和Method各有多个path，触发笛卡尔组合）
     */
    @GetMapping({"/list", "/all"})
    public UserResp listUsers() {
        return null;
    }

    /**
     * 创建用户
     */
    @PostMapping({"/create", "/new"})
    public UserResp createUser() {
        return null;
    }

}
