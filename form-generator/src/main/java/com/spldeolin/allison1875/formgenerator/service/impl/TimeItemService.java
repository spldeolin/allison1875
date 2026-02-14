package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.DATE_RANGE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.DATE_TIME_RANGE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class TimeItemService implements ItemService<TimeItemDef> {

    @Override
    public List<FilterPattern> getFilterPatterns(TimeItemDef itemDef) {
        return Lists.newArrayList(IN, DATE_RANGE, DATE_TIME_RANGE);
    }

    @Override
    public Boolean isSortable(TimeItemDef itemDef) {
        return true;
    }

    @Override
    public String getDbColumnName(TimeItemDef itemDef) {
        return "";
    }

    @Override
    public String getDbColumnType(TimeItemDef itemDef) {
        return "";
    }

    @Override
    public String getJavaType(TimeItemDef itemDef) {
        return "";
    }

    @Override
    public List<String> getJavaValidAnnotations(TimeItemDef itemDef) {
        return Collections.emptyList();
    }

    @Override
    public String getJavaJsonFormatAnnoatation(TimeItemDef itemDef) {
        return "";
    }

}
