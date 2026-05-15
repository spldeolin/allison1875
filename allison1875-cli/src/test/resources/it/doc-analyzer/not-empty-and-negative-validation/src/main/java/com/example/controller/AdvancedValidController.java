package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.AdvancedValidReq;
import com.example.dto.resp.AdvancedValidResp;

/**
 * 高级校验
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/advanced-valid")
public class AdvancedValidController {

    /**
     * 提交校验
     */
    @PostMapping
    public AdvancedValidResp submit(@RequestBody AdvancedValidReq req) {
        return null;
    }

}
