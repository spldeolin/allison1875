package com.spldeolin.allison1875.querytransformer.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2020-10-06
 */
@AllArgsConstructor
@Getter
public enum PredicateEnum {

    EQUALS("eq"),

    NOT_EQUALS("ne"),

    IN("in"),

    NOT_IN("nin"),

    GREATER_THEN("gt"),

    GREATER_OR_EQUALS("ge"),

    LESS_THEN("lt"),

    LESS_OR_EQUALS("le"),

    NOT_NULL("notnull"),

    IS_NULL("isnull"),

    LIKE("like"),

    DESC("desc"),

    ASC("asc"),

    ;

    private final String value;

    public static boolean isValid(String value) {
        return Arrays.stream(PredicateEnum.values()).anyMatch(one -> one.getValue().equals(value));
    }

    public static PredicateEnum of(String value) {
        return Arrays.stream(PredicateEnum.values()).filter(one -> one.getValue().equals(value)).findAny().orElse(null);
    }

}