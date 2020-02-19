package com.spldeolin.allison1875.da.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum StringFormatTypeEnum {

    normal("normal"),

    datetime("datetime(%s)");

    private String value;

}
