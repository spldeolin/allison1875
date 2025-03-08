package com.spldeolin.allison1875.docanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JSON整数类型的细类
 *
 * @author Deolin 2025-01-24
 */
@Getter
@AllArgsConstructor
public enum JsonIntegerTypeEnum {

    JAVA_LONG("Int(64)"),

    JAVA_INTEGER("Int(32)"),

    JAVA_SHORT("Int(16)"),

    JAVA_BYTE("Int(8)"),

    OTHERS("Int(64)");

    private final String title;

}
