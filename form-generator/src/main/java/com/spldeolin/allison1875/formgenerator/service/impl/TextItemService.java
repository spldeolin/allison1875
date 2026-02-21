package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.LIKE;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class TextItemService implements ItemService<TextItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public ItemType supportedItemType() {
        return ItemType.TEXT;
    }

    @Override
    public List<FilterPattern> getFilterPatterns(TextItemDef itemDef) {
        return Lists.newArrayList(IN, LIKE);
    }

    @Override
    public Boolean isSortable(TextItemDef itemDef) {
        return true;
    }

    @Override
    public String getDbColumnName(TextItemDef itemDef) {
        return MoreStringUtils.camelToSnakeCase(itemDef.getName());
    }

    @Override
    public String getDbColumnType(TextItemDef itemDef) {
        return !itemDef.getIsMultilineOrRich() ? "VARCHAR(" + itemDef.getMaxLength() + ")" : "LONGTEXT";
    }

    @Override
    public String getJavaTypeInDTO(TextItemDef itemDef) {
        return "String";
    }

    @Override
    public List<AnnotationExpr> getJavaValidAnnotations(TextItemDef itemDef) {
        List<AnnotationExpr> retval = Lists.newArrayList();
        if (itemDef.getIsNonVoid()) {
            retval.add(annotationExprService.notBlank());
        }
        retval.add(annotationExprService.size(0, itemDef.getMaxLength()));
        return retval;
    }

    @Override
    public Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(TextItemDef itemDef) {
        return Optional.empty();
    }

    @Override
    public String getTodoValue(TextItemDef itemDef) {
        return "\"\"";
    }

}
