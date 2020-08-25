package com.spldeolin.allison1875.persistencegenerator.javabean;

import java.util.Collection;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-07-12
 */
@Data
@Accessors(chain = true)
public class PersistenceDto {

    private String tableName;

    private String entityName;

    private String mapperName;

    private String descrption;

    /**
     * 主键字段（存在联合主键的可能）
     */
    private Collection<PropertyDto> idProperties;

    /**
     * 非主键字段
     */
    private Collection<PropertyDto> nonIdProperties;

    /**
     * 所有字段
     */
    private Collection<PropertyDto> properties;

    /**
     * 逻辑外键字段（id结尾的字段算做逻辑外键）
     */
    private Collection<PropertyDto> keyProperties;

    /**
     * 存在逻辑删除标识符
     */
    private Boolean isDeleteFlagExist = false;

}