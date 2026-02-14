package com.spldeolin.allison1875.formgenerator.dsl.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * FormDef 类级别校验：items 名称唯一、indices 引用有效、name 为 upperCamel 格式
 *
 * @author Deolin 2026-02-11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = FormDefValidator.class)
public @interface FormDefValid {

    String message() default "表单定义校验失败";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
