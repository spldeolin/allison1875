package com.spldeolin.allison1875.formgenerator.dsl.item;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.OptionDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-02-11
 */
@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
public class MultiSelectItemDef extends ItemDef {

    /**
     * 字段类型，用于在反序列时区别ItemDef的具体类型
     */
    final ItemType type = ItemType.MULTI_SELECT;

    /**
     * 字段的可选项
     */
    @NotEmpty
    @Valid
    List<@NotNull OptionDef> options;

}