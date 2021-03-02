package com.spldeolin.allison1875.base.valid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import com.spldeolin.allison1875.base.valid.validator.FileIsDirectoryValidator;
import com.spldeolin.allison1875.base.valid.validator.PathIsDirectoryValidator;
import com.spldeolin.allison1875.base.valid.validator.StringIsDirectoryValidator;

/**
 * 必须是目录
 *
 * <pre>
 *  支持类型：Path、String、
 *  规则：被申明的Map对象中，不允许出现value中指定的key
 *  </pre>
 *
 * @author Deolin 2021-02-20
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {FileIsDirectoryValidator.class, PathIsDirectoryValidator.class,
        StringIsDirectoryValidator.class})
public @interface IsDirectory {

    String message() default "must be a directory";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
