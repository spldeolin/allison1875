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

- **业务领域（domains）**：配置的核心概念。每个 domain 代表一个业务领域（如 user、order），描述该领域各层代码的**模块位置**和*
  *包名**。通过 `domains` 列表配置，列表中的每个元素对应一个 `DomainConfig` 对象。单模块项目只需配一个 domain 且无需填写
  `*Module` 字段；多模块项目通过 `*Module` 字段指定各层所在的 Maven 子模块相对路径（相对于 parent
  pom.xml）。一个项目可能包含多个业务领域，但 `domains` 列表中**只需声明用户指定要集成 allison1875 的领域**，无需列出项目中的所有领域。
- **可推断**：通过扫描项目包结构、已有 Controller/Service/Mapper/Entity、`application.yml`、JDBC 等推断，直接填写并简要说明依据。
- **需询问**：无法从代码或配置中可靠推断时（如密码、是否逻辑删除、分页类型等），在技能流程中列出问题，询问用户后再填。
- **不可省略**：即便部分字段可直接使用默认值，也必须在`.allison1875.yml`中显式注明。

以下通过YAML示例的形式，按模块分组列举每个字段，并说明含义、如何推断和何时询问。

```yaml
# 业务领域配置列表，描述各领域的代码位置。
# 每个 domain 描述一个业务领域各层代码所在的 Maven 模块和包名。
# *Module 字段为相对于 parent pom.xml 的 Maven 子模块路径，单模块项目可不填（默认使用当前执行的模块）。
domains:
  - name: user                         # 业务领域名称，用于 -Ddomain 参数匹配

    # -- 控制层 --
    controllerModule: my-web            # Controller 所在 Maven 子模块路径，单模块项目不填
    controllerPackage: com.company.proj.user.controller  # @RestController/@Controller 所在包

    # -- DTO层（reqDTO + respDTO 共用 module） --
    dtoModule: my-web                   # reqDTO/respDTO 所在 Maven 子模块路径，单模块项目不填
    reqDTOPackage: com.company.proj.user.dto.req         # 控制层 @RequestBody 类型所在包
    respDTOPackage: com.company.proj.user.dto.resp       # 控制层 @ResponseBody 业务数据类型所在包

    # -- 枚举层 --
    enumModule: my-common               # 枚举所在 Maven 子模块路径，单模块项目不填
    enumPackage: com.company.proj.user.enums             # 枚举所在包

    # -- 业务层 Service --
    serviceModule: my-service           # Service 接口所在 Maven 子模块路径，单模块项目不填
    servicePackage: com.company.proj.user.service        # Service 接口所在包

    # -- 业务层 ServiceImpl --
    serviceImplModule: my-service       # ServiceImpl 所在 Maven 子模块路径，单模块项目不填
    serviceImplPackage: com.company.proj.user.service.impl  # ServiceImpl 所在包

    # -- 持久层（mapper + entity + design + paramDTO + recordDTO + mapperXmlDirs 共用 module） --
    persistenceModule: my-dao           # 持久层所在 Maven 子模块路径，单模块项目不填
    mapperPackage: com.company.proj.user.mapper           # Mapper 接口所在包
    entityPackage: com.company.proj.user.entity           # Entity 类所在包
    designPackage: com.company.proj.user.design           # Design 类（query-transformer 中间文件）所在包
    paramDTOPackage: com.company.proj.user.dto.param      # Mapper 方法 Param 类所在包
    recordDTOPackage: com.company.proj.user.dto.record    # Mapper 方法 Record 类所在包
    mapperXmlDirs: # mapper.xml 所在目录（相对于持久层 module 的 basedir）
      - src/main/resources/mapper

    # -- WholeDTO层 --
    wholeDTOModule: my-service          # WholeDTO 所在 Maven 子模块路径，单模块项目不填
    wholeDTOPackage: com.company.proj.user.dto            # WholeDTO 类所在包

# 生成的代码的作者。使用 `git config user.name` 或者扫描代码过程中多次出现的 @author
author: Allister

# 编译版本，可选值 8、11、17、21。从 pom.xml 的 maven.compiler.source 或 Spring Boot 版本推断
javaVersion: '21'

# 是否将 javax 迁移到 jakarta。从 Spring Boot 版本推断，Spring Boot 3.x 设为 true，2.x 设为 false
enableJavaxMoveToJakarta: false

# DataModel 是否不使用 Lombok。根据项目风格推断，默认 false
isDataModelWithoutLombok: false

# 是否生成「Any modifications may be overwritten」声明，一律 true
enableNoModifyAnnounce: true

# ==================== handler-transformer 配置 ====================

# 是否「一个 Controller 对应一个 Service」。观察 Controller 是否共用一个 Service 接口；不确定则询问
enableOneService: true

# ==================== persistence-generator 配置 ====================

# 数据库 JDBC URL。从 application.yml 的 spring.datasource.url 推断
jdbcUrl: jdbc:mysql://127.0.0.1:3306

# 数据库用户名。从 spring.datasource.username 推断
userName: root

# 数据库密码。从 spring.datasource.password 推断
password: root

# Schema 名。从 url 中的库名或 schema 配置推断
schema: my_database

# 要生成的表名列表。询问用户需要生成哪些表；不填表示 schema 下全部表
tables:
  - teacher
  - student

# 使用指定的 DDL 在 In-memory H2 中构建表结构。不指定，后续用户需要时自行配置
ddl: null

# 是否为 query-transformer 生成 Design 类
enableGenerateDesign: true

# 分页参数风格，可选值 PAGE_NO_PAGE_SIZE、OFFSET_LIMIT。通过询问得知
pageParamStyle: PAGE_NO_PAGE_SIZE

# Entity 类名是否以 Entity 结尾。根据现有 Entity 命名进行推断
isEntityEndWithEntity: true

# 逻辑删除的"已删"条件（只支持等式 SQL）。询问用户是否有逻辑删除
deletedSql: 'delete_flag = 1'

# 逻辑删除的"未删"条件（只支持等式 SQL）
notDeletedSql: 'delete_flag = 0'

# 生成 Entity 时文件已存在的策略，可选值 OVERWRITE、RENAME
entityExistenceResolution: OVERWRITE

# ==================== star-transformer 配置 ====================

# WholeDTO 后缀
wholeDTONamePostfix: WholeDTO

# ==================== doc-analyzer 配置 ====================

# 依赖的外部项目目录或 Java 文件路径（相对于 pom 所在 basedir 的相对路径或绝对路径）。不指定，让用户自行补充
dependencyDirsOrJavaFilePath: [ ]

# 全局 URL 前缀。从 server.servlet.context-path 或统一前缀约定推断
globalUrlPrefix: ''

# 文档输出目标，可选值：MARKDOWN、YAPI、SHOWDOC、DSL
flushTo:
  - MARKDOWN

# Markdown 输出目录（相对于 pom 所在 basedir 的相对路径或绝对路径）
markdownDir: api-docs

# 接口 DSL 输出目录（相对于 pom 所在 basedir 的相对路径或绝对路径）
dslDir: api-dsls

# ShowDoc 开放 API 基础目录名
showdocBaseCatName: doc-analyzer

# YApi 地址与 Token。仅当 flushTo 含 YAPI 时询问
yapiUrl: null
yapiToken: null

# ShowDoc 开放 API。仅当 flushTo 含 SHOWDOC 时询问
showdocUrl: null
showdocApiKey: null
showdocApiToken: null

# 每个 Endpoint 是否输出到单个 markdown 文件。根据项目需求推断，默认 false
singleEndpointPerMarkdown: false

# 仅分析匹配的 Handler 方法全限定名（支持 * 和 ? 通配符）。仅当 Controller 非常多时询问
mvcHandlerQualifierWildcards: [ ]

# 获取枚举 Code 的方法名
getEnumCodeMethodName: getCode

# 获取枚举 Title 的方法名
getEnumTitleMethodName: getTitle

# ==================== form-generator 配置 ====================

# 表单 DSL 文件路径（相对于 pom 所在 basedir 的相对路径或绝对路径）
dslPath: ./forms.yml

# 是否用 doc-analyzer 生成接口文档
enableDocAnalyzer: true

# 代码片段配置（嵌套对象，对应 Java 中 Config.CodeSnippet 内部类）
codeSnippet:

  # 分页对象全限定类名。在 Controller/Service 中搜索分页返回类型（如 PageResult、Page）
  pageTypeQualifier: com.company.proj.common.PageInfo

  # Spring MVC 请求方法统一返回类的全限定名。在项目中找返回值包装类
  requestResultQualifier: com.company.proj.common.RequestResult

  # 统一返回类型声明，${dataType} 为业务数据类型占位符
  requestResultTypeDeclaration: RequestResult<${dataType}>

  # 构造无业务数据的成功返回值的代码片段
  requestResultSuccessNoData: RequestResult.success()

  # 构造有业务数据的成功返回值的代码片段，${data} 为数据对象占位符
  requestResultSuccessWithData: RequestResult.success(${data})

  # Controller 上 @RequestMapping 路径模板。占位符 ${formName} 代表表单名，根据其他 Controller 推断
  controllerRequestMapping: /api/v1/${formName}

  # 短 UUID 生成代码片段。建议搜索 UUID 相关的工具类或者使用默认值
  shortUuidGeneration: UUID.randomUUID().toString().replaceAll("-", "").toLowerCase()

  # 判断集合为空的代码片段（${list} 为列表占位符）。建议使用 commons-collection4 提供的 CollectionUtils 或者使用默认值
  collectionEmptyCheck: ${list} == null || ${list}.isEmpty()

  # 构造分页返回值的代码片段（${total} 总条数，${dtos} 当前页列表）。需要询问
  constructPageResult: "new PageResult<>(${total}, ${dtos})"

  # 构造空的分页返回值的代码片段。需要询问
  constructEmptyPageResult: "new PageResult<>()"

# -- 自定义 Module 绑定 （需要在 <plugin> 的 <dependencies> 标签中加入对应的 artifact） -- #

# 自定义 Module 绑定一律固定为 null，占位留给用户后续修改
# docAnalyzerModule: null
# handlerTransformerModule: null
# persistenceGeneratorModule: null
# queryTransformerModule: null
# starTransformerModule: null
# formGeneratorModule: null
```

## 三、校验与后续

配置完成后，将 `examples/forms.yml` 拷贝到项目根目录，然后运行
`mvn com.spldeolin.allison1875:allison1875-maven-plugin:13.0-SNAPSHOT:form-generator -Ddomain=my-domain-1 -N`
命令验证配置是否正确，如果因java版本导致失败，尝试使用`jenv`切换正确的Java版本