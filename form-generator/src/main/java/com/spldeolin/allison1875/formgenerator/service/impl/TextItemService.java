package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.LIKE;

import java.util.List;
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
        return MoreStringUtils.toLowerCamel(itemDef.getName());
    }

    @Override
    public String getDbColumnType(TextItemDef itemDef) {
        return !itemDef.getIsMultilineOrRich() ? "VARCHAR(" + itemDef.getMaxLength() + ")" : "LONGTXT";
    }

    @Override
    public String getJavaTypeInDTO(TextItemDef itemDef) {
        return "String";
    }

    @Override
    public List<String> getJavaValidAnnotations(TextItemDef itemDef) {
        List<String> retval = Lists.newArrayList();
        if (itemDef.getIsNonValid()) {
            retval.add(annotationExprService.notBlank().toString());
        }
        return retval;
    }

    @Override
    public String getJavaJsonFormatAnnoatation(TextItemDef itemDef) {
        return null;
    }

}
