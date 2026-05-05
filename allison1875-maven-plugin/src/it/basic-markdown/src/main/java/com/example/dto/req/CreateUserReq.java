package com.example.dto.req;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建用户请求
 *
 * @author test-author 2026-04-29
 */
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
