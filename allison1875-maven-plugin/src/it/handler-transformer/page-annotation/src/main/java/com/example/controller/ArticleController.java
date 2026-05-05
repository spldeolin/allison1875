package com.example.controller;

import com.spldeolin.allison1875.support.P;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分页查询
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    {
        String handler = "/page-articles";
        String desc = "分页查询文章";
        class Req {
            /** 分类ID */
            Long categoryId;
            /** 页码 */
            @NotNull
            Integer pageNo;
            /** 每页条数 */
            @NotNull
            Integer pageSize;
        }
        @P
        class Resp {
            /** 文章ID */
            Long articleId;
            /** 标题 */
            String title;
            /** 摘要 */
            String summary;
        }
    }

}
