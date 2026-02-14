package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.LIKE;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class TextItemService implements ItemService<TextItemDef> {

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
        return "";
    }

    @Override
    public String getDbColumnType(TextItemDef itemDef) {
        return "";
    }

    @Override
    public String getJavaType(TextItemDef itemDef) {
        return "";
    }

    @Override
    public List<String> getJavaValidAnnotations(TextItemDef itemDef) {
        return Collections.emptyList();
    }

    @Override
    public String getJavaJsonFormatAnnoatation(TextItemDef itemDef) {
        return "";
    }

}
