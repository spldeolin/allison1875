package com.spldeolin.allison1875.formgenerator.dsl.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 校验字符串为 upperCamel 格式：首字母大写，单词首字母大写，如 StudentBasicInfo
 *
 * @author Deolin 2026-02-11
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = UpperCamelValidator.class)
public @interface UpperCamel {

    String message() default "必须是 upperCamel 格式（首字母大写，单词首字母大写，如 StudentBasicInfo）";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
