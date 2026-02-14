package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.item.SelectItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class SelectItemService implements ItemService<SelectItemDef> {

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
        return "";
    }

    @Override
    public String getDbColumnType(SelectItemDef itemDef) {
        return "";
    }

    @Override
    public String getJavaType(SelectItemDef itemDef) {
        return "";
    }

    @Override
    public List<String> getJavaValidAnnotations(SelectItemDef itemDef) {
        return Collections.emptyList();
    }

    @Override
    public String getJavaJsonFormatAnnoatation(SelectItemDef itemDef) {
        return "";
    }

}
