package com.spldeolin.allison1875.persistencegenerator.javabean;

import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-07-12
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableStructureAnalysisDto {

    String tableName;

    String entityName;

    String mapperName;

    String descrption;

    /**
     * 主键字段（存在联合主键的可能）
     */
    List<PropertyDto> idProperties;

    /**
     * 非主键字段
     */
    List<PropertyDto> nonIdProperties;

    /**
     * 所有字段
     */
    List<PropertyDto> properties;

    /**
     * 逻辑外键字段（id结尾的字段算做逻辑外键）
     */
    List<PropertyDto> keyProperties;

    /**
     * 存在逻辑删除标识符
     */
    Boolean isDeleteFlagExist = false;

    /**
     * 如果分析表结构时需要生成源码，使用这个属性传递
     */
    final List<FileFlush> flushes = Lists.newArrayList();

    String lotNo;

}