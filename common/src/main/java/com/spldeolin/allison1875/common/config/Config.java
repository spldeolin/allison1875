package com.spldeolin.allison1875.common.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spldeolin.allison1875.common.enums.FlushToEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Allison1875 unified configuration (immutable).
 *
 * @author Deolin 2026-03-12
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
public class Config {

    // ==================== Common ====================

    List<DomainConfig> domains;

    String author;

    Boolean isDataModelWithoutLombok;

    Boolean enableNoModifyAnnounce;

    Boolean enableJavaxMoveToJakarta;

    String javaHome;

    // ==================== Guice Module ====================

    String docAnalyzerModule;

    String handlerTransformerModule;

    String persistenceGeneratorModule;

    String queryTransformerModule;

    String starTransformerModule;

    String formGeneratorModule;

    String appGeneratorModule;

    // ==================== handler-transformer ====================

    Boolean enableOneService;

    // ==================== persistence-generator ====================

    String jdbcUrl;

    String userName;

    String password;

    String schema;

    String ddl;

    List<String> tables;

    Boolean enableGenerateDesign;

    Boolean isEntityEndWithEntity;

    String deletedSql;

    String notDeletedSql;

    // ==================== star-transformer ====================

    String wholeDTONamePostfix;

    // ==================== doc-analyzer ====================

    List<File> dependencyDirsOrJavaFilePath;

    String globalUrlPrefix;

    List<FlushToEnum> flushTo;

    String yapiUrl;

    String yapiToken;

    File markdownDir;

    String showdocBaseCatName;

    String showdocUrl;

    String showdocApiKey;

    String showdocApiToken;

    File dslDir;

    Boolean singleEndpointPerMarkdown;

    List<String> mvcHandlerQualifierWildcards;

    String getEnumCodeMethodName;

    String getEnumTitleMethodName;

    // ==================== app-generator ====================

    File appDslPath;

    File appGeneratorOutputDir;

    // ==================== form-generator ====================

    File dslPath;

    CodeSnippet codeSnippet;

    /**
     * Deserialize from a YAML file, apply defaults, validate, and return an immutable Config.
     */
    public static Config fromYaml(File yamlFile) {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Config raw;
        try {
            raw = yamlMapper.readValue(yamlFile, Config.class);
        } catch (IOException e) {
            throw new Allison1875Exception("Failed to read config from " + yamlFile.getAbsolutePath(), e);
        }
        Config config = applyDefaults(raw);
        validate(config);
        return config;
    }

