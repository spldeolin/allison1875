package com.example.dto.resp;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author test-author 2026-05-13
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleResp {

    /**
     * 文章ID
     */
    Long id;

    /**
     * 文章标题
     */
    String title;

    /**
     * 文章内容
     */
    String content;

}