package com.spldeolin.allison1875.persistencegenerator.javabean;

import lombok.Data;

/**
 * @author Deolin 2020-07-11
 */
@Data
public class InformationSchemaDto {

    private String tableName;

    private String tableComment;

    private String columnName;

    private String dataType;

    private String columnType;

    private String columnComment;

    private String columnKey;

    private Long characterMaximumLength;

    private String isNullable; // YES NO

    private String columnDefault;

}