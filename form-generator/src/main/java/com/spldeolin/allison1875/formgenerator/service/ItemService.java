package com.spldeolin.allison1875.formgenerator.service;

import java.util.List;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;

/**
 * @author Deolin 2026-02-11
 */
public interface ItemService<I extends ItemDef> {

    /**
     * 该类型字段支持的过滤方式，null或者empty代表不支持过滤
     */
    List<FilterPattern> getFilterPatterns(I itemDef);

    /**
     * 该类型字段是否可以排序
     */
    Boolean isSortable(I itemDef);

    String getDbColumnName(I itemDef);

    String getDbColumnType(I itemDef);

    String getJavaType(I itemDef);

    List<String> getJavaValidAnnotations(I itemDef);

    String getJavaJsonFormatAnnoatation(I itemDef);

}
