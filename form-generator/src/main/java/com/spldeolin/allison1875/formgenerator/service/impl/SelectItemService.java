package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.SelectItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class SelectItemService implements ItemService<SelectItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private CommonConfig commonConfig;

    @Override
    public ItemType supportedItemType() {
        return ItemType.SELECT;
    }

    @Override
    public List<FilterPattern> getFilterPatterns(SelectItemDef itemDef) {
        return Lists.newArrayList(IN);
    }

    @Override
    public Boolean isSortable(SelectItemDef itemDef) {
        return false;
    }

    @Override
    public String getDbColumnName(SelectItemDef itemDef) {
        return MoreStringUtils.camelToSnakeCase(itemDef.getName());
    }

    @Override
    public String getDbColumnType(SelectItemDef itemDef) {
        return "VARCHAR(64)";
    }

    @Override
    public String getJavaTypeInDTO(SelectItemDef itemDef) {
        return commonConfig.getEnumPackage() + "." + MoreStringUtils.toUpperCamel(itemDef.getName()) + "Enum";
    }

    @Override
    public List<AnnotationExpr> getJavaValidAnnotations(SelectItemDef itemDef) {
        List<AnnotationExpr> retval = Lists.newArrayList();
        if (itemDef.getIsNonVoid()) {
            retval.add(annotationExprService.notNull());
        }
        return retval;
    }

    @Override
    public Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(SelectItemDef itemDef) {
        return Optional.empty();
    }

}
