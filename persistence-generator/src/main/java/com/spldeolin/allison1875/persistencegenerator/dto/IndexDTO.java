package com.spldeolin.allison1875.persistencegenerator.dto;

import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2025-09-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IndexDTO {

    /**
     * 索引名
     */
    String indexName;

    /**
     * 索引字段
     */
    List<PropertyDTO> properties = Lists.newArrayList();

    /**
     * 是否唯一
     */
    Boolean isUnique;

}