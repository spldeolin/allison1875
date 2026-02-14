package com.spldeolin.allison1875.formgenerator.dsl;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.formgenerator.dsl.constraint.FormDefValid;
import com.spldeolin.allison1875.formgenerator.dsl.constraint.UpperCamel;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2025-08-12
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@FormDefValid
public class FormDef {

    /**
     * 表单名称，表单在源码中的命名，值为upperCamel分隔的英语单词，无需以"Form"结尾
     */
    @NotEmpty
    @UpperCamel
    String name;

    /**
     * 表单标题，对用户可见
     */
    @NotEmpty
    String title;

    /**
     * 表单描述，对用户可见
     */
    String desc;

    /**
     * 字段，仅定义用户需要感知的字段，
     * 无需定义代表表单"主键"，或是代表"创建时间"、"更新人"等审计字段的字段，每个元素的名称属性必须在列表中唯一
     */
    @NotEmpty
    @Valid
    List<@NotNull ItemDef> items;

    /**
     * 索引，表单字段在数据库中组成的索引
     */
    @Valid
    List<@NotNull IndexDef> indices;

}