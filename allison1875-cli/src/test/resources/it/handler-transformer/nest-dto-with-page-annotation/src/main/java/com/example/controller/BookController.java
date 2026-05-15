package com.example.controller;

import com.spldeolin.allison1875.support.P;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 嵌套DTO @P 注解测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/book")
public class BookController {

    {
        String handler = "/get-book";
        String desc = "查询书籍详情";
        class Resp {
            /** 书名 */
            String bookName;
            /** 章节列表（嵌套 DTO 标注 @P） */
            @P
            class Chapter {
                /** 章节标题 */
                String chapterTitle;
                /** 页数 */
                Integer pageCount;
            }
        }
    }

}
