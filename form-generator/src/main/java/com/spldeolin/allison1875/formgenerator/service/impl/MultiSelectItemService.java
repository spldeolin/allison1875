package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern.IN;
import static com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern.DO_NOT;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.FilterPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.SelectItemDef;
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
    private CommonConfig commonConfig;

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
        return "java.util.List<" + commonConfig.getEnumPackage() + "." + MoreStringUtils.toUpperCamel(itemDef.getName())
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
        FormDef form = new FormDef();
        form.setName(majorForm.getName() + StringUtils.capitalize(item.getName()));
        form.setTitle(majorForm.getTitle() + "的" + item.getTitle());
        form.setDesc(null);
        List<ItemDef> items = Lists.newArrayList();
        items.add(majorForm.getItems().get(0)); // 主表单的业务主键
        SelectItemDef code = new SelectItemDef();
        code.setName(item.getName());
        code.setTitle(item.getTitle());
        code.setIsNonVoid(item.getIsNonVoid());
        code.setInitPattern(DO_NOT); // 非主表单，init和edit没有意义
        code.setEditPattern(DO_NOT);
        code.setOptions(item.getOptions());
        items.add(code);
        TimeItemDef createdAt = new TimeItemDef();
        createdAt.setName("createdAt");
        createdAt.setTitle("创建时间");
        createdAt.setIsNonVoid(true);
        createdAt.setInitPattern(DO_NOT);
        createdAt.setEditPattern(DO_NOT);
        items.add(createdAt);
        form.setItems(items);
        List<IndexDef> indices = Lists.newArrayList();
        IndexDef index = new IndexDef();
        index.setItemNames(Lists.newArrayList(majorForm.getItems().get(0).getName(), item.getName()));
        index.setIsUnique(true);
        form.setIndices(indices);
        return form;
    }

}
