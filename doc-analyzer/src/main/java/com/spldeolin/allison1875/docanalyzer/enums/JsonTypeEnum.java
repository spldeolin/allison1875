package com.spldeolin.allison1875.docanalyzer.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum JsonTypeEnum {

    STRING("string"),

    NUMBER("number"),

    BOOLEAN("boolean"),

    OBJECT("object"),

    STRING_ARRAY("stringArray"),

    NUMBER_ARRAY("numberArray"),

    BOOLEAN_ARRAY("booleanArray"),

    OBJECT_ARRAY("objectArray"),

    RECURSION("recursion"),

    RECURSION_ARRAY("recursionArray"),

    FILE("file"),

    UNKNOWN("unknown");

    private final String value;

    public boolean isArrayLike() {
        return STRING_ARRAY == this || NUMBER_ARRAY == this || BOOLEAN_ARRAY == this || OBJECT_ARRAY == this;
    }

    public boolean isNotNumberLike() {
        return NUMBER_ARRAY != this && NUMBER != this;
    }

    public boolean isObjectLike() {
        return OBJECT == this || OBJECT_ARRAY == this || RECURSION == this || RECURSION_ARRAY == this
                || UNKNOWN == this;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}
