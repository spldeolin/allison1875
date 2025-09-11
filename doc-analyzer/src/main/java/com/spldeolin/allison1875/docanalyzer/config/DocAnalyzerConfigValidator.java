package com.spldeolin.allison1875.docanalyzer.config;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.docanalyzer.enums.FlushToEnum;

/**
 * @author Deolin 2025-09-11
 */
public class DocAnalyzerConfigValidator implements ConstraintValidator<DocAnalyzerConfigValid, DocAnalyzerConfig> {

    @Override
    public boolean isValid(DocAnalyzerConfig config, ConstraintValidatorContext context) {
        if (config.getFlushTo() == null) {
            // nothing to valid here
            return true;
        }

        List<String> errmsgs = Lists.newArrayList();
        if (config.getFlushTo().contains(FlushToEnum.YAPI)) {
            if (config.getYapiUrl() == null) {
                errmsgs.add("yapiUrl must not be null when flushTo contains 'YAPI'");
            }
            if (config.getYapiToken() == null) {
                errmsgs.add("yapiToken must not be null when flushTo contains 'YAPI'");
            }
        }

        if (config.getFlushTo().contains(FlushToEnum.MARKDOWN)) {
            if (config.getMarkdownDir() == null) {
                errmsgs.add("markdownDirectoryPath must not be null when flushTo contains 'MARKDOWN'");
            }
        }

        if (config.getFlushTo().contains(FlushToEnum.DSL)) {
            if (config.getDslDir() == null) {
                errmsgs.add("dslDir must not be null when flushTo contains 'DSL'");
            }
        }

        if (config.getFlushTo().contains(FlushToEnum.SHOWDOC)) {
            if (config.getShowdocUrl() == null) {
                errmsgs.add("showdocUrl must not be null when flushTo contains 'SHOWDOC'");
            }
            if (config.getShowdocApiKey() == null) {
                errmsgs.add("showdocApiKey must not be null when flushTo contains 'SHOWDOC'");
            }
            if (config.getShowdocApiToken() == null) {
                errmsgs.add("showdocApiToken must not be null when flushTo contains 'SHOWDOC'");
            }
        }

        if (config.getFlushTo().contains(FlushToEnum.MARKDOWN) || config.getFlushTo().contains(FlushToEnum.SHOWDOC)) {
            if (config.getEnableCurl() == null) {
                errmsgs.add("enableCurl must not be null when flushTo contains 'MARKDOWN' or 'SHOWDOC'");
            }
            if (config.getEnableResponseBodySample() == null) {
                errmsgs.add("enableResponseBodySample must not be null when flushTo contains 'MARKDOWN' or 'SHOWDOC'");
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
