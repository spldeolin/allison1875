package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.ArrayEnumReq;
import com.example.dto.resp.ArrayEnumResp;

/**
 * 数组枚举测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/array-enum")
public class ArrayEnumController {

    /**
     * 提交权限
     */
    @PostMapping
    public ArrayEnumResp submit(@RequestBody ArrayEnumReq req) {
        return null;
    }

}
