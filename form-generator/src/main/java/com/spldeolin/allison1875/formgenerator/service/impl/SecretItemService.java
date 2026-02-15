package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.SecretItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class SecretItemService implements ItemService<SecretItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public ItemType supportedItemType() {
        return ItemType.SECRET;
    }

    @Override
    public List<FilterPattern> getFilterPatterns(SecretItemDef itemDef) {
        return null;
    }

    @Override
    public Boolean isSortable(SecretItemDef itemDef) {
        return false;
    }

    @Override
    public String getDbColumnName(SecretItemDef itemDef) {
        return MoreStringUtils.toLowerCamel(itemDef.getName());
    }

    @Override
    public String getDbColumnType(SecretItemDef itemDef) {
        return "VARCHAR(255)";
    }

    @Override
    public String getJavaTypeInDTO(SecretItemDef itemDef) {
        return "String";
    }

    @Override
    public List<String> getJavaValidAnnotations(SecretItemDef itemDef) {
        List<String> retval = Lists.newArrayList();
        if (itemDef.getIsNonValid()) {
            retval.add(annotationExprService.notEmpty().toString());
        }
        return retval;
    }

    @Override
    public String getJavaJsonFormatAnnoatation(SecretItemDef itemDef) {
        return null;
    }

}
