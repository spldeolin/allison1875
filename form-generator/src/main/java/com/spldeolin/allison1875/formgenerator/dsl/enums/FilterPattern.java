package com.spldeolin.allison1875.formgenerator.dsl.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文本类字段作为过滤条件时，支持的过滤方式
 *
 * @author Deolin 2025-08-12
 */
@Getter
@AllArgsConstructor
public enum FilterPattern {

    /**
     * 在…范围内 或者 与…有交集，只有单个元素时等价于“等于”
     */
    IN("in"),

    /**
     * 大于等于…
     */
    GE("ge"),

    /**
     * 小于等于…
     */
    GT("gt"),

    /**
     * 小于等于…
     */
    LE("le"),

    /**
     * 小于…
     */
    LT("lt"),

    /**
     * 左右模糊匹配
     */
    LIKE("like"),

    /**
     * 在开始日期…和结束日期…范围内
     */
    DATE_RANGE("dateRange"),

    /**
     * 在开始时间…和结束时间…范围内
     */
    DATE_TIME_RANGE("dateTimeRange"),

    ;

    @JsonValue
    private final String code;

    @JsonCreator
    public static FilterPattern of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

}
