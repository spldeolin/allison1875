package com.example.dto.req;

import javax.validation.constraints.NotBlank;
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
public class CreateArticleReq {

    /**
     * 文章标题
     */
    @NotBlank
    String title;

    /**
     * 文章内容
     */
    @NotBlank
    String content;

}