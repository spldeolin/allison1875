package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.dto.req.CreateReportReq;
import com.example.dto.resp.ReportResp;

/**
 * 报表管理
 *
 * @author test-author 2026-04-29
 */
@Controller
@RequestMapping("/api/reports")
public class ReportController {

    /**
     * 查询报表（有@ResponseBody）
     */
    @GetMapping
    @ResponseBody
    public ReportResp getReport() {
        return null;
    }

    /**
     * 创建报表（有@ResponseBody）
     */
    @PostMapping
    @ResponseBody
    public ReportResp createReport(@RequestBody CreateReportReq req) {
        return null;
    }

}
