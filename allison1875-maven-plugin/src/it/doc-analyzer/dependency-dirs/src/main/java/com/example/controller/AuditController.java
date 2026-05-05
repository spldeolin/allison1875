package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.resp.AuditResp;

/**
 * 审计管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    /**
     * 查询审计信息
     */
    @GetMapping
    public AuditResp getAudit() {
        return null;
    }

}
