package com.spldeolin.allison1875.common.util;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-28
 */
@Slf4j
public class ValidUtils {

    private static final Validator validator;

    static {
        Locale.setDefault(Locale.ENGLISH);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private ValidUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static List<InvalidDto> valid(Object object) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object);
        if (CollectionUtils.isEmpty(violations)) {
            return Lists.newArrayList();
        }

        List<InvalidDto> result = Lists.newArrayList();
        for (ConstraintViolation<?> violation : violations) {
            String path = violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath().toString()
                    .replace(".<iterable element>", "");
            String valueText = formatValue(violation.getInvalidValue());
            InvalidDto invalid = new InvalidDto().setPath(path).setValue(valueText).setReason(violation.getMessage());
            result.add(invalid);
        }
        return result;
    }

    public static String formatValue(Object invalidValue) {
        if (invalidValue == null) {
            return "<null>";
        } else if (invalidValue instanceof String && ((String) invalidValue).isEmpty()) {
            return "<empty>";
        } else {
            return invalidValue.toString();
        }
    }

}