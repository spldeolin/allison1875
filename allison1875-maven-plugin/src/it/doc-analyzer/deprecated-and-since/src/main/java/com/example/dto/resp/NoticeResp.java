package com.example.dto.resp;

import lombok.Data;

/**
 * 公告响应
 *
 * @author test-author 2026-04-29
 */
@Data
public class NoticeResp {

    /**
     * 公告ID
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 优先级
     */
    private Integer priority;

}
