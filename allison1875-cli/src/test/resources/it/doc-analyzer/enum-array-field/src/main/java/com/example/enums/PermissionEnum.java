package com.example.enums;

/**
 * 权限类型枚举
 *
 * @author test-author 2026-04-29
 */
public enum PermissionEnum {
    READ(1, "读取"),
    WRITE(2, "写入"),
    ADMIN(3, "管理"),
    ;
    private final int code;
    private final String title;
    PermissionEnum(int code, String title) { this.code = code; this.title = title; }
    public int getCode() { return code; }
    public String getTitle() { return title; }
}
