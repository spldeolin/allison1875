package com.spldeolin.allison1875.docanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2025-01-24
 */
@Getter
@AllArgsConstructor
public enum ValueTypeEnum {

    /**
     * 字符串大类（含日期、时间等）
     */
    STRING("String"),

    /**
     * 整数大类
     */
    INTEGER("Integer"),

    /**
     * 小数大类
     */
    DECIMAL("Decimal"),

    /**
     * 布尔值大类
     */
    BOOLEAN("Boolean"),

    /**
     * 暂不支持分析，甚至可能不是一个Value Type
     */
    UNKNOWN("Unknown"),

    ;

    private final String title;

}
