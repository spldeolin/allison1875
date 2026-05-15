package com.example.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.resp.SettingResp;

/**
 * 系统设置
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/admin/settings")
public class SettingController {

    /**
     * 查询系统设置
     */
    @GetMapping
    public SettingResp getSettings() {
        return null;
    }

}
