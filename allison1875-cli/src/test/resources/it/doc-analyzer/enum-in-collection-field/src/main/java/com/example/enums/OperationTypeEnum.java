package com.example.enums;

/**
 * 操作类型枚举
 *
 * @author test-author 2026-04-29
 */
public enum OperationTypeEnum {

    INSERT(1, "新增"),
    UPDATE(2, "修改"),
    DELETE(3, "删除"),
    ;

    private final int code;
    private final String title;

    OperationTypeEnum(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public int getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

}
