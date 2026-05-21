package com.spldeolin.allison1875.formgenerator.dsl.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 校验 lowerCamel 格式：首字母小写，后续单词首字母大写，如 studentName
 *
 * @author Deolin 2026-02-11
 */
public class LowerCamelValidator implements ConstraintValidator<LowerCamel, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        if (!value.chars().allMatch(Character::isLetterOrDigit)) {
            context.buildConstraintViolationWithTemplate("只允许字母和数字（无特殊字符）").addConstraintViolation();
            return false;
        }
        if (!Character.isLowerCase(value.charAt(0))) {
            context.buildConstraintViolationWithTemplate("首字母必须小写").addConstraintViolation();
            return false;
        }
        if (value.length() > 1) {
            int consecutiveUpper = 0;
            for (int i = 1; i < value.length(); i++) {
                if (Character.isUpperCase(value.charAt(i))) {
                    consecutiveUpper++;
                    if (consecutiveUpper > 1) {
                        context.buildConstraintViolationWithTemplate(
                                "必须是 lowerCamel 格式（不允许连续大写字母，如 studentName）").addConstraintViolation();
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
