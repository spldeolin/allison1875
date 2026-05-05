package com.example.dto.req;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 搜索请求
 *
 * @author test-author 2026-04-29
 */
@Data
public class SearchReq {

    /**
     * 关键词
     */
    @NotBlank
    private String keyword;

    /**
     * 此字段在文档中不展示
     * #API-DOC-IGNORE#
     */
    private String internalTraceId;

}
