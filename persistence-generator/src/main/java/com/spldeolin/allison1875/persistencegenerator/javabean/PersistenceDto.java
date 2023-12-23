package com.spldeolin.allison1875.persistencegenerator.javabean;

import java.util.Collection;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-07-12
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersistenceDto {

    String tableName;

    String entityName;

    String mapperName;

    String descrption;

    /**
     * 主键字段（存在联合主键的可能）
     */
    Collection<PropertyDto> idProperties;

    /**
     * 非主键字段
     */
    Collection<PropertyDto> nonIdProperties;

    /**
     * 所有字段
     */
    Collection<PropertyDto> properties;

    /**
     * 逻辑外键字段（id结尾的字段算做逻辑外键）
     */
    Collection<PropertyDto> keyProperties;

    /**
     * 存在逻辑删除标识符
     */
    Boolean isDeleteFlagExist = false;

    LotNo lotNo;

}