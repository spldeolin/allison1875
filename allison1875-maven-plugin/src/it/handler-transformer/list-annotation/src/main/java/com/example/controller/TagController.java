package com.example.controller;

import com.spldeolin.allison1875.support.L;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标签管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/tag")
public class TagController {

    {
        String handler = "/list-tags";
        String desc = "查询标签列表";
        @L
        class Resp {
            /** 标签ID */
            Long tagId;
            /** 标签名 */
            String tagName;
        }
    }

}
