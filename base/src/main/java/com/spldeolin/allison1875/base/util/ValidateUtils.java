package com.spldeolin.allison1875.base.util;

import java.util.Collection;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-28
 */
@Log4j2
public class ValidateUtils {

    public static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> void ensureValid(T object) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
        reportAndExit(violations);
    }

    public static <T> void ensureValid(Collection<T> objects) {
        Set<ConstraintViolation<T>> violations = Sets.newHashSet();
        for (T object : objects) {
            violations.addAll(VALIDATOR.validate(object));
        }
        reportAndExit(violations);
    }

    private static <T> void reportAndExit(Set<ConstraintViolation<T>> violations) {
        if (violations.size() > 0) {
            log.error("Allison 1875 cannot work any more cause invalid config");
            for (ConstraintViolation<?> violation : violations) {
                String configName =
                        violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath().toString()
                                .replace(".<iterable element>", "");
                String valueText = getInvalidValueText(violation);
                log.error("{} {}, current value: {}", configName, violation.getMessage(), valueText);
            }
            System.exit(-1);
        }
    }

    private static String getInvalidValueText(ConstraintViolation<?> violation) {
        String valueText;
        Object invalidValue = violation.getInvalidValue();
        if (invalidValue == null) {
            valueText = "<null>";
        } else if (invalidValue instanceof String && ((String) invalidValue).length() == 0) {
            valueText = "<empty>";
        } else {
            valueText = invalidValue.toString();
        }
        return valueText;
    }

}