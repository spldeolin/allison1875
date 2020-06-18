package com.spldeolin.allison1875.docanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum ValidatorTypeEnum {

    NOT_BLANK("必须有非空格字符"),

    NOT_EMPTY("必须有元素/字符"),

    MAX_SIZE("最大长度/容量："),

    MIN_SIZE("最小长度/容量："),

    MAX_NUMBER("最大值："),

    MIN_NUMBER("最小值："),

    VALIDATOR_TYPE_ENUM("必须是未来的时间"),

    PAST("必须是过去的时间"),

    MAX_INTEGRAL_DIGITS("最大整数位数："),

    MAX_FRACTIONAL_DIGITS("最大小数位数："),

    POSITIVE("必须是正数"),

    NEGATIVE("必须是小数"),

    REGEX("正则表达式：");

    private final String value;

}
