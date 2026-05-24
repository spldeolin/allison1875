package com.spldeolin.allison1875.appgenerator.dsl;

import java.util.List;
import com.spldeolin.allison1875.formgenerator.dsl.constraint.UpperCamel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-05-24
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppDef {

    /**
     * 应用的命名空间，只允许小写字母、数字、点号
     */
    String namespace;

    /**
     * 应用名称，值为upperCamel分隔的英语单词
     */
    @UpperCamel
    String name;

    /**
     * 表单标题，对用户可见
     */
    String title;

    /**
     * 菜单
     */
    @NotEmpty
    List<@NotNull MenuDef> menus;

}