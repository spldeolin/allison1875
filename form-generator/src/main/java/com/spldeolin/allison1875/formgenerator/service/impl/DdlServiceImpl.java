package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.List;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.service.DdlService;
import com.spldeolin.allison1875.formgenerator.service.ItemService;

/**
 * @author Deolin 2026-02-15
 */
@Singleton
public class DdlServiceImpl implements DdlService {

    @Inject
    private ItemService<ItemDef> itemService;

    @Override
    public String generateDdl(List<FormDef> forms) {
        StringBuilder ddl = new StringBuilder(512);
        for (FormDef form : forms) {
            String tableName = camelToSnakeCase(form.getName());
            ddl.append("CREATE TABLE `").append(tableName).append("`\n(");
            ddl.append("`id` BIGINT NOT NULL COMMENT '主键',\n");
            for (ItemDef item : form.getItems()) {
                ddl.append("`").append(itemService.getDbColumnName(item)).append("` ")
                        .append(itemService.getDbColumnType(item));
                if (item.getIsNonVoid()) {
                    ddl.append(" NOT NULL");
                }
                ddl.append(" COMMENT '").append(item.getTitle()).append("',\n");
            }
            ddl.append("PRIMARY KEY (`id`)\n");
            ddl.append(") COMMENT '").append(form.getTitle()).append("'").append(";\n\n");
        }
        return ddl.toString();
    }

    private String camelToSnakeCase(String camelStr) {

        // 2. 优化正则：只在非开头的大写字母前加下划线
        // 正则解释：(?<!^) 负向断言，匹配"不是字符串开头"的位置；([A-Z]) 匹配大写字母
        String snakeCaseStr = camelStr.replaceAll("(?<!^)([A-Z])", "_$1");

        // 3. 转为小写（无需处理开头下划线，因为正则已避免开头加下划线）
        return snakeCaseStr.toLowerCase();
    }

}
