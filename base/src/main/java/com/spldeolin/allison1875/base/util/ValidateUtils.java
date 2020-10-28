package com.spldeolin.allison1875.base.util;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * @author Deolin 2020-10-28
 */
public class ValidateUtils {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> Set<ConstraintViolation<T>> validate(T object) {
        Set<ConstraintViolation<T>> result = validator.validate(object);
        return result;
    }

}