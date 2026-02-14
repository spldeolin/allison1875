package com.spldeolin.allison1875.formgenerator.dsl.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 校验 upperCamel 格式：首字母大写，单词首字母大写，如 StudentBasicInfo
 *
 * @author Deolin 2026-02-11
 */
public class UpperCamelValidator implements ConstraintValidator<UpperCamel, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        if (!value.chars().allMatch(Character::isLetterOrDigit)) {
            context.buildConstraintViolationWithTemplate("只允许字母和数字（无特殊字符）").addConstraintViolation();
            return false;
        }
        if (!Character.isUpperCase(value.charAt(0))) {
            context.buildConstraintViolationWithTemplate("首字母必须大写").addConstraintViolation();
            return false;
        }
        if (value.length() > 1) {
            boolean hasLower = value.chars().anyMatch(Character::isLowerCase);
            if (!hasLower) {
                context.buildConstraintViolationWithTemplate(
                                "必须是 upperCamel 格式（首字母大写，单词首字母大写，如 StudentBasicInfo）")
                        .addConstraintViolation();
                return false;
            }
            int consecutiveUpper = 0;
            for (int i = 1; i < value.length(); i++) {
                if (Character.isUpperCase(value.charAt(i))) {
                    consecutiveUpper++;
                    if (consecutiveUpper > 2) {
                        context.buildConstraintViolationWithTemplate(
                                        "必须是 upperCamel 格式（不允许超过两个连续大写字母，如 StudentBasicInfo）")
                                .addConstraintViolation();
                        return false;
                    }
                } else {
                    consecutiveUpper = 0;
                }
            }
        }
        return true;
    }

}
