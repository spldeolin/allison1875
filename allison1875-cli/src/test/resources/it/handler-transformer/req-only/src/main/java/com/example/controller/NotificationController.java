package com.example.controller;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    {
        String handler = "/send-notification";
        String desc = "发送通知";
        class Req {
            /** 接收人ID */
            @NotNull
            Long receiverId;
            /** 通知内容 */
            @NotBlank
            String content;
        }
    }

}
