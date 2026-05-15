package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateEventReq;
import com.example.dto.resp.EventResp;

/**
 * 事件管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    /**
     * 创建事件
     */
    @PostMapping
    public EventResp createEvent(@RequestBody CreateEventReq req) {
        return null;
    }

}
