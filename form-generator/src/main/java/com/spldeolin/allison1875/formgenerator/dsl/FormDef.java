package com.spldeolin.allison1875.formgenerator.dsl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Deolin 2025-08-12
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class FormDef {

    /**
     * 表单名称，表单在源码中的命名，值为upperCamel分隔的英语单词，无需以"Form"结尾
     */
    String name;

    /**
     * 表单标题，对用户可见
     */
    String title;

    /**
     * 表单描述，对用户可见
     */
    String desc;

    /**
     * 字段，仅定义用户需要感知的字段，
     * 无需定义代表表单"主键"，或是代表"创建时间"、"更新人"等审计字段的字段，每个元素的名称属性必须在列表中唯一
     */
    List<ItemDef> items;

    /**
     * 索引，表单字段在数据库中组成的索引
     */
    List<IndexDef> indices;

    public static void validate(List<FormDef> forms) {
        List<String> errors = new ArrayList<>();
        if (forms == null || forms.isEmpty()) {
            throw new Allison1875Exception("forms 列表不能为空");
        }
        for (int i = 0; i < forms.size(); i++) {
            validateFormDef(forms.get(i), i, errors);
        }
        if (!errors.isEmpty()) {
            throw new Allison1875Exception("DSL 校验失败:\n" + String.join("\n", errors));
        }
    }

    public String getEntityName(Config persistenceGeneratorConfig) {
        if (persistenceGeneratorConfig.getIsEntityEndWithEntity()) {
            return this.getName() + "Entity";
        } else {
            return this.getName();
        }
    }

    @JsonIgnore
    public String getVarName() {
        return StringUtils.uncapitalize(this.getName());
    }

    @JsonIgnore
    public String getBizIdName() {
        return this.getItems().get(0).getName();
    }

    @JsonIgnore
    public String getBizIdGetterName() {
        return "get" + StringUtils.capitalize(this.getBizIdName());
    }

    @JsonIgnore
    public String getBizIdSetterName() {
        return "set" + StringUtils.capitalize(this.getBizIdName());
    }

    @JsonIgnore
    public List<ItemDef> getNonAuditedItems() {
        return items.stream()
                .filter(item -> !Boolean.TRUE.equals(item.getIsBuiltinField()))
                .collect(Collectors.toList());
    }

    private static void validateFormDef(FormDef form, int formIndex, List<String> errors) {
        String prefix = "forms[" + formIndex + "]";

        if (form.getName() == null || form.getName().isEmpty()) {
            errors.add(prefix + ".name 不能为空");
        } else {
            validateUpperCamel(form.getName(), prefix + ".name", errors);
            if (form.getName().equalsIgnoreCase("User")) {
                errors.add(prefix + ".name 不允许与内置实体同名");
            }
        }

        if (form.getTitle() == null || form.getTitle().isEmpty()) {
            errors.add(prefix + ".title 不能为空");
        }

        if (form.getItems() == null || form.getItems().isEmpty()) {
            errors.add(prefix + ".items 不能为空");
        } else {
            validateItemsUnique(form, prefix, errors);
            for (int i = 0; i < form.getItems().size(); i++) {
                validateItemDef(form.getItems().get(i), prefix + ".items[" + i + "]", errors);
            }
        }

        if (form.getIndices() != null && form.getItems() != null) {
            validateIndices(form, prefix, errors);
        }
    }

    private static void validateItemDef(ItemDef item, String prefix, List<String> errors) {
        if (item == null) {
            errors.add(prefix + " 不能为 null");
            return;
        }
        if (item.getName() == null || item.getName().isEmpty()) {
            errors.add(prefix + ".name 不能为空");
        } else {
            validateLowerCamel(item.getName(), prefix + ".name", errors);
        }
        if (item.getTitle() == null || item.getTitle().isEmpty()) {
            errors.add(prefix + ".title 不能为空");
        }
        if (item.getIsNonVoid() == null) {
            errors.add(prefix + ".isNonVoid 不能为 null");
        }
    }

    private static void validateItemsUnique(FormDef form, String prefix, List<String> errors) {
        Set<String> seen = new HashSet<>();
        for (ItemDef item : form.getItems()) {
            if (item == null || item.getName() == null) {
                continue;
            }
            if (!seen.add(item.getName())) {
                errors.add(prefix + ".items 字段名称必须唯一，发现重复: " + item.getName());
            }
        }
    }

    private static void validateIndices(FormDef form, String prefix, List<String> errors) {
        Set<String> itemNames = form.getItems().stream()
                .filter(item -> item != null && item.getName() != null)
                .map(ItemDef::getName)
                .collect(Collectors.toSet());

        for (int i = 0; i < form.getIndices().size(); i++) {
            IndexDef index = form.getIndices().get(i);
            if (index == null) {
                continue;
            }
            if (index.getItemNames() == null || index.getItemNames().isEmpty()) {
                errors.add(prefix + ".indices[" + i + "].itemNames 不能为空");
                continue;
            }
            Set<String> missing = index.getItemNames().stream()
                    .filter(n -> n != null && !itemNames.contains(n))
                    .collect(Collectors.toSet());
            if (!missing.isEmpty()) {
                errors.add(prefix + ".indices[" + i + "].itemNames 必须在 items 中存在，缺失: " + missing);
            }
            for (ItemDef item : form.getItems()) {
                if (item != null && index.getItemNames().contains(item.getName())
                        && item.getType() == ItemType.MULTI_SELECT) {
                    errors.add(prefix + ".indices[" + i + "] 中的 itemNames 不能是多选类型，错误项: " + item.getName());
                }
            }
        }
    }

    private static void validateUpperCamel(String value, String path, List<String> errors) {
        if (!value.chars().allMatch(Character::isLetterOrDigit)) {
            errors.add(path + " 只允许字母和数字");
            return;
        }
        if (!Character.isUpperCase(value.charAt(0))) {
            errors.add(path + " 首字母必须大写");
            return;
        }
        if (value.length() > 1) {
            boolean hasLower = value.chars().anyMatch(Character::isLowerCase);
            if (!hasLower) {
                errors.add(path + " 必须是 upperCamel 格式");
                return;
            }
            int consecutiveUpper = 0;
            for (int i = 1; i < value.length(); i++) {
                if (Character.isUpperCase(value.charAt(i))) {
                    consecutiveUpper++;
                    if (consecutiveUpper > 2) {
                        errors.add(path + " 不允许超过两个连续大写字母");
                        return;
                    }
                } else {
                    consecutiveUpper = 0;
                }
            }
        }
    }

    private static void validateLowerCamel(String value, String path, List<String> errors) {
        if (!value.chars().allMatch(Character::isLetterOrDigit)) {
            errors.add(path + " 只允许字母和数字");
            return;
        }
        if (!Character.isLowerCase(value.charAt(0))) {
            errors.add(path + " 首字母必须小写");
            return;
        }
        if (value.length() > 1) {
            int consecutiveUpper = 0;
            for (int i = 1; i < value.length(); i++) {
                if (Character.isUpperCase(value.charAt(i))) {
                    consecutiveUpper++;
                    if (consecutiveUpper > 1) {
                        errors.add(path + " 不允许连续大写字母");
                        return;
                    }
                } else {
                    consecutiveUpper = 0;
                }
            }
        }
    }

}
