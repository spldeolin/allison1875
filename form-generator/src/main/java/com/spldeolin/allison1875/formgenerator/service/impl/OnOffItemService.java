package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.item.OnOffItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class OnOffItemService implements ItemService<OnOffItemDef> {

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
        return "";
    }

    @Override
    public String getDbColumnType(OnOffItemDef itemDef) {
        return "";
    }

    @Override
    public String getJavaType(OnOffItemDef itemDef) {
        return "";
    }

    @Override
    public List<String> getJavaValidAnnotations(OnOffItemDef itemDef) {
        return Collections.emptyList();
    }

    @Override
    public String getJavaJsonFormatAnnoatation(OnOffItemDef itemDef) {
        return "";
    }

}
