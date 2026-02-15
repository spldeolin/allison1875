package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;

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
import com.spldeolin.allison1875.formgenerator.dsl.item.OnOffItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class OnOffItemService implements ItemService<OnOffItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public ItemType supportedItemType() {
        return ItemType.ON_OFF;
    }

    @Override
    public List<FilterPattern> getFilterPatterns(OnOffItemDef itemDef) {
        return Lists.newArrayList(IN);
    }

    @Override
    public Boolean isSortable(OnOffItemDef itemDef) {
        return false;
    }

    @Override
    public String getDbColumnName(OnOffItemDef itemDef) {
        return MoreStringUtils.camelToSnakeCase(itemDef.getName());
    }

    @Override
    public String getDbColumnType(OnOffItemDef itemDef) {
        return "TINYINT(1)";
    }

    @Override
    public String getJavaTypeInDTO(OnOffItemDef itemDef) {
        return "Boolean";
    }

    @Override
    public List<AnnotationExpr> getJavaValidAnnotations(OnOffItemDef itemDef) {
        List<AnnotationExpr> retval = Lists.newArrayList();
        if (itemDef.getIsNonVoid()) {
            retval.add(annotationExprService.notNull());
        }
        return retval;
    }

    @Override
    public Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(OnOffItemDef itemDef) {
        return Optional.empty();
    }

}
