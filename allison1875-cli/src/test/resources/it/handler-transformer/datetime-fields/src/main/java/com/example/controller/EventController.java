package com.example.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 事件管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/event")
public class EventController {

    {
        String handler = "/create-event";
        String desc = "创建事件";
        class Req {
            /** 事件名称 */
            String eventName;
            /** 创建时间 (Date) */
            Date createTime;
            /** 开始时间 (LocalDateTime) */
            LocalDateTime startTime;
            /** 事件日期 (LocalDate) — 已有 @JsonFormat，不应被覆盖 */
            @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy/MM/dd")
            LocalDate eventDate;
            /** 提醒时刻 (LocalTime) */
            LocalTime remindTime;
        }
        class Resp {
            /** 事件ID */
            Long eventId;
            /** 创建时间 */
            Date createdAt;
            /** 更新时间 */
            LocalDateTime updatedAt;
        }
    }

}
