package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.resp.ConfigResp;

/**
 * 配置管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping(value = "/api/config", params = {"module=system"})
public class ConfigController {

    /**
     * 查询配置
     */
    @GetMapping(params = {"action=read"})
    public ConfigResp getConfig() {
        return null;
    }

}
