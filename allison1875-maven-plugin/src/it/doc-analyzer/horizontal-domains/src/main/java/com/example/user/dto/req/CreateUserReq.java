package com.example.user.dto.req;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class CreateUserReq {

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

}
