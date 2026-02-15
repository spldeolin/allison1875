package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;

import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class MultiSelectItemService implements ItemService<MultiSelectItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private CommonConfig commonConfig;

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
        return MoreStringUtils.toLowerCamel(itemDef.getName());
    }

    @Override
    public String getDbColumnType(MultiSelectItemDef itemDef) {
        return "VARCHAR(64)";
    }

    @Override
    public String getJavaType(MultiSelectItemDef itemDef) {
        return commonConfig.getEnumPackage() + "." + MoreStringUtils.toUpperCamel(itemDef.getName()) + "Enum";
    }

    @Override
    public List<String> getJavaValidAnnotations(MultiSelectItemDef itemDef) {
        List<String> retval = Lists.newArrayList();
        if (itemDef.getIsNonValid()) {
            retval.add(annotationExprService.notEmpty().toString());
        }
        return retval;
    }

    @Override
    public String getJavaJsonFormatAnnoatation(MultiSelectItemDef itemDef) {
        return null;
    }

}
