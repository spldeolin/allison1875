package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.List;
import java.util.Optional;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * ItemService的主实现，作为分发器根据ItemDef的类型委托给具体的ItemService实现
 *
 * @author Deolin 2026-02-15
 */
@Singleton
public class PrimaryItemServiceImpl implements ItemService<ItemDef> {

    @Inject
    private MultiSelectItemService multiSelectItemService;

    @Inject
    private NumberItemService numberItemService;

    @Inject
    private OnOffItemService onOffItemService;

    @Inject
    private SecretItemService secretItemService;

    @Inject
    private SelectItemService selectItemService;

    @Inject
    private TextItemService textItemService;

    @Inject
    private TimeItemService timeItemService;

    @Override
    public ItemType supportedItemType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FilterPattern> getFilterPatterns(ItemDef itemDef) {
        return delegate(itemDef).getFilterPatterns(itemDef);
    }

    @Override
    public Boolean isSortable(ItemDef itemDef) {
        return delegate(itemDef).isSortable(itemDef);
    }

    @Override
    public String getDbColumnName(ItemDef itemDef) {
        return delegate(itemDef).getDbColumnName(itemDef);
    }

    @Override
    public String getDbColumnType(ItemDef itemDef) {
        return delegate(itemDef).getDbColumnType(itemDef);
    }

    @Override
    public String getJavaTypeInDTO(ItemDef itemDef) {
        return delegate(itemDef).getJavaTypeInDTO(itemDef);
    }

    @Override
    public List<AnnotationExpr> getJavaValidAnnotations(ItemDef itemDef) {
        return delegate(itemDef).getJavaValidAnnotations(itemDef);
    }

    @Override
    public Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(ItemDef itemDef) {
        return delegate(itemDef).getJavaJsonFormatAnnoatation(itemDef);
    }

    @SuppressWarnings("unchecked")
    private <I extends ItemDef> ItemService<I> delegate(ItemDef itemDef) {
        switch (itemDef.getType()) {
            case MULTI_SELECT:
                return (ItemService<I>) multiSelectItemService;
            case NUMBER:
                return (ItemService<I>) numberItemService;
            case ON_OFF:
                return (ItemService<I>) onOffItemService;
            case SECRET:
                return (ItemService<I>) secretItemService;
            case SELECT:
                return (ItemService<I>) selectItemService;
            case TEXT:
                return (ItemService<I>) textItemService;
            case TIME:
                return (ItemService<I>) timeItemService;
        }
        throw new IllegalStateException("unsupported item type");
    }

}
