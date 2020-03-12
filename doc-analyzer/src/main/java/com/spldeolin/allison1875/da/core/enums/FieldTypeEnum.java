package com.spldeolin.allison1875.da.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum FieldTypeEnum {

    string("string"),

    number("number"),

    bool("boolean"),

    object("object"),

    stringArray("stringArray"),

    numberArray("numberArray"),

    booleanArray("booleanArray"),

    objectArray("objectArray"),

    file("file");

    private String value;

    public boolean isArrayLike() {
        return stringArray == this || numberArray == this || booleanArray == this || objectArray == this;
    }

    public boolean isNotNumberLike() {
        return numberArray != this && number != this;
    }

}
