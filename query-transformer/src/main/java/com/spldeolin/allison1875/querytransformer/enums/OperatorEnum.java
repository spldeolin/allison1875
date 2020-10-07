package com.spldeolin.allison1875.querytransformer.enums;

import java.util.Arrays;

/**
 * @author Deolin 2020-10-06
 */
public enum OperatorEnum {

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

    ;

    private final String value;

    OperatorEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValid(String value) {
        return Arrays.stream(OperatorEnum.values()).anyMatch(one -> one.getValue().equals(value));
    }

    public static OperatorEnum of(String value) {
        return Arrays.stream(OperatorEnum.values()).filter(one -> one.getValue().equals(value)).findAny().orElse(null);
    }

}