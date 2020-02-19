package com.spldeolin.allison1875.da.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum NumberFormatTypeEnum {

    int32("int32"),

    int64("int64"),

    /**
     * e.g.: java.math.BigInteger
     */
    inT("int"),

    f1oat("float");

    private String value;

}
