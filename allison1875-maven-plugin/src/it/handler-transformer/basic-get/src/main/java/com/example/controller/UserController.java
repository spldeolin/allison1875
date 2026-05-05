package com.example.controller;

import com.spldeolin.allison1875.support.GetUrlQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    {
        String handler = "/list-users";
        String desc = "查询用户列表";
        @GetUrlQuery
        class Req {
            /** 关键字 */
            String keyword;
            /** 页码 */
            Integer pageNo;
        }
        class Resp {
            /** 用户ID */
            Long userId;
            /** 用户名 */
            String username;
        }
    }

}
