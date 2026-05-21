package com.spldeolin.allison1875.common.config;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.config.Config.CodeSnippet;
import com.spldeolin.allison1875.common.enums.FlushToEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 统一校验类，整合 DocAnalyzerConfig 和 PersistenceGeneratorConfig 的校验逻辑。
 *
 * @author Deolin 2026-03-12
 */
public class ConfigValidator implements ConstraintValidator<ConfigValid, Config> {

    @Override
    public boolean isValid(Config config, ConstraintValidatorContext context) {
        List<String> errmsgs = Lists.newArrayList();

        // ---- persistence-generator 相关校验 ----
        if (config.getJdbcUrl() != null || config.getDdl() != null) {
            // 只在用户配置了持久层相关字段时才校验
            if (StringUtils.isAllEmpty(config.getJdbcUrl(), config.getDdl())) {
                errmsgs.add("jdbcUrl and ddl must not both be null");
            } else if (StringUtils.isNotEmpty(config.getJdbcUrl())) {
                if (StringUtils.isEmpty(config.getUserName())) {
                    errmsgs.add("userName must not be empty when jdbcUrl is not empty");
                }
                if (StringUtils.isEmpty(config.getPassword())) {
                    errmsgs.add("password must not be empty when jdbcUrl is not empty");
                }
                if (StringUtils.isEmpty(config.getSchema())) {
                    errmsgs.add("schema must not be empty when jdbcUrl is not empty");
                }
            }
        }

        // ---- doc-analyzer 相关校验 ----
        if (config.getFlushTo() != null) {
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

        }

        // ---- codeTemplate 相关校验 ----
        if (config.getCodeSnippet() != null) {
            CodeSnippet cs = config.getCodeSnippet();
            boolean hasQualifier = StringUtils.isNotEmpty(cs.getRequestResultQualifier());
            boolean hasTypeDecl = StringUtils.isNotEmpty(cs.getRequestResultTypeDeclaration());
            boolean hasSuccessNoData = StringUtils.isNotEmpty(cs.getRequestResultSuccessNoData());
            boolean hasSuccessWithData = StringUtils.isNotEmpty(cs.getRequestResultSuccessWithData());
            boolean anyPresent = hasQualifier || hasTypeDecl || hasSuccessNoData || hasSuccessWithData;
            boolean allPresent = hasQualifier && hasTypeDecl && hasSuccessNoData && hasSuccessWithData;
            if (anyPresent && !allPresent) {
                if (!hasQualifier) {
                    errmsgs.add("codeSnippet.requestResultQualifier must not be empty when any other codeSnippet field "
                            + "is specified");
                }
                if (!hasTypeDecl) {
                    errmsgs.add("codeSnippet.requestResultTypeDeclaration must not be empty when any other codeSnippet "
                            + "field is specified");
                }
                if (!hasSuccessNoData) {
                    errmsgs.add("codeSnippet.requestResultSuccessNoData must not be empty when any other codeSnippet "
                            + "field is specified");
                }
                if (!hasSuccessWithData) {
                    errmsgs.add("codeSnippet.requestResultSuccessWithData must not be empty when any other codeSnippet "
                            + "field is specified");
                }
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
