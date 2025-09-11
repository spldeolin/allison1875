package com.spldeolin.allison1875.persistencegenerator.config;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;

/**
 * @author Deolin 2025-09-11
 */
public class PersistenceGeneratorConfigValidator implements
        ConstraintValidator<PersistenceGeneratorConfigValid, PersistenceGeneratorConfig> {

    @Override
    public boolean isValid(PersistenceGeneratorConfig config, ConstraintValidatorContext context) {
        if (StringUtils.isAllEmpty(config.getJdbcUrl(), config.getDdl())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("jdbcUrl and ddl must not both be null")
                    .addConstraintViolation();
            return false;
        }

        List<String> errmsgs = Lists.newArrayList();
        if (StringUtils.isNotEmpty(config.getJdbcUrl())) {
            if (StringUtils.isEmpty(config.getUserName())) {
                errmsgs.add("userName must not be empty when jdbc is not empty");
            }
            if (StringUtils.isEmpty(config.getPassword())) {
                errmsgs.add("password must not be empty when jdbc is not empty");
            }
            if (StringUtils.isEmpty(config.getSchema())) {
                errmsgs.add("schema must not be empty when jdbc is not empty");
            }
        }

        if (errmsgs.isEmpty()) {
            return true;
        } else {
            context.disableDefaultConstraintViolation();
            for (String errmsg : errmsgs) {
                context.buildConstraintViolationWithTemplate(errmsg).addConstraintViolation();
            }
            return false;
        }
    }

}
