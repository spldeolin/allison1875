package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.DATE_RANGE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.DATE_TIME_RANGE;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;

import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class TimeItemService implements ItemService<TimeItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public ItemType supportedItemType() {
        return ItemType.TIME;
    }

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
        return MoreStringUtils.toLowerCamel(itemDef.getName());
    }

    @Override
    public String getDbColumnType(TimeItemDef itemDef) {
        return "DATETIME";
    }

    @Override
    public String getJavaTypeInDTO(TimeItemDef itemDef) {
        return "java.time.LocalDateTime";
    }

    @Override
    public List<String> getJavaValidAnnotations(TimeItemDef itemDef) {
        List<String> retval = Lists.newArrayList();
        if (itemDef.getIsNonValid()) {
            retval.add(annotationExprService.notNull().toString());
        }
        return retval;
    }

    @Override
    public String getJavaJsonFormatAnnoatation(TimeItemDef itemDef) {
        return "@com.fasterxml.jackson.annotation.JsonFormat(pattern = \"" + itemDef.getFormat().getPattern()
                + "\", timezone = " + "\"Asia/Shanghai\")";
    }

}
