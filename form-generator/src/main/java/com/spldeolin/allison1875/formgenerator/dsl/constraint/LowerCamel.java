package com.spldeolin.allison1875.formgenerator.dsl.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 校验字符串为 lowerCamel 格式：首字母小写，后续单词首字母大写，如 studentName
 *
 * @author Deolin 2026-02-11
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = LowerCamelValidator.class)
public @interface LowerCamel {

    String message() default "必须是 lowerCamel 格式（首字母小写，后续单词首字母大写，如 studentName）";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
