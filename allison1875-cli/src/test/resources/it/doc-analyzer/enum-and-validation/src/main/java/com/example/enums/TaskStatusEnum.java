package com.example.enums;

/**
 * 任务状态枚举
 *
 * @author test-author 2026-04-29
 */
public enum TaskStatusEnum {

    PENDING(1, "待处理"),

    IN_PROGRESS(2, "处理中"),

    COMPLETED(3, "已完成"),

    CANCELLED(4, "已取消"),

    ;

    private final int code;

    private final String title;

    TaskStatusEnum(int code, String title) {
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
