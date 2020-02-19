package com.spldeolin.allison1875.da.view.rap;

import com.spldeolin.allison1875.da.core.enums.FieldTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-10-21
 */
@AllArgsConstructor
@Getter
public enum RapJsonType {

    NUMBER("number"),

    STRING("string"),

    BOOLEAN("boolean"),

    ARRAY_NUMBER("array<number>"),

    ARRAY_STRING("array<string>"),

    ARRAY_BOOLEAN("array<boolean>"),

    OBJECT("object"),

    ARRAY_OBJECT("array<object>");

    private String name;

    public static RapJsonType convert(FieldTypeEnum typeName) {
        switch (typeName) {
            case string:
                return STRING;
            case number:
                return NUMBER;
            case bool:
                return BOOLEAN;
            case object:
                return OBJECT;
            case stringArray:
                return ARRAY_STRING;
            case numberArray:
                return ARRAY_NUMBER;
            case booleanArray:
                return ARRAY_BOOLEAN;
            case objectArray:
                return ARRAY_OBJECT;
        }
        throw new IllegalArgumentException(typeName.getValue());
    }

}
