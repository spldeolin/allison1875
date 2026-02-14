package com.spldeolin.allison1875.formgenerator.dsl.enums;

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
public enum InitOrEditPattern {

    /**
     * 不进行初始化或者不可编辑
     */
    DO_NOT("doNot"),

    /**
     * 用户输入
     */
    USER_INPUT("userInput"),

    /**
     * 生成为T0DO，由开发者自行开发
     */
    TODO("todo"),

    ;

    @JsonValue
    private final String code;


    @JsonCreator
    public static InitOrEditPattern of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

}
