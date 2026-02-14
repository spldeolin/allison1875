package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.GE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.GT;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.LE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.LT;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.item.NumberItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class NumberItemService implements ItemService<NumberItemDef> {

    @Override
    public List<FilterPattern> getFilterPatterns(NumberItemDef itemDef) {
        return Lists.newArrayList(IN, GE, GT, LE, LT);
    }

    @Override
    public Boolean isSortable(NumberItemDef itemDef) {
        return true;
    }

    @Override
    public String getDbColumnName(NumberItemDef itemDef) {
        return "";
    }

    @Override
    public String getDbColumnType(NumberItemDef itemDef) {
        return "";
    }

    @Override
    public String getJavaType(NumberItemDef itemDef) {
        return "";
    }

    @Override
    public List<String> getJavaValidAnnotations(NumberItemDef itemDef) {
        return Collections.emptyList();
    }

    @Override
    public String getJavaJsonFormatAnnoatation(NumberItemDef itemDef) {
        return "";
    }

}
