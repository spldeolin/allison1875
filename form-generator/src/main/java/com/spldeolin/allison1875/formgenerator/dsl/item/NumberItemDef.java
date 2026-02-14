package com.spldeolin.allison1875.formgenerator.dsl.item;

import java.util.List;
import javax.validation.constraints.NotNull;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-02-11
 */
@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NumberItemDef extends ItemDef {

    /**
     * 字段类型，用于在反序列时区别ItemDef的具体类型
     */
    final ItemType type = ItemType.NUMBER;

    /**
     * 是否可以是小数
     */
    @NotNull
    Boolean canBeDecimal = false; // 只影响DB、Java类型，其他行为和NUMBER一致，所以不设计独立的ItemType了

    @Override
    public String getDbColumnType() {
        return canBeDecimal ? "DECIMAL(14, 4)" : "BIGINT";
    }

    @Override
    public String getJavaType() {
        return "Long";
    }

    @Override
    public List<String> getJavaValidAnnotations() {
        List<String> retval = Lists.newArrayList();
        if (getIsNonValid()) {
            retval.add(annotationExprService.notNull().toString());
        }
        return retval;
    }

    @Override
    public String getJavaJsonFormatAnnoatation() {
        return "";
    }

}