package com.spldeolin.allison1875.formgenerator.dsl;

import static com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern.USER_INPUT;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.formgenerator.dsl.constraint.LowerCamel;
import com.spldeolin.allison1875.formgenerator.dsl.enums.InitOrEditPattern;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2025-08-12
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class ItemDef {

    /**
     * 字段名称，字段在源码中的命名，值为lowCamel分隔的英语单词
     */
    @NotEmpty
    @LowerCamel
    String name;

    /**
     * 字段标题
     */
    @NotEmpty
    String title;

    /**
     * 字段是否非空（广义的，具体指：非未指定、非null值、非空列表/数组、非0长度/纯空格字符串
     */
    @NotNull
    Boolean isNonValid;

    /**
     * 字段的初始化方式
     */
    @NotNull
    InitOrEditPattern initPattern = USER_INPUT;

    /**
     * 字段的编辑方式
     */
    @NotNull
    InitOrEditPattern editPattern = USER_INPUT;

    /**
     * 字段的类型
     */
    public abstract ItemType getType();

}