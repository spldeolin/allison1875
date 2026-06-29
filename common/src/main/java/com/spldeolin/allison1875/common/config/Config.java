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

    /**
     * 业务领域配置列表，描述各领域的代码位置
     */
    List<DomainConfig> domains;

    /**
     * 为生成的代码指定作者
     */
    String author;

    /**
     * 生成的DataModel是否使用Lombok
     */
    Boolean isDataModelWithoutLombok;

    /**
     * 是否在该生成的地方生成 Any modifications may be overwritten by future code generations. 声明
     */
    Boolean enableNoModifyAnnounce;

    /**
     * 将javax命名空间移动到jakarta，兼容Spring Boot 3+的项目
     */
    Boolean enableJavaxMoveToJakarta;

    /**
     * 执行mvn命令时使用的JDK安装目录路径，为null时使用系统默认的JDK
     */
    String javaHome;

    /**
     * doc-analyzer 功能所使用的 Guice Module 实现类全限定名
     */
    String docAnalyzerModule;

    /**
     * handler-transformer 功能所使用的 Guice Module 实现类全限定名
     */
    String handlerTransformerModule;

    /**
     * persistence-generator 功能所使用的 Guice Module 实现类全限定名
     */
    String persistenceGeneratorModule;

    /**
     * query-transformer 功能所使用的 Guice Module 实现类全限定名
     */
    String queryTransformerModule;

    /**
     * star-transformer 功能所使用的 Guice Module 实现类全限定名
     */
    String starTransformerModule;

    /**
     * form-generator 功能所使用的 Guice Module 实现类全限定名
     */
    String formGeneratorModule;

    /**
     * app-generator 功能所使用的 Guice Module 实现类全限定名
     */
    String appGeneratorModule;

    /**
     * 启用「一个Controller均调用同一个Service」的模式（handler-transformer 使用）
     */
    Boolean enableOneService;

    /**
     * 数据库连接URL
     */
    String jdbcUrl;

    /**
     * 数据库用户名
     */
    String userName;

    /**
     * 数据库密码
     */
    String password;

    /**
     * 指定数据库schema
     */
    String schema;

    /**
     * 使用指定的DDL，在In-memory H2中构建表结构
     */
    String ddl;

    /**
     * 指定table，非必填，未填写时代表schema下所有的table
     */
    List<String> tables;

    /**
     * 是否为query-transformer生成Design类
     */
    Boolean enableGenerateDesign;

    /**
     * 生成出的Entity类是否以Entity作为类名的结尾
     */
    Boolean isEntityEndWithEntity;

    /**
     * 如果有逻辑删除，怎么样算作"数据被删"，非必填，只支持等式SQL
     */
    String deletedSql;

    /**
     * 如果有逻辑删除，怎么样算作"数据未被删"，非必填，只支持等式SQL
     */
    String notDeletedSql;

    /**
     * Whole DTO的后缀
     */
    String wholeDTONamePostfix;

    /**
     * 目标项目handler方法签名所依赖的外部项目的目录或者具体Java文件的路径
     */
    List<File> dependencyDirsOrJavaFilePath;

    /**
     * 全局URL前缀
     */
    String globalUrlPrefix;

    /**
     * 文档保存到的目标列表
     */
    List<FlushToEnum> flushTo;

    /**
     * 文档输出到YApi时，YApi请求URL
     */
    String yapiUrl;

    /**
     * 文档输出到YApi时，YApi项目的TOKEN
     */
    String yapiToken;

    /**
     * 文档输出到markdown时，Markdown文件的目录路径
     */
    File markdownDir;

    /**
     * 文档输出到Showdoc时，文档的基础目录名
     */
    String showdocBaseCatName;

    /**
     * 文档输出到Showdoc时，ShowDoc开放API的URL
     */
    String showdocUrl;

    /**
     * 文档输出到Showdoc时，ShowDoc开放API的api_key
     */
    String showdocApiKey;

    /**
     * 文档输出到Showdoc时，ShowDoc开放API的api_token
     */
    String showdocApiToken;

    /**
     * 文档输出到dsl时，dsl文件的目录路径
     */
    File dslDir;

    /**
     * 文档输出到markdown或ShowDoc时，每个Endpoint是否输出到单个markdown文件
     */
    Boolean singleEndpointPerMarkdown;

    /**
     * 多个方法全限定名，只有能够匹配这些的MVC Handler方法才会被分析并输出文档，支持通配符
     */
    List<String> mvcHandlerQualifierWildcards;

    /**
     * 获取枚举Code的方法名
     */
    String getEnumCodeMethodName;

    /**
     * 获取枚举Title的方法名
     */
    String getEnumTitleMethodName;

    /**
     * App DSL 文件路径
     */
    File appDslPath;

    /**
     * app-generator 输出目录
     */
    File appGeneratorOutputDir;

    /**
     * form-generator DSL文件路径
     */
    File dslPath;

    /**
     * 代码模板配置
     */
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

        /**
         * Spring MVC 请求方法统一返回类的全限定名
         */
        String requestResultQualifier;

        /**
         * Spring MVC 请求方法统一返回类型声明的代码片段，其中 ${dataType} 为业务返回数据类型的占位符
         */
        String requestResultTypeDeclaration;

        /**
         * 构造统一返回对象（无业务数据，成功场景）的代码片段
         */
        String requestResultSuccessNoData;

        /**
         * 构造统一返回对象（有业务数据，成功场景）的代码片段，其中 ${data} 为业务返回数据对象的占位符
         */
        String requestResultSuccessWithData;

        /**
         * Controller类@RequestMapping路径的代码片段，占位符${formName}代表表单名称
         */
        String controllerRequestMapping;

        /**
         * 生成短UUID的代码片段
         */
        String shortUuidGeneration;

        /**
         * 判断列表是否为empty的代码片段，占位符${list}代表列表
         */
        String collectionEmptyCheck;

        /**
         * 业务逻辑异常的全限定名
         */
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
