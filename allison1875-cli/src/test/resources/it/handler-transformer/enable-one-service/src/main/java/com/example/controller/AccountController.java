package com.example.controller;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账户管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    {
        String handler = "/create-account";
        String desc = "创建账户";
        class Req {
            /** 账户名 */
            @NotBlank
            String accountName;
        }
        class Resp {
            /** 账户ID */
            Long accountId;
        }
    }

    {
        String handler = "/freeze-account";
        String desc = "冻结账户";
        class Req {
            /** 账户ID */
            @NotNull
            Long accountId;
        }
    }

    {
        String handler = "/get-account-balance";
        String desc = "查询余额";
        class Resp {
            /** 余额 */
            Long balance;
        }
    }

}
