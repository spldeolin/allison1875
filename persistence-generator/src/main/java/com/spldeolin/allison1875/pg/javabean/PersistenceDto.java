package com.spldeolin.allison1875.pg.javabean;

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
    private Collection<PropertyDto> pkProperties;

    /**
     * 非主键字段
     */
    private Collection<PropertyDto> nonPkProperties;

    /**
     * 所有字段
     */
    private Collection<PropertyDto> properties;

    /**
     * 逻辑外键字段（id结尾的字段算做逻辑外键）
     */
    private Collection<PropertyDto> fkProperties;

}