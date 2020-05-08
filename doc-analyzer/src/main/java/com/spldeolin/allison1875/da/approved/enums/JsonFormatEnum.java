package com.spldeolin.allison1875.da.approved.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum JsonFormatEnum {

    INT_32("int32"),

    INT_64("int64"),

    /**
     * 未知位数的整数
     */
    INT_UNKNOWN("int"),

    FLOAT("float"),

    /**
     * "时间"类
     */
    TIME("datetime(%s)"),

    ENUM("enum(%s)"),

    /**
     * 不是上述中的任何一种
     */
    NOTHING_SPECIAL("");

    private final String value;

}
