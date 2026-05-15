package com.example.dto.resp;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class PingResp {

    /**
     * 响应消息
     */
    private String message;

    /**
     * 时间戳
     */
    private Long timestamp;

}
