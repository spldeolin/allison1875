package com.spldeolin.allison1875.formgenerator.dsl.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2026-02-15
 */
@Getter
@AllArgsConstructor
public enum ApiType {

    CREATE("create"),

    UPDATE("update"),

    LIST("list"),

    GET_DETAIL("getDetail"),

    DELETE("delete"),

    ;

    @JsonValue
    private final String code;

    @JsonCreator
    public static ApiType of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }
}
