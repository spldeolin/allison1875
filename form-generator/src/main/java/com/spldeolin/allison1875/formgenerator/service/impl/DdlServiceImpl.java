package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.service.DdlService;
import com.spldeolin.allison1875.formgenerator.service.ItemService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-02-15
 */
@Singleton
@Slf4j
public class DdlServiceImpl implements DdlService {

    @Inject
    private ItemService<ItemDef> itemService;

    @Inject
    private MultiSelectItemService multiSelectItemService;

    @Override
    public String generateDdl(List<FormDef> forms) {
        StringBuilder ddl = new StringBuilder(512);
        List<FormDef> associationForms = Lists.newArrayList();
        for (FormDef form : forms) {
            associationForms.addAll(generateDdlForForm(form, ddl));
        }
        for (FormDef form : associationForms) {
            generateDdlForForm(form, ddl);
        }
        return ddl.toString();
    }

    private List<FormDef> generateDdlForForm(FormDef form, StringBuilder ddl) {
        List<FormDef> associationForms = Lists.newArrayList();
        String tableName = MoreStringUtils.camelToSnakeCase(form.getName());
        ddl.append("CREATE TABLE `").append(tableName).append("`\n(");
        ddl.append("`id` BIGINT NOT NULL COMMENT '主键',\n");
        for (ItemDef item : form.getItems()) {
            if (item.getType() == ItemType.MULTI_SELECT) {
                log.info("未多选创建关联表单, item: {}", item.getName());
                // 创建关联表单
                associationForms.add(multiSelectItemService.toAssociationForm(form, (MultiSelectItemDef) item));
                continue;
            }
            ddl.append("`").append(itemService.getDbColumnName(item)).append("` ")
                    .append(itemService.getDbColumnType(item));
            if (item.getIsNonVoid()) {
                ddl.append(" NOT NULL");
            }
            ddl.append(" COMMENT '").append(item.getTitle()).append("',\n");
        }
        ddl.append("PRIMARY KEY (`id`)\n");
        ddl.append(") COMMENT '").append(form.getTitle()).append("'").append(";\n\n");
        return associationForms;
    }

}
