package com.example.dto.req;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建用户请求
 *
 * @author test-author 2026-04-29
 */
@Data
public class CreateUserReq {

    /**
     * 用户名
     */
    @NotBlank
    private String username;

    /**
     * 年龄
     */
    @NotNull
    private Integer age;

    /**
     * 邮箱
     */
    private String email;

}
