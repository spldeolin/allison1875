package com.spldeolin.allison1875.formgenerator.service;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.service.impl.PrimaryItemServiceImpl;

/**
 * @author Deolin 2026-02-11
 */
@ImplementedBy(PrimaryItemServiceImpl.class)
public interface ItemService<I extends ItemDef> {

    /**
     * 该实现类支持的ItemType
     */
    ItemType supportedItemType();

    /**
     * 该类型字段支持的过滤方式，null或者empty代表不支持过滤
     */
    List<FilterPattern> getFilterPatterns(I itemDef);

    /**
     * 该类型字段是否可以排序
     */
    Boolean isSortable(I itemDef);

    /**
     * 字段对应的数据库列名
     */
    String getDbColumnName(I itemDef);

    /**
     * 字段对应的数据库字段类型
     */
    String getDbColumnType(I itemDef);

    /**
     * 字段对应在DTO中的Java类型
     */
    String getJavaTypeInDTO(I itemDef);

    /**
     * 字段对应的Java校验注解
     */
    List<AnnotationExpr> getJavaValidAnnotations(I itemDef);

    /**
     * 字段对应的Java @JsonFormat注解
     */
    Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(I itemDef);

}