    private static Config applyDefaults(Config raw) {
        ConfigBuilder b = raw.toBuilder();
        if (raw.author == null) {
            b.author("Allison 1875");
        }
        if (raw.isDataModelWithoutLombok == null) {
            b.isDataModelWithoutLombok(false);
        }
        if (raw.enableNoModifyAnnounce == null) {
            b.enableNoModifyAnnounce(true);
        }
        if (raw.enableJavaxMoveToJakarta == null) {
            b.enableJavaxMoveToJakarta(false);
        }
        if (raw.docAnalyzerModule == null) {
            b.docAnalyzerModule("com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule");
        }
        if (raw.handlerTransformerModule == null) {
            b.handlerTransformerModule("com.spldeolin.allison1875.handlertransformer.HandlerTransformerModule");
        }
        if (raw.persistenceGeneratorModule == null) {
            b.persistenceGeneratorModule(
                    "com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule");
        }
        if (raw.queryTransformerModule == null) {
            b.queryTransformerModule("com.spldeolin.allison1875.querytransformer.QueryTransformerModule");
        }
        if (raw.starTransformerModule == null) {
            b.starTransformerModule("com.spldeolin.allison1875.startransformer.StarTransformerModule");
        }
        if (raw.formGeneratorModule == null) {
            b.formGeneratorModule("com.spldeolin.allison1875.formgenerator.FormGeneratorModule");
        }
        if (raw.appGeneratorModule == null) {
            b.appGeneratorModule("com.spldeolin.allison1875.appgenerator.AppGeneratorModule");
        }
        if (raw.enableOneService == null) {
            b.enableOneService(false);
        }
        if (raw.tables == null) {
            b.tables(new ArrayList<>());
        }
        if (raw.enableGenerateDesign == null) {
            b.enableGenerateDesign(true);
        }
        if (raw.isEntityEndWithEntity == null) {
            b.isEntityEndWithEntity(true);
        }
        if (raw.wholeDTONamePostfix == null) {
            b.wholeDTONamePostfix("WholeDTO");
        }
        if (raw.dependencyDirsOrJavaFilePath == null) {
            b.dependencyDirsOrJavaFilePath(new ArrayList<>());
        }
        if (raw.globalUrlPrefix == null) {
            b.globalUrlPrefix("");
        }
        if (raw.flushTo == null) {
            b.flushTo(List.of(FlushToEnum.MARKDOWN));
        }
        if (raw.markdownDir == null) {
            b.markdownDir(new File("api-docs"));
        }
        if (raw.showdocBaseCatName == null) {
            b.showdocBaseCatName("doc-analyzer");
        }
        if (raw.dslDir == null) {
            b.dslDir(new File("api-dsls"));
        }
        if (raw.singleEndpointPerMarkdown == null) {
            b.singleEndpointPerMarkdown(false);
        }
        if (raw.getEnumCodeMethodName == null) {
            b.getEnumCodeMethodName("getCode");
        }
        if (raw.getEnumTitleMethodName == null) {
            b.getEnumTitleMethodName("getTitle");
        }
        if (raw.appDslPath == null) {
            b.appDslPath(new File("./app.yml"));
        }
        if (raw.appGeneratorOutputDir == null) {
            b.appGeneratorOutputDir(new File("./output"));
        }
        if (raw.dslPath == null) {
            b.dslPath(new File("./forms.yml"));
        }
        if (raw.codeSnippet == null) {
            b.codeSnippet(CodeSnippet.applyDefaults(CodeSnippet.builder().build()));
        } else {
            b.codeSnippet(CodeSnippet.applyDefaults(raw.codeSnippet));
        }
        // domains default to empty list
        if (raw.domains == null) {
            b.domains(new ArrayList<>());
        } else {
            List<DomainConfig> defaultedDomains = new ArrayList<>();
            for (DomainConfig dc : raw.domains) {
                defaultedDomains.add(DomainConfig.applyDefaults(dc));
            }
            b.domains(defaultedDomains);
        }
        return b.build();
    }

