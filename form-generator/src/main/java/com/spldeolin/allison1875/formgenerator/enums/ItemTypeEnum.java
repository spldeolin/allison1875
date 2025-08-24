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
public enum ItemTypeEnum {

    WHOLE_NUMBER("wholeNumber", "BIGINT"),

    DECIMAL_NUMBER("decimalNumber", "DECIMAL"),

    PLAIN_TEXT("plainText", "VARCHAR(1024)"),

    RICH_TEXT("richText", "TEXT"),

    GENERAL_DATE_TIME("generalDateTime", "DATETIME"),

    SINGLE_SELECTION("singleSelection", "VARCHAR(512)"),

    MULTIPLE_SELECTION("multipleSelection", "VARCHAR(512)"),

    BOOLEAN("boolean", "TINYINT(1)"),

    ;

    @JsonValue
    private final String code;

    private final String ddlColumnType;

    @JsonCreator
    public static ItemTypeEnum of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

}
