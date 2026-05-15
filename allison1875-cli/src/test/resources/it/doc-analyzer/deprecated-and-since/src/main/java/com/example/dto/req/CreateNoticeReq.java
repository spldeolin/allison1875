package com.example.dto.req;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建公告请求
 *
 * @author test-author 2026-04-29
 */
@Data
public class CreateNoticeReq {

    /**
     * 公告标题
     */
    @NotBlank
    private String title;

    /**
     * 公告内容
     */
    @NotBlank
    private String content;

    /**
     * 优先级
     *
     * @since v3.0.0
     */
    private Integer priority;

    /**
     * 旧分类字段
     *
     * @deprecated 请使用 tags 字段替代
     */
    private String category;

}