    private static void validate(Config config) {
        List<String> errors = new ArrayList<>();

        // domains must not be empty
        if (config.domains == null || config.domains.isEmpty()) {
            errors.add("domains must not be empty");
        }

        // persistence-generator: jdbc requires userName, password, schema
        if (config.jdbcUrl != null || config.ddl != null) {
            if (config.jdbcUrl != null && !config.jdbcUrl.isEmpty()) {
                if (config.userName == null || config.userName.isEmpty()) {
                    errors.add("userName must not be empty when jdbcUrl is not empty");
                }
                if (config.password == null || config.password.isEmpty()) {
                    errors.add("password must not be empty when jdbcUrl is not empty");
                }
                if (config.schema == null || config.schema.isEmpty()) {
                    errors.add("schema must not be empty when jdbcUrl is not empty");
                }
            }
        }

        // doc-analyzer: flushTo conditional validation
        if (config.flushTo != null) {
            if (config.flushTo.contains(FlushToEnum.YAPI)) {
                if (config.yapiUrl == null) {
                    errors.add("yapiUrl must not be null when flushTo contains 'YAPI'");
                }
                if (config.yapiToken == null) {
                    errors.add("yapiToken must not be null when flushTo contains 'YAPI'");
                }
            }
            if (config.flushTo.contains(FlushToEnum.MARKDOWN)) {
                if (config.markdownDir == null) {
                    errors.add("markdownDir must not be null when flushTo contains 'MARKDOWN'");
                }
            }
            if (config.flushTo.contains(FlushToEnum.DSL)) {
                if (config.dslDir == null) {
                    errors.add("dslDir must not be null when flushTo contains 'DSL'");
                }
            }
            if (config.flushTo.contains(FlushToEnum.SHOWDOC)) {
                if (config.showdocUrl == null) {
                    errors.add("showdocUrl must not be null when flushTo contains 'SHOWDOC'");
                }
                if (config.showdocApiKey == null) {
                    errors.add("showdocApiKey must not be null when flushTo contains 'SHOWDOC'");
                }
                if (config.showdocApiToken == null) {
                    errors.add("showdocApiToken must not be null when flushTo contains 'SHOWDOC'");
                }
            }
        }

        // codeSnippet: requestResult* four fields all-or-nothing
        if (config.codeSnippet != null) {
            CodeSnippet cs = config.codeSnippet;
            boolean hasQualifier = cs.getRequestResultQualifier() != null
                    && !cs.getRequestResultQualifier().isEmpty();
            boolean hasTypeDecl = cs.getRequestResultTypeDeclaration() != null
                    && !cs.getRequestResultTypeDeclaration().isEmpty();
            boolean hasSuccessNoData = cs.getRequestResultSuccessNoData() != null
                    && !cs.getRequestResultSuccessNoData().isEmpty();
            boolean hasSuccessWithData = cs.getRequestResultSuccessWithData() != null
                    && !cs.getRequestResultSuccessWithData().isEmpty();
            boolean anyPresent = hasQualifier || hasTypeDecl || hasSuccessNoData || hasSuccessWithData;
            boolean allPresent = hasQualifier && hasTypeDecl && hasSuccessNoData && hasSuccessWithData;
            if (anyPresent && !allPresent) {
                if (!hasQualifier) {
                    errors.add("codeSnippet.requestResultQualifier must not be empty when any other "
                            + "requestResult field is specified");
                }
                if (!hasTypeDecl) {
                    errors.add("codeSnippet.requestResultTypeDeclaration must not be empty when any other "
                            + "requestResult field is specified");
                }
                if (!hasSuccessNoData) {
                    errors.add("codeSnippet.requestResultSuccessNoData must not be empty when any other "
                            + "requestResult field is specified");
                }
                if (!hasSuccessWithData) {
                    errors.add("codeSnippet.requestResultSuccessWithData must not be empty when any other "
                            + "requestResult field is specified");
                }
            }
        }

        // cascade validate each DomainConfig
        if (config.domains != null) {
            for (int i = 0; i < config.domains.size(); i++) {
                DomainConfig dc = config.domains.get(i);
                String prefix = "domains[" + i + "].";
                DomainConfig.validate(dc, prefix, errors);
            }
        }

        if (!errors.isEmpty()) {
            throw new Allison1875Exception("Config validation failed:\n- " + String.join("\n- ", errors));
        }
    }

    /**
     * Code snippet configuration (immutable).
     *
     * @author Deolin 2026-03-12
     */
    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    public static class CodeSnippet {

        String requestResultQualifier;

        String requestResultTypeDeclaration;

        String requestResultSuccessNoData;

        String requestResultSuccessWithData;

        String controllerRequestMapping;

        String shortUuidGeneration;

        String collectionEmptyCheck;

        String bizExceptionQualifier;

        static CodeSnippet applyDefaults(CodeSnippet raw) {
            CodeSnippetBuilder b = raw.toBuilder();
            if (raw.controllerRequestMapping == null) {
                b.controllerRequestMapping("/api/v1/${formName}");
            }
            if (raw.shortUuidGeneration == null) {
                b.shortUuidGeneration("UUID.randomUUID().toString().replaceAll(\"-\", \"\").toLowerCase()");
            }
            if (raw.collectionEmptyCheck == null) {
                b.collectionEmptyCheck("${list} == null || ${list}.isEmpty()");
            }
            if (raw.bizExceptionQualifier == null) {
                b.bizExceptionQualifier("java.lang.RuntimeException");
            }
            return b.build();
        }

    }

}
