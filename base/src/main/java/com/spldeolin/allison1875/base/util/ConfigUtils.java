package com.spldeolin.allison1875.base.util;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-28
 */
@Log4j2
public class ConfigUtils {

    private ConfigUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static void validate(Object config) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Object>> valids = validator.validate(config);
        valids.forEach(valid -> log.error("配置项校验未通过：{}{}", valid.getPropertyPath(), valid.getMessage()));
        if (valids.size() > 0) {
            System.exit(-9);
        }
    }

}