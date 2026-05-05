---
name: integrate-allison1875
description: Add allison1875-maven-plugin and allison1875-support to a modern Spring Boot project and configure .allison1875.yml. Use when the user asks to integrate allison1875, add the Allison 1875 Maven plugin, or configure .allison1875.yml in a Spring Boot project.
---

# 接入 Allison1875

为现代 Spring Boot 工程接入 allison1875-maven-plugin、allison1875-support 依赖，并配置 `.allison1875.yml` 文件，文件内容是YAML格式的配置项。

## 一、添加 Maven 依赖与插件

### 1. 依赖

在 `pom.xml` 的 `<dependencies>` 中增加：

```xml

<dependency>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-support</artifactId>
    <version>14.0-SNAPSHOT</version>
</dependency>
```

若使用正式发布版，将 `version` 改为 Maven Central 上的版本。

### 2. 插件

在 `<build><plugins>` 中增加：

```xml

<plugin>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-maven-plugin</artifactId>
    <version>14.0-SNAPSHOT</version>
</plugin>
```

若项目使用自定义 Module（见下文「自定义 Module」），可在 `<plugin>` 内通过 `<dependencies>` 引入对应 jar。

## 二、配置 `.allison1875.yml`

在**工程根目录**（与 `pom.xml` 同级）创建 `.allison1875.yml`。字段对应 Allison 1875 中的 Java 配置类，YAML 键名使用 camelCase，与 Java 字段名一致。

### 配置方式约定

- **可推断**：通过扫描项目包结构、已有 Controller/Service/Mapper/Entity、`application.yml`、JDBC 等推断，直接填写并简要说明依据。
- **需询问**：无法从代码或配置中可靠推断时（如密码、是否逻辑删除、分页类型等），在技能流程中列出问题，询问用户后再填。
- **不可省略**：即便部分字段可直接使用默认值，也必须在`.allison1875.yml`中显式注明。

以下通过YAML示例的形式，按模块分组列举每个字段，并说明含义、如何推断和何时询问。

