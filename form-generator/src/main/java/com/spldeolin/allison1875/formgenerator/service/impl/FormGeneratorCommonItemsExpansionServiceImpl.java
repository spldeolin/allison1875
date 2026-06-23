package com.spldeolin.allison1875.formgenerator.service.impl;

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
    public void addCommonItems(List<FormDef> forms) {
        for (FormDef form : forms) {
            TextItemDef bizId = new TextItemDef();
            bizId.setName(StringUtils.uncapitalize(form.getName()) + "Code");
            bizId.setTitle("业务主键");
            bizId.setIsNonVoid(true);
            bizId.setCanInputOnInit(false);
            bizId.setCanInputOnEdit(false);
            bizId.setIsBuiltinField(true);
            bizId.setMaxLength(36);
            form.getItems().add(0, bizId);

            TimeItemDef createdAt = new TimeItemDef();
            createdAt.setName("createdAt");
            createdAt.setTitle("创建时间");
            createdAt.setIsNonVoid(true);
            createdAt.setCanInputOnInit(false);
            createdAt.setCanInputOnEdit(false);
            createdAt.setIsBuiltinField(true);
            form.getItems().add(createdAt);

            TimeItemDef updatedAt = new TimeItemDef();
            updatedAt.setName("updatedAt");
            updatedAt.setTitle("更新时间");
            updatedAt.setIsNonVoid(true);
            updatedAt.setCanInputOnInit(false);
            updatedAt.setCanInputOnEdit(false);
            updatedAt.setIsBuiltinField(true);
            form.getItems().add(updatedAt);

            IndexDef index = new IndexDef();
            index.setItemNames(Lists.newArrayList(bizId.getName()));
            index.setIsUnique(true);
            if (form.getIndices() == null) {
                form.setIndices(Lists.newArrayList());
            }
            form.getIndices().addFirst(index);
        }
    }

}
