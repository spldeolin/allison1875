package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class MultiSelectItemService implements ItemService<MultiSelectItemDef> {

    @Override
    public List<FilterPattern> getFilterPatterns(MultiSelectItemDef itemDef) {
        return Lists.newArrayList(IN);
    }

    @Override
    public Boolean isSortable(MultiSelectItemDef itemDef) {
        return false;
    }

    @Override
    public String getDbColumnName(MultiSelectItemDef itemDef) {
        return "";
    }

    @Override
    public String getDbColumnType(MultiSelectItemDef itemDef) {
        return "";
    }

    @Override
    public String getJavaType(MultiSelectItemDef itemDef) {
        return "";
    }

    @Override
    public List<String> getJavaValidAnnotations(MultiSelectItemDef itemDef) {
        return Collections.emptyList();
    }

    @Override
    public String getJavaJsonFormatAnnoatation(MultiSelectItemDef itemDef) {
        return "";
    }

}
