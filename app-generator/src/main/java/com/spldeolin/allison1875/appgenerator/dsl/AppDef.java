package com.spldeolin.allison1875.appgenerator.dsl;

import java.util.List;
import com.spldeolin.allison1875.formgenerator.dsl.constraint.UpperCamel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @NotEmpty
    @Pattern(regexp = "^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$")
    String namespace;

    /**
     * 应用名称，值为upperCamel分隔的英语单词
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
     * 菜单
     */
    @NotEmpty
    @Valid
    List<@NotNull MenuDef> menus;

}