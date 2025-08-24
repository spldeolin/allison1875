package com.spldeolin.allison1875.formgenerator.enums;

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
public enum ItemValidEnum {

    NOT_NULL_BROADLY("notNullBroadly"),

    MAX_LENGTH("maxLength"),

    MIN_LENGTH("minLength"),

    PATTERN("pattern"),

    ;

    @JsonValue
    private final String code;

    @JsonCreator
    public static ItemValidEnum of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }
}
