package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.resp.PingResp;

/**
 * 无类级RequestMapping
 *
 * @author test-author 2026-04-29
 */
@RestController
public class PingController {

    /**
     * Ping接口
     */
    @GetMapping("/ping")
    public PingResp ping() {
        return null;
    }

    /**
     * Version接口
     */
    @GetMapping("/version")
    public PingResp version() {
        return null;
    }

}
