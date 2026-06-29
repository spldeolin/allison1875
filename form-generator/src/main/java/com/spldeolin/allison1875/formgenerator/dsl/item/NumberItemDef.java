package com.spldeolin.allison1875.formgenerator.dsl.item;

import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Deolin 2026-02-11
 */
@Getter
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NumberItemDef extends ItemDef {

    /**
     * 字段类型，用于在反序列时区别ItemDef的具体类型
     */
    @Builder.Default
    ItemType type = ItemType.NUMBER;

    /**
     * 是否可以是小数
     */
    @Builder.Default
    Boolean canBeDecimal = false;

}
