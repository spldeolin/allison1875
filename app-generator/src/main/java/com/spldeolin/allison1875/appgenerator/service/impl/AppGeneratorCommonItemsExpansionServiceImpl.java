package com.spldeolin.allison1875.appgenerator.service.impl;

import java.util.List;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.service.CommonItemsExpansionService;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorCommonItemsExpansionServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
public class AppGeneratorCommonItemsExpansionServiceImpl implements CommonItemsExpansionService {

    @Override
    public void addCommonItems(List<FormDef> forms) {
        new FormGeneratorCommonItemsExpansionServiceImpl().addCommonItems(forms);

        for (FormDef form : forms) {
            TextItemDef createdBy = new TextItemDef();
            createdBy.setName("createdBy");
            createdBy.setTitle("创建人");
            createdBy.setIsNonVoid(true);
            createdBy.setCanInputOnInit(false);
            createdBy.setCanInputOnEdit(false);
            createdBy.setIsBuiltinField(true);
            createdBy.setMaxLength(32);
            form.getItems().add(createdBy);

            TextItemDef updatedBy = new TextItemDef();
            updatedBy.setName("updatedBy");
            updatedBy.setTitle("最近更新人");
            updatedBy.setIsNonVoid(true);
            updatedBy.setCanInputOnInit(false);
            updatedBy.setCanInputOnEdit(false);
            updatedBy.setIsBuiltinField(true);
            updatedBy.setMaxLength(32);
            form.getItems().add(updatedBy);
        }
    }

}