```yaml
# -- 通用配置 -- #

# 控制器所在包。扫描 `@RestController` / `@Controller` 所在包
controllerPackage: com.company.proj.biz.controller

# 控制层 @RequestBody 类型所在包。扫描 Controller 方法参数中的 DTO 包
reqDTOPackage: com.company.proj.biz.dto.req

# 控制层 @ResponseBody 业务数据类型所在包。
respDTOPackage: com.company.proj.biz.dto.resp

# 枚举所在包。扫描项目 `**/enums` 或 `public enum` 包 
enumPackage: com.company.proj.biz.enums

# Service 接口所在包。扫描 `public interface *Service` 包
servicePackage: com.company.proj.biz.service

# ServiceImpl 实现类所在包。扫描 `public class *ServiceImpl` 包
serviceImplPackage: com.company.proj.biz.service.impl

# Mapper 接口所在包。`@Mapper` 或 `public interface *Mapper` 所在包
mapperPackage: com.company.proj.biz.mapper

# Entity 所在包。扫描实体/POJO 包（如 model、entity）
entityPackage: com.company.proj.biz.entity

# Design 类所在包。Design 类是 Allison 1875 生成的临时中间文件，所以固定使用backPackage包下的design包
designPackage: com.company.proj.biz.design

# Mapper 方法 Param 类所在包。如 `dto.param`
paramDTOPackage: com.company.proj.biz.dto.param

# Mapper 方法 Record 类所在包。如 `dto.record`
recordDTOPackage: com.company.proj.biz.dto.record

# WholeDTO 所在包。如 `dto` 或 `xxx.dto`
wholeDTOPackage: com.company.proj.biz.dto

# mapper.xml 所在目录列表。扫描 `*Mapper.xml` 所在目录相对于pom.xml的路径，如果项目支持多种数据库，需要形成列表
mapperXmlDirs:
  - src/main/resources/mysql
  - src/main/resources/oracle

# 生成的代码的作用。使用 `git config user.name` 或者扫描代码过程中多次出现的@author
author: Allister

# 编译版本，可选值8、17、21。从 `pom.xml` 的 `maven.compiler.source` 或 Spring Boot 版本推断
javaVersion: 21

# 是否将 javax 迁移到 jakarta。从 Spring Boot 版本推断，Spring Boot 3.x 设为 `true`，2.x 设为 `false`
enableJavaxMoveToJakarta: false

# DataModel 是否不使用 Lombok。根据项目风格推断，默认false
isDataModelWithoutLombok: false

# 是否生成「Any modifications may be overwritten」声明，一律 true
enableNoModifyAnnounce: true

# -- doc-analyzer 工具的配置 -- #

# 依赖的外部项目目录或 Java 文件路径。不指定，让用户自行补充
dependencyDirsOrJavaFilePath: [ ]

# 全局 URL 前缀。从 `server.servlet.context-path` 或统一前缀约定推断
globalUrlPrefix: ''

# 文档输出目标，可选值：`MARKDOWN`、`YAPI`、`SHOWDOC`、`DSL`。固定为`MARKDOWN`
flushTo:
  - MARKDOWN

# Markdown 输出目录。固定为`api-docs`。
markdownDir: api-docs

# 接口DSL 输出目录。固定为`api-dsls`。
dslDir: api-dsls

# ShowDoc 开放 API 基础目录名。固定为`doc-analyzer`。
showdocBaseCatName: doc-analyzer

# YApi 地址与 Token。仅当 `flushTo` 含 `YAPI` 时询问。
yapiUrl: null
yapiToken: null

# ShowDoc 开放 API。仅当 `flushTo` 含 `SHOWDOC` 时询问。
showdocUrl: null
showdocApiKey: null
showdocApiToken: null

# 文档输出到markdown或ShowDoc时，每个Endpoint是否输出到单个markdown文件。根据项目需求推断，默认false
singleEndpointPerMarkdown: false

# 仅分析匹配的 Handler 方法全限定名（支持 `*`/`?`）。仅当Controller非常多时询问。
mvcHandlerQualifierWildcards: [ ]

# -- handler-transformer 工具的配置 -- #

# 是否「一个 Controller 对应一个 Service」。观察 Controller 是否共用一个 Service 接口；不确定则询问
enableOneService: true

# Service 接口源码路径。如果是多 Maven Module 项目，并且业务层与控制层位于不同的 Maven Module，那么填业务层src/main/java目录相对于控制层pom.xml文件的相对路径
serviceSourcePath: ../my-biz-service/src/main/java
serviceImplSourcePath: ../my-biz-service/src/main/java
dtoSourcePath: ../my-biz-service/src/main/java

# 数据库 JDBC URL。从调试或本地环境的 `application.yml` / `application.properties` 的 `spring.datasource.url` 推断，去掉库名，仅到端口
jdbcUrl: jdbc:mysql://127.0.0.1:3306

# 数据库用户名。从 `spring.datasource.username` 推断
userName: root

# 数据库密码。从 `spring.datasource.password` 推断
password: root

# Schema 名。从 `url` 中的库名或 `schema` 配置推断
schema:

# 要生成的表名列表。询问用户需要生成哪些表；不填表示 schema 下全部表。
tables:
  - teacher
  - student

# 数据库DML。不指定，后续用户需要时自行配置
ddl: null

# 是否为 query-transformer 生成 Design。本次配置固定为null。
enableGenerateDesign: true

# 分页参数风格，可选值`PAGE_NO_PAGE_SIZE`、`OFFSET_LIMIT`。通过询问得知。
pageParamStyle: OFFSET_LIMIT

# Entity 类名是否以 Entity 结尾。根据现有 Entity 命名进行推断。
isEntityEndWithEntity: false

# 逻辑删除的“已删”条件。本次配置固定为 `delete_flag = 1`
deletedSql: 'delete_flag = 1'

# 逻辑删除的“未删”条件。本次配置固定为 `delete_flag = 0`
notDeletedSql: 'delete_flag = 0'

# 生成 Entity 时文件已存在的策略。本次配置固定为 `OVERWRITE`
entityExistenceResolution: OVERWRITE

# -- query-transformer -- #

# 持久层源码路径。如果是多 Maven Module 项目，并且业务层与持久层位于不同的 Maven Module，那么填持久层src/main/java目录相对于业务层pom.xml文件的相对路径
persistenceSourcePath: ../my-biz-persist/src/main/java

# -- star-transformer -- #

# Whole DTO 后缀。本次配置固定为 `WholeDTO`
wholeDTONamePostfix: WholeDTO

# -- form-generator -- #

# 表单 DSL 文件路径。本次配置固定为 `./forms.yml`
dslPath: ./forms.yml

# 是否用 doc-analyzer 生成接口文档。本次配置固定为 `true`
enableDocAnalyzer: true

# 代码片段配置（嵌套对象，对应 Java 中 Config.CodeSnippet 内部类）
codeSnippet:

  # 分页对象全限定类名。在 Controller/Service 中搜索分页返回类型（如 `PageResult`、`Page`）
  pageTypeQualifier: com.company.proj.biz.common.Pageinfo

  # Spring MVC 请求方法统一返回类的全限定名。在项目中找返回值包装类
  requestResultQualifier: com.company.proj.common.RequestResult

  # 统一返回类型声明，${dataType} 为业务数据类型占位符
  requestResultTypeDeclaration: RequestResult<${dataType}>

  # 构造无业务数据的成功返回值的代码片段
  requestResultSuccessNoData: RequestResult.success()

  # 构造有业务数据的成功返回值的代码片段，${data} 为数据对象占位符
  requestResultSuccessWithData: RequestResult.success(${data})

  # Controller 上 @RequestMapping 路径模板。占位符 `${formName}` 代表表单名，根据其他 Controller 推断
  controllerRequestMapping: /api/v1/${formName}

  # 短 UUID 生成代码片段。建议搜索UUID相关的工具类或者使用默认值
  shortUuidGeneration: UUID.randomUUID().toString().replaceAll("-", "").toLowerCase()

  # 判断集合为空的代码片段（${list} 为列表占位符）。建议使用commons-collection4提供的CollectionUtils或者使用默认值
  collectionEmptyCheck: ${list} == null || ${list}.isEmpty()

  # 构造分页返回值的代码片段（${total} 总条数，${dtos} 当前页列表）。需要询问
  constructPageResult: "new PageResult<>(${total}, ${dtos})"

  # 构造空的分页返回值的代码片段。需要询问
  constructEmptyPageResult: "new PageResult<>()"

# -- 自定义 Module 绑定 （需要再<plugin>的<dependencies>标签中加入对应的 artifact） -- #

# 自定义 Module 绑定本次配置一律固定为null，占位留给用户后续修改
# docAnalyzerModule: null
# handlerTransformerModule: null
# persistenceGeneratorModule: null
# queryTransformerModule: null
# starTransformerModule: null
# formGeneratorModule: null
```

## 三、校验与后续

配置完成后，将 `examples/forms.yml` 拷贝到项目根目录，然后运行 `mvn -q allison1875:form-generator` 命令验证配置是否正确。
