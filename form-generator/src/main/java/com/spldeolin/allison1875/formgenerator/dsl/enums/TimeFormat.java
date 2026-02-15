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
public enum TimeFormat {

    /**
     * yyyy-MM-dd形式的日期
     */
    DATE("date", "yyyy-MM-dd", "java.time.LocalDate"),

    /**
     * HH:mm:ss形式的时间
     */
    TIME("time", "HH:mm:ss", "java.time.LocalTime"),

    /**
     * yyyy-MM-dd HH:mm:ss形式的日期时间
     */
    DATE_TIME("dateTime", "yyyy-MM-dd HH:mm:ss", "java.time.LocalDateTime"),

    ;

    @JsonValue
    private final String code;

    private final String pattern;

    private final String javaType;

    @JsonCreator
    public static TimeFormat of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

}
