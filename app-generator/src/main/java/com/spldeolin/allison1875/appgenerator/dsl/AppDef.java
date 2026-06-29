package com.spldeolin.allison1875.appgenerator.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Deolin 2026-05-24
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class AppDef {

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$");

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9\\-]*$");

    /**
     * 应用的命名空间，只允许小写字母、数字、点号
     */
    String namespace;

    /**
     * 应用名称，值为upperCamel分隔的英语单词
     */
    String name;

    /**
     * 表单标题，对用户可见
     */
    String title;

    /**
     * 菜单
     */
    List<MenuDef> menus;

    public void validate() {
        List<String> errors = new ArrayList<>();

        if (namespace == null || namespace.isEmpty()) {
            errors.add("namespace 不能为空");
        } else if (!NAMESPACE_PATTERN.matcher(namespace).matches()) {
            errors.add("namespace 格式不合法，只允许小写字母、数字、点号（如 com.example.demo）");
        }

        if (name == null || name.isEmpty()) {
            errors.add("name 不能为空");
        } else if (!NAME_PATTERN.matcher(name).matches()) {
            errors.add("name 只允许小写英文字母、数字、短横线（如 my-app）");
        }

        if (title == null || title.isEmpty()) {
            errors.add("title 不能为空");
        }

        if (menus == null || menus.isEmpty()) {
            errors.add("menus 不能为空");
        } else {
            for (int i = 0; i < menus.size(); i++) {
                MenuDef menu = menus.get(i);
                if (menu == null) {
                    errors.add("menus[" + i + "] 不能为 null");
                } else if (menu.getForm() == null) {
                    errors.add("menus[" + i + "].form 不能为 null");
                }
            }
            List<FormDef> forms = menus.stream()
                    .filter(m -> m != null && m.getForm() != null)
                    .map(MenuDef::getForm)
                    .toList();
            if (!forms.isEmpty()) {
                FormDef.validate(forms);
            }
        }

        if (!errors.isEmpty()) {
            throw new Allison1875Exception("app.yml 校验失败:\n" + String.join("\n", errors));
        }
    }

}
