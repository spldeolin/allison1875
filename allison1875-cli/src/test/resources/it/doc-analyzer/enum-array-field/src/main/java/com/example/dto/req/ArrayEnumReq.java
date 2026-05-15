package com.example.dto.req;

import com.example.enums.PermissionEnum;
import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class ArrayEnumReq {

    /**
     * 权限列表（枚举数组，触发 isArray() 分支）
     */
    private PermissionEnum[] permissions;

    /**
     * 用户名
     */
    private String username;
}
