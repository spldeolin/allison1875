package com.spldeolin.allison1875.common.config;

import java.io.File;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.enums.FlushToEnum;
import com.spldeolin.allison1875.common.enums.PageParamStyleEnum;
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
     * 控制器所在包的包名
     */
    @NotEmpty
    String controllerPackage;

    /**
     * 控制层@RequestBody类型所在包的包名
     */
    @NotEmpty
    String reqDTOPackage;

    /**
     * 控制层@ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty
    String respDTOPackage;

    /**
     * 枚举所在包的包名
     */
    @NotEmpty
    String enumPackage;

    /**
     * 业务层Service接口所在包的包名
     */
    @NotEmpty
    String servicePackage;

    /**
     * 业务层ServiceImpl类所在包的包名
     */
    @NotEmpty
    String serviceImplPackage;

    /**
     * 持久层mapper接口所在包的包名
     */
    @NotEmpty
    String mapperPackage;

    /**
     * 持久层Entity类所在包的包名
     */
    @NotEmpty
    String entityPackage;

    /**
     * Design类所在包的包名
     */
    @NotEmpty
    String designPackage;

    /**
     * 持久层Mapper方法签名中Param类所在包的包名
     */
    @NotEmpty
    String paramDTOPackage;

    /**
     * 持久层Mapper方法签名中Record类所在包的包名
     */
    @NotEmpty
    String recordDTOPackage;

    /**
     * WholeDTO类所在包的包名
     */
    @NotEmpty
    String wholeDTOPackage;

    /**
     * mapper.xml所在目录（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    @NotEmpty
    List<File> mapperXmlDirs = Lists.newArrayList(new File("src/main/resources/mapper"));

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty
    String author = "Allison 1875";

    /**
     * 生成的DataModel是否实现java.io.Serializable接口
     */
    @NotNull
    Boolean isDataModelSerializable = false;

    /**
     * 生成的DataModel是否实现java.lang.Cloneable接口
     */
    @NotNull
    Boolean isDataModelCloneable = false;

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
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: DA1000S-967D9357 的声明
     */
    @NotNull
    Boolean enableLotNoAnnounce = false;

    /**
     * 将javax命名空间移动到jakarta，兼容Spring Boot 3+的项目
     */
    @NotNull
    Boolean enableJavaxMoveToJakarta = false;

    /**
     * 编译版本（如：8、11、17、21），用于链式执行时Maven编译阶段的source和target版本
     */
    @NotNull
    String javaVersion = "21";

    // ==================== handler-transformer 配置 ====================

    /**
     * 分页对象的全限定名（handler-transformer 使用）
     */
    String pageTypeQualifier;

    /**
     * 启用「一个Controller均调用同一个Service」的模式（handler-transformer 使用）
     */
    @NotNull
    Boolean enableOneService = false;

    /**
     * Service接口所在的SourcePath（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    File serviceSourcePath;

    /**
     * ServiceImpl类所在的SourcePath（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    File serviceImplSourcePath;

    /**
     * DTO类所在的SourcePath（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    File dtoSourcePath;

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

    /**
     * 如果生成的Entity需要指定父类，指定父类的Class对象
     */
    Class<?> superEntity;

    /**
     * 生成Entity时，文件已存在的解决方式
     */
    @NotNull
    FileExistenceResolutionEnum entityExistenceResolution = FileExistenceResolutionEnum.OVERWRITE;

    // ==================== query-transformer 配置 ====================

    /**
     * 持久层所在的SourcePath（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    File persistenceSourcePath;

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
     * 文档输出到markdown或ShowDoc时，是否启用cURL命令的输出
     */
    Boolean enableCurl = false;

    /**
     * 文档输出到markdown或ShowDoc时，是否启用Response Body示例的输出
     */
    Boolean enableResponseBodySample = false;

    /**
     * 多个方法全限定名，只有能够匹配这些的MVC Handler方法才会被分析并输出文档，支持*和?通配符的
     */
    List<String> mvcHandlerQualifierWildcards;

    // ==================== form-generator 配置 ====================

    /**
     * DSL.yml文件的相对路径（相对于pom所在basedir的相对路径 或 绝对路径 皆可）
     */
    @NotNull
    File dslPath = new File("./forms.yml");

    /**
     * 使用doc-analyzer生成接口文档
     */
    @NotNull
    Boolean enableDocAnalyzer = false;

    /**
     * Controller类@RequestMapping路径的代码片段（占位符${formName}代表表单名称）
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
    String collectionEmptyCheck = "CollectionUtils.isEmpty(${list})";

    /**
     * 分页相关代码模板配置
     */
    @NotNull
    @Valid
    PageTemplates pageTemplates = new PageTemplates();

    /**
     * 分页相关代码模板配置（form-generator 使用）
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PageTemplates {

        /**
         * 构造分页返回值的代码片段（占位符${total}代表总条数，${dtos}代表当前页数据列表）
         */
        String pageResultConstruction;

        /**
         * 构造空的分页返回值的代码片段
         */
        String pageResultEmptyConstruction;

    }

}
