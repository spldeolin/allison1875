package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.CommonItemsExpansionService;

/**
 * @author Deolin 2026-06-23
 */
public class FormGeneratorCommonItemsExpansionServiceImpl implements CommonItemsExpansionService {

    @Override
    public List<FormDef> addCommonItems(List<FormDef> forms) {
        List<FormDef> result = new ArrayList<>();
        for (FormDef form : forms) {
            TextItemDef bizId = TextItemDef.builder()
                    .name(StringUtils.uncapitalize(form.getName()) + "Code")
                    .title("业务主键")
                    .isNonVoid(true)
                    .canInputOnInit(false)
                    .canInputOnEdit(false)
                    .isBuiltinField(true)
                    .maxLength(36)
                    .build();

            TimeItemDef createdAt = TimeItemDef.builder()
                    .name("createdAt")
                    .title("创建时间")
                    .isNonVoid(true)
                    .canInputOnInit(false)
                    .canInputOnEdit(false)
                    .isBuiltinField(true)
                    .build();

            TimeItemDef updatedAt = TimeItemDef.builder()
                    .name("updatedAt")
                    .title("更新时间")
                    .isNonVoid(true)
                    .canInputOnInit(false)
                    .canInputOnEdit(false)
                    .isBuiltinField(true)
                    .build();

            List<com.spldeolin.allison1875.formgenerator.dsl.ItemDef> items = new ArrayList<>();
            items.add(bizId);
            if (form.getItems() != null) {
                items.addAll(form.getItems());
            }
            items.add(createdAt);
            items.add(updatedAt);

            IndexDef bizIdIndex = IndexDef.builder()
                    .itemNames(Lists.newArrayList(bizId.getName()))
                    .isUnique(true)
                    .build();

            List<IndexDef> indices = new ArrayList<>();
            indices.add(bizIdIndex);
            if (form.getIndices() != null) {
                indices.addAll(form.getIndices());
            }

            result.add(form.toBuilder().items(items).indices(indices).build());
        }
        return result;
    }

}
