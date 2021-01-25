package com.spldeolin.allison1875.persistencegenerator.javabean;

import lombok.Data;

/**
 * @author Deolin 2020-07-12
 */
@Data
public class PropertyDto {

    private String columnName;

    private String propertyName;

    private Class<?> javaType;

    private String description;

    private Long length;

    private Boolean notnull;

    private String defaultV;

}