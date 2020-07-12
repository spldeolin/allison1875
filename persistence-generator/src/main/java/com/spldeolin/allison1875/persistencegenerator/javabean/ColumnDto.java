package com.spldeolin.allison1875.persistencegenerator.javabean;

import lombok.Data;

/**
 * @author Deolin 2020-07-11
 */
@Data
public class ColumnDto {

    private String tableName;

    private String columnName;

    private Boolean isNullable;

    private String dataType;

    private Long charMaxLength;

    private String columnType;

    private String columnComment;

}