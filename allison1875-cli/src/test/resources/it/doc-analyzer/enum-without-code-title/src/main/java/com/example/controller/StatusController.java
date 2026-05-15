package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.SimpleStatusReq;
import com.example.dto.resp.SimpleStatusResp;

/**
 * 状态管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/status")
public class StatusController {

    /**
     * 更新状态
     */
    @PostMapping
    public SimpleStatusResp update(@RequestBody SimpleStatusReq req) {
        return null;
    }

}
