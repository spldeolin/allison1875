package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.SelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-11
 */
@Singleton
public class MultiSelectItemService implements ItemService<MultiSelectItemDef> {

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private Config config;

    @Override
    public ItemType supportedItemType() {
        return ItemType.MULTI_SELECT;
    }

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
        return MoreStringUtils.camelToSnakeCase(itemDef.getName());
    }

    @Override
    public String getDbColumnType(MultiSelectItemDef itemDef) {
        return "VARCHAR(64)";
    }

    @Override
    public String getJavaTypeInDTO(MultiSelectItemDef itemDef) {
        return "java.util.List<" + DomainContext.get().getEnumPackage() + "." + MoreStringUtils.toUpperCamel(
                itemDef.getName())
                + "Enum" + ">";
    }

    @Override
    public List<AnnotationExpr> getJavaValidAnnotations(MultiSelectItemDef itemDef) {
        List<AnnotationExpr> retval = Lists.newArrayList();
        if (itemDef.getIsNonVoid()) {
            retval.add(annotationExprService.notEmpty());
        }
        return retval;
    }

    @Override
    public Optional<AnnotationExpr> getJavaJsonFormatAnnoatation(MultiSelectItemDef itemDef) {
        return Optional.empty();
    }

    @Override
    public String getTodoValue(MultiSelectItemDef itemDef) {
        return "null";
    }

    public FormDef toAssociationForm(FormDef majorForm, MultiSelectItemDef item) {
        List<ItemDef> items = Lists.newArrayList();
        items.add(majorForm.getItems().get(0)); // 主表单的业务主键

        SelectItemDef code = SelectItemDef.builder()
                .name(item.getName())
                .title(item.getTitle())
                .isNonVoid(item.getIsNonVoid())
                .canInputOnInit(false)
                .canInputOnEdit(false)
                .options(item.getOptions())
                .build();
        items.add(code);

        TimeItemDef createdAt = TimeItemDef.builder()
                .name("createdAt")
                .title("创建时间")
                .isNonVoid(true)
                .canInputOnInit(false)
                .canInputOnEdit(false)
                .build();
        items.add(createdAt);

        if (hasCreatedBy(majorForm)) {
            TextItemDef createdBy = TextItemDef.builder()
                    .name("createdBy")
                    .title("创建人")
                    .isNonVoid(true)
                    .canInputOnInit(false)
                    .canInputOnEdit(false)
                    .isBuiltinField(true)
                    .maxLength(32)
                    .build();
            items.add(createdBy);
        }

        IndexDef index1 = IndexDef.builder()
                .itemNames(Lists.newArrayList(majorForm.getItems().get(0).getName(), item.getName()))
                .isUnique(true)
                .build();
        IndexDef index2 = IndexDef.builder()
                .itemNames(Lists.newArrayList(item.getName(), majorForm.getItems().get(0).getName()))
                .isUnique(true)
                .build();

        return FormDef.builder()
                .name(majorForm.getName() + StringUtils.capitalize(item.getName()))
                .title(majorForm.getTitle() + "的" + item.getTitle())
                .items(items)
                .indices(Lists.newArrayList(index1, index2))
                .build();
    }

    private boolean hasCreatedBy(FormDef form) {
        return form.getItems().stream().anyMatch(item -> "createdBy".equals(item.getName()));
    }

}
