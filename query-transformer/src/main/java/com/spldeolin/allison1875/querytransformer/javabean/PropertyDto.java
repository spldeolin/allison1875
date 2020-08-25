package com.spldeolin.allison1875.querytransformer.javabean;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-08-12
 */
@Data
@Accessors(fluent = true)
public class PropertyDto {

    private String propertyName;

    private String columnName;

    private String varName;

    private String dollarVar;

    private String operator;

}