package com.spldeolin.allison1875.appgenerator.service.impl;

import java.util.ArrayList;
import java.util.List;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.service.CommonItemsExpansionService;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorCommonItemsExpansionServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
public class AppGeneratorCommonItemsExpansionServiceImpl implements CommonItemsExpansionService {

    @Override
    public List<FormDef> addCommonItems(List<FormDef> forms) {
        List<FormDef> result = new FormGeneratorCommonItemsExpansionServiceImpl().addCommonItems(forms);

        List<FormDef> finalResult = new ArrayList<>();
        for (FormDef form : result) {
            TextItemDef createdBy = TextItemDef.builder()
                    .name("createdBy")
                    .title("创建人")
                    .isNonVoid(true)
                    .canInputOnInit(false)
                    .canInputOnEdit(false)
                    .isBuiltinField(true)
                    .maxLength(32)
                    .build();

            TextItemDef updatedBy = TextItemDef.builder()
                    .name("updatedBy")
                    .title("最近更新人")
                    .isNonVoid(true)
                    .canInputOnInit(false)
                    .canInputOnEdit(false)
                    .isBuiltinField(true)
                    .maxLength(32)
                    .build();

            List<ItemDef> items = new ArrayList<>(form.getItems());
            items.add(createdBy);
            items.add(updatedBy);

            finalResult.add(form.toBuilder().items(items).build());
        }
        return finalResult;
    }

}
