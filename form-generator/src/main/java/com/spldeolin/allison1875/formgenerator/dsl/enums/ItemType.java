package com.spldeolin.allison1875.formgenerator.dsl.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2025-08-12
 */
@Getter
@AllArgsConstructor
public enum ItemType {

    /**
     * 数字类字段定义
     */
    NUMBER("number", , ),

    /**
     * 开关类字段定义，只有True、False的选择类字段定义
     */
    ON_OFF("onOff", , ),

    /**
     * 密码、密钥类字段定义（特点是不能返回，DB中需要被加密保存，不能编辑，只能重置）
     */
    SECRET("secret", , ),

    /**
     * 选择类字段定义
     */
    SELECT("select", , ),

    /**
     * 多选类字段定义
     */
    MULTI_SELECT("multiSelect", , ),// 多选类很特殊，为了能过滤需要创建关联表

    /**
     * 文本类字段定义
     */
    TEXT("text", , ),

    /**
     * 时间类字段定义
     */
    TIME("time", , ),

    ;

    @JsonValue
    private final String code;

    @JsonCreator
    public static ItemType of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

}
