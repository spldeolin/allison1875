package com.example.dto.req;

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
public class UpdateArticleReq {

    /**
     * 文章标题
     */
    String title;

    /**
     * 文章内容
     */
    String content;

}