package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateNoticeReq;
import com.example.dto.resp.NoticeResp;

/**
 * 公告管理
 *
 * @since v2.0.0
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    /**
     * 查询最新公告
     *
     * @since v1.0.0
     */
    @GetMapping("/latest")
    public NoticeResp getLatest() {
        return null;
    }

    /**
     * 创建公告
     *
     * @since v2.0.0
     */
    @PostMapping
    public NoticeResp createNotice(@RequestBody CreateNoticeReq req) {
        return null;
    }

    /**
     * 查询过期公告（已废弃）
     *
     * @deprecated 请使用 getLatest 替代，本接口将在 v4.0 移除
     * @since v1.0.0
     */
    @GetMapping("/expired")
    public NoticeResp getExpired() {
        return null;
    }

}
