package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.Collections;
import java.util.List;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.item.SecretItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class SecretItemService implements ItemService<SecretItemDef> {

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
        return "";
    }

    @Override
    public String getDbColumnType(SecretItemDef itemDef) {
        return "";
    }

    @Override
    public String getJavaType(SecretItemDef itemDef) {
        return "";
    }

    @Override
    public List<String> getJavaValidAnnotations(SecretItemDef itemDef) {
        return Collections.emptyList();
    }

    @Override
    public String getJavaJsonFormatAnnoatation(SecretItemDef itemDef) {
        return "";
    }

}
