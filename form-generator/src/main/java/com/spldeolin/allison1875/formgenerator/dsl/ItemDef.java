package com.spldeolin.allison1875.formgenerator.dsl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.FileItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.NumberItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.OnOffItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.SecretItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.SelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author Deolin 2025-08-12
 */
@Getter
@SuperBuilder(toBuilder = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible =
        true)
@JsonSubTypes({@JsonSubTypes.Type(value = NumberItemDef.class, name = "number"),
        @JsonSubTypes.Type(value = OnOffItemDef.class, name = "onOff"),
        @JsonSubTypes.Type(value = SecretItemDef.class, name = "secret"),
        @JsonSubTypes.Type(value = SelectItemDef.class, name = "select"),
        @JsonSubTypes.Type(value = MultiSelectItemDef.class, name = "multiSelect"),
        @JsonSubTypes.Type(value = TextItemDef.class, name = "text"),
        @JsonSubTypes.Type(value = TimeItemDef.class, name = "time"),
        @JsonSubTypes.Type(value = FileItemDef.class, name = "file")})
public abstract class ItemDef {

    /**
     * 字段名称，字段在源码中的命名，值为lowCamel分隔的英语单词
     */
    String name;

    /**
     * 字段标题
     */
    String title;

    /**
     * 字段是否非空（广义的，具体指：非未指定、非null值、非空列表/数组、非0长度/纯空格字符串
     */
    Boolean isNonVoid;

    /**
     * 创建（init）时是否允许用户输入。默认 true。
     */
    @Builder.Default
    Boolean canInputOnInit = true;

    /**
     * 编辑（edit）时是否允许用户输入。默认 true。
     */
    @Builder.Default
    Boolean canInputOnEdit = true;

    /**
     * 字段的类型
     */
    public abstract ItemType getType();

    /**
     * 内部标记，非DSL字段。由CommonItemsExpansionService设置，用于getNonAuditedItems()过滤。
     */
    Boolean isBuiltinField;

}
