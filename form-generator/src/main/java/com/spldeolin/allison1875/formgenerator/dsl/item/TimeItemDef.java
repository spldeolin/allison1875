package com.spldeolin.allison1875.formgenerator.dsl.item;

import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.TimeFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-02-11
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
public class TimeItemDef extends ItemDef {

    /**
     * 字段类型，用于在反序列时区别ItemDef的具体类型
     */
    final ItemType type = ItemType.TIME;

    /**
     * 时间格式
     */
    @NotNull
    TimeFormat format = TimeFormat.DATE_TIME;

}