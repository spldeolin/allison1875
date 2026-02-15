package com.spldeolin.allison1875.formgenerator.dsl.constraint;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;

/**
 * FormDef 校验器：
 * 1. items[].name 必须唯一
 * 2. indices[].itemNames 中的名称必须存在于 items[].name
 * 3. name 必须是 upperCamel 分隔的单词（由 @UpperCamel 注解校验）
 *
 * @author Deolin 2026-02-11
 */
public class FormDefValidator implements ConstraintValidator<FormDefValid, FormDef> {

    @Override
    public boolean isValid(FormDef formDef, ConstraintValidatorContext context) {
        if (formDef == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        // 1. name 的 upperCamel 格式由 @UpperCamel 注解校验

        // 2. items[].name 必须唯一
        if (formDef.getItems() != null && !validateItemNamesUnique(formDef, context)) {
            return false;
        }

        // 3. indices[].itemNames 必须存在于 items[].name
        if (formDef.getItems() != null && formDef.getIndices() != null && !validateIndicesItemNamesExist(formDef,
                context)) {
            return false;
        }

        // 4. indices[].itemNames中不能包含多选
        if (formDef.getItems() != null && formDef.getIndices() != null) {
            for (int i = 0; i < formDef.getIndices().size(); i++) {
                IndexDef indexDef = formDef.getIndices().get(i);
                for (ItemDef item : formDef.getItems()) {
                    if (indexDef.getItemNames().contains(item.getName()) && item.getType() == ItemType.MULTI_SELECT) {
                        buildViolation(context, "indices",
                                "indices[" + i + "] 中的 itemNames 不能是多选类型，错误项: " + item.getName());
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 校验 items[].name 唯一性
     */
    private boolean validateItemNamesUnique(FormDef formDef, ConstraintValidatorContext context) {
        Set<String> seen = new HashSet<>();
        for (ItemDef item : formDef.getItems()) {
            if (item == null || item.getName() == null) {
                continue;
            }
            if (!seen.add(item.getName())) {
                buildViolation(context, "items", "字段名称必须在列表中唯一，发现重复: " + item.getName());
                return false;
            }
        }
        return true;
    }

    /**
     * 校验 indices[].itemNames 中的名称必须存在于 items[].name
     */
    private boolean validateIndicesItemNamesExist(FormDef formDef, ConstraintValidatorContext context) {
        Set<String> itemNames = formDef.getItems().stream().filter(item -> item != null && item.getName() != null)
                .map(ItemDef::getName).collect(Collectors.toSet());

        for (int i = 0; i < formDef.getIndices().size(); i++) {
            IndexDef index = formDef.getIndices().get(i);
            if (index == null || index.getItemNames() == null) {
                continue;
            }
            Set<String> missing = index.getItemNames().stream()
                    .filter(name -> name != null && !itemNames.contains(name)).collect(Collectors.toSet());
            if (!missing.isEmpty()) {
                buildViolation(context, "indices",
                        "indices[" + i + "] 中的 itemNames 必须在 items 中存在，缺失: " + missing);
                return false;
            }
        }
        return true;
    }

    private void buildViolation(ConstraintValidatorContext context, String property, String message) {
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(property).addConstraintViolation();
    }

}
