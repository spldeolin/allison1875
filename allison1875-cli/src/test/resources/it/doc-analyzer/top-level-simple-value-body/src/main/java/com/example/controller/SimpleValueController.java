package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简单值处理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/simple")
public class SimpleValueController {

    /**
     * 发送字符串消息
     *
     * @param message 消息内容
     * @return 处理结果
     */
    @PostMapping("/string")
    public String sendString(@RequestBody String message) {
        return message;
    }

    /**
     * 发送数字计数
     *
     * @param count 计数值
     * @return 计数值
     */
    @PostMapping("/integer")
    public Integer sendInteger(@RequestBody Integer count) {
        return count;
    }

}
