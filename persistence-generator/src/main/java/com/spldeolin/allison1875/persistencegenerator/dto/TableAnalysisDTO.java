package com.spldeolin.allison1875.persistencegenerator.dto;

import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-07-12
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableAnalysisDTO {

    String tableName;

    String entityName;

    String mapperName;

    String descrption;

    /**
     * 主键字段（存在联合主键的可能）
     */
    List<PropertyDTO> idProperties = Lists.newArrayList();

    /**
     * 非主键字段
     */
    List<PropertyDTO> nonIdProperties = Lists.newArrayList();

    /**
     * 索引
     */
    List<IndexDTO> indices = Lists.newArrayList();

    /**
     * 所有字段
     */
    List<PropertyDTO> properties = Lists.newArrayList();

    /**
     * 存在逻辑删除标识符
     */
    Boolean isDeleteFlagExist = false;

    /**
     * 如果分析表结构时需要生成源码，使用这个属性传递
     */
    final List<FileFlush> flushes = Lists.newArrayList();

    /**
     * 所有属性均不能为null
     */
    Boolean isAllPropertiesNotNull;

    String lotNo;

}