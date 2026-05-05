package com.example.dto.resp;

import lombok.Data;

/**
 * 用户响应
 *
 * @author test-author 2026-04-29
 */
@Data
public class UserResp {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 邮箱
     */
    private String email;

}
