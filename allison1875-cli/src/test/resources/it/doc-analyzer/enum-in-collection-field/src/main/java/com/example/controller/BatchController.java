package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.BatchUpdateReq;
import com.example.dto.resp.BatchUpdateResp;

/**
 * 批量操作管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    /**
     * 批量更新状态
     */
    @PostMapping
    public BatchUpdateResp batchUpdate(@RequestBody BatchUpdateReq req) {
        return null;
    }

}
