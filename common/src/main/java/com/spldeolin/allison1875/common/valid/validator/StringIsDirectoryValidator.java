package com.spldeolin.allison1875.common.valid.validator;

import java.io.File;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.spldeolin.allison1875.common.valid.annotation.IsDirectory;

/**
 * 校验器：确保目标是一个目录
 *
 * @author Deolin 2021-02-20
 */
public class StringIsDirectoryValidator implements ConstraintValidator<IsDirectory, String> {

    @Override
    public void initialize(IsDirectory constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return new File(value).isDirectory();
    }

}