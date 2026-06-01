package com.spldeolin.allison1875.common.config;

import java.io.File;
import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.enums.FlushToEnum;
import com.spldeolin.allison1875.common.enums.PageParamStyleEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Allison1875 统一配置类，整合所有模块的配置项。
 *
 * @author Deolin 2026-03-12
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigValid
public class Config {

    // ==================== 公共配置 ====================

    /**
     * 业务领域配置列表，描述各领域的代码位置
     */
    @NotEmpty
    @Valid
    List<DomainConfig> domains = Lists.newArrayList();

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty
    String author = "Allison 1875";

    /**
     * 生成的DataModel是否使用Lombok
     */
    @NotNull
    Boolean isDataModelWithoutLombok = false;

    /**
     * 是否在该生成的地方生成 Any modifications may be overwritten by future code generations. 声明
     */
    @NotNull
    Boolean enableNoModifyAnnounce = true;

    /**
     * 将javax命名空间移动到jakarta，兼容Spring Boot 3+的项目
     */
    @NotNull
    Boolean enableJavaxMoveToJakarta = false;

    /**
     * 执行mvn命令时使用的JDK安装目录路径，为null时使用系统默认的JDK
     *
     * <p>配置后，在执行mvn子进程时会通过{@code JAVA_HOME}环境变量指定该JDK路径，
     * 例如配置为{@code /Users/xxx/.jenv/versions/1.8}
     */
    String javaHome;

    // ==================== Guice Module 配置 ====================

    /** doc-analyzer 功能所使用的 Guice Module 实现类全限定名 */
    String docAnalyzerModule = "com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule";

    /** handler-transformer 功能所使用的 Guice Module 实现类全限定名 */
    String handlerTransformerModule = "com.spldeolin.allison1875.handlertransformer.HandlerTransformerModule";

    /** persistence-generator 功能所使用的 Guice Module 实现类全限定名 */
    String persistenceGeneratorModule = "com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorModule";

    /** query-transformer 功能所使用的 Guice Module 实现类全限定名 */
    String queryTransformerModule = "com.spldeolin.allison1875.querytransformer.QueryTransformerModule";

    /** star-transformer 功能所使用的 Guice Module 实现类全限定名 */
    String starTransformerModule = "com.spldeolin.allison1875.startransformer.StarTransformerModule";

    /** form-generator 功能所使用的 Guice Module 实现类全限定名 */
    String formGeneratorModule = "com.spldeolin.allison1875.formgenerator.FormGeneratorModule";

    /** app-generator 功能所使用的 Guice Module 实现类全限定名 */
    String appGeneratorModule = "com.spldeolin.allison1875.appgenerator.AppGeneratorModule";

    // ==================== handler-transformer 配置 ====================

    /**
     * 启用「一个Controller均调用同一个Service」的模式（handler-transformer 使用）
     */
    @NotNull
    Boolean enableOneService = false;

    // ==================== persistence-generator 配置 ====================

    /**
     * 数据库连接
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
     * 指定schema
     */
    String schema;

    /**
     * 使用指定的DDL，在In-memory H2中构建表结构
     */
    String ddl;

    /**
     * 指定table，非必填，未填写时代表schema下所有的table
     */
    List<String> tables = Lists.newArrayList();

    /**
     * 是否为[query-transformer]生成Design类
     */
    @NotNull
    Boolean enableGenerateDesign = true;

    /**
     * 指定Design类中的分页接口使用「pageNo + pageSize」还是「offset + limit」
     */
    @NotNull
    PageParamStyleEnum pageParamStyle = PageParamStyleEnum.PAGE_NO_PAGE_SIZE;

    /**
     * 生成出的Entity类是否以Entity作为类名的结尾
     */
    @NotNull
    Boolean isEntityEndWithEntity = true;

    /**
     * 如果有逻辑删除，怎么样算作"数据被删"，非必填，只支持等式SQL
     */
    String deletedSql;

    /**
     * 如果有逻辑删除，怎么样算作"数据未被删"，非必填，只支持等式SQL
     */
    String notDeletedSql;

    // ==================== star-transformer 配置 ====================

    /**
     * Whole DTO的后缀
     */
    @NotNull
    String wholeDTONamePostfix = "WholeDTO";

    // ==================== doc-analyzer 配置 ====================

    /**
     * 目标项目handler方法签名所依赖的外部项目的目录或者具体Java文件的相对路径（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    @NotNull
    List<File> dependencyDirsOrJavaFilePath = Lists.newArrayList();

    /**
     * 全局URL前缀
     */
    @NotNull
    String globalUrlPrefix = "";

    /**
     * 文档保存到...
     */
    @NotEmpty(message = "must be 'MARKDOWN', 'YAPI', 'SHOWDOC' or 'DSL'")
    List<FlushToEnum> flushTo = Lists.newArrayList(FlushToEnum.MARKDOWN);

    /**
     * 文档输出到YApi时，YApi请求URL
     */
    String yapiUrl;

    /**
     * 文档输出到YApi时，YApi项目的TOKEN
     */
    String yapiToken;

    /**
     * 文档输出到markdown时，Markdown文件的目录的路径（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    File markdownDir = new File("api-docs");

    /**
     * 文档输出到Showdoc时，文档的基础目录名（ShowDoc提供的开放API不支持删除，设置基础目录名便于手动一次性删除后重新同步）
     */
    String showdocBaseCatName = "doc-analyzer";

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
     * 文档输出到dsl时，dsl文件的目录的路径（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    File dslDir = new File("api-dsls");

    /**
     * 文档输出到markdown或ShowDoc时，每个Endpoint是否输出到单个markdown文件
     */
    Boolean singleEndpointPerMarkdown = false;

    /**
     * 多个方法全限定名，只有能够匹配这些的MVC Handler方法才会被分析并输出文档，支持*和?通配符的
     */
    List<String> mvcHandlerQualifierWildcards;

    /**
     * 获取枚举Code的方法名
     */
    @NotNull
    String getEnumCodeMethodName = "getCode";

    /**
     * 获取枚举Title的方法名
     */
    @NotNull
    String getEnumTitleMethodName = "getTitle";

    // ==================== app-generator 配置 ====================

    /**
     * App DSL 文件路径
     */
    @NotNull
    File appDslPath = new File("./app.yml");

    /**
     * app-generator 输出目录
     */
    @NotNull
    File appGeneratorOutputDir = new File("./output");

    // ==================== form-generator 配置 ====================

    /**
     * DSL.yml文件的相对路径（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    @NotNull
    File dslPath = new File("./forms.yml");

    /**
     * 代码模板配置
     */
    @NotNull
    @Valid
    CodeSnippet codeSnippet = new CodeSnippet();

    /**
     * 代码片段
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CodeSnippet {

        /**
         * 分页对象的全限定名
         * <p>
         * 例如：com.company.project.common.PageInfo
         */
        @NotEmpty
        String pageTypeQualifier;

        /**
         * Spring MVC 请求方法统一返回类的全限定名。
         * <p>
         * 例如：com.company.project.common.RequestResult
         */
        String requestResultQualifier;

        /**
         * Spring MVC 请求方法统一返回类型声明的代码片段，其中 ${dataType} 为业务返回数据类型的固定占位符
         * <p>
         * 例如：RequestResult&lt;${dataType}&gt;
         */
        String requestResultTypeDeclaration;

        /**
         * 构造统一返回对象（无业务数据，成功场景）的代码片段
         * <p>
         * 例如：RequestResult.success()
         */
        String requestResultSuccessNoData;

        /**
         * 构造统一返回对象（有业务数据，成功场景）的代码片段，其中 ${data} 为业务返回数据对象的固定占位符
         * <p>
         * 例如：RequestResult.success(${data})
         */
        String requestResultSuccessWithData;

        /**
         * Controller类@RequestMapping路径的代码片段（占位符${formName}代表form-generator的表单名称）
         */
        @NotEmpty
        String controllerRequestMapping = "/api/v1/${formName}";

        /**
         * 生成短UUID的代码片段
         */
        @NotEmpty
        String shortUuidGeneration = "UUID.randomUUID().toString().replaceAll(\"-\", \"\").toLowerCase()";

        /**
         * 判断列表是否为empty的代码片段（占位符${list}代表列表）
         */
        @NotEmpty
        String collectionEmptyCheck = "${list} == null || ${list}.isEmpty()";

        /**
         * 构造分页返回值的代码片段（占位符${total}代表总条数，${dtos}代表当前页数据列表）
         */
        @NotEmpty
        String constructPageResult;

        /**
         * 构造空的分页返回值的代码片段
         */
        @NotEmpty
        String constructEmptyPageResult;

        /**
         * 业务逻辑异常的全限定名
         * <p>
         * form-generator 生成的 SaveApiService 等会在唯一键冲突或记录不存在时抛出该异常。
         * <p>
         * 例如：com.example.common.BizException
         */
        @NotEmpty
        String bizExceptionQualifier = "java.lang.RuntimeException";

    }

}
