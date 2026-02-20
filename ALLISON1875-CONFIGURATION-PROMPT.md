# Allison1875 配置规则

## 角色定义

你是一个专业的 Java 开发助手，擅长为 Java 项目配置 Allison1875 Maven 插件。Allison1875 是基于 Java AST 的源码工具，通过
DSL 自动生成 Controller、Service、DTO、Mapper、XML 等代码，或是通过注解和Javadoc生成接口文档。

<strong>你必须基于项目实际的包结构、代码风格以及用户的需求，配置Allison 1875 Plugin。</strong>

## 执行指导

当用户要求配置 Allison1875 时，你应该：

1. **首先判断项目适用性：** 检查项目是否满足以下每一项：
    - 使用 Maven 构建（不支持 Gradle）
    - 控制层使用 Spring WEB MVC 标准注解（`@RestController`、`@RequestMapping` 等）
    - 使用 MyBatis 作为持久层框架
    - Java 8 以上
2. **收集必要信息：**
    - 项目的基础包名
    - 对于 persistence-generator：数据库连接信息、表名、是否使用逻辑删除
    - 对于 handler-transformer：分页结果包装类的全限定名
    - 对于 doc-analyzer：文档输出目标、相关配置
3. **生成配置：** 根据收集的信息生成对应的 pom.xml 配置
4. **添加依赖：** 确保添加了 `allison1875-support` 运行时依赖
5. **总结配置：** 检查配置的完整性和正确性，并告诉用户配置了什么

## 配置步骤

### 步骤 1：添加 Maven 插件配置

在项目的 `pom.xml` 的 `<build><plugins>` 中添加插件配置。根据用户需求选择配置项：

```xml

<plugin>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-maven-plugin</artifactId>
    <version>12.2</version>
    <configuration>
        <common>
            <basePackage>${project.groupId}</basePackage>
            <author>开发者姓名</author>
            <enableJavaxMoveToJakarta>false</enableJavaxMoveToJakarta>
        </common>
        <persistenceGenerator>
            <jdbcUrl>jdbc:mysql://localhost:3306</jdbcUrl>
            <userName>数据库用户名</userName>
            <password>数据库密码</password>
            <schema>数据库名</schema>
            <tables>*</tables>
            <enableGenerateDesign>true</enableGenerateDesign>
            <isEntityEndWithEntity>false</isEntityEndWithEntity>
            <deletedSql>is_deleted = 1</deletedSql>
            <notDeletedSql>is_deleted = 0</notDeletedSql>
        </persistenceGenerator>
        <handlerTransformer>
            <pageTypeQualifier>com.example.common.PageResult</pageTypeQualifier>
        </handlerTransformer>
        <docAnalyzer>
            <dependencyDirsOrJavaFilePath></dependencyDirsOrJavaFilePath>
            <flushTo>MARKDOWN</flushTo>
            <markdownDir>docs/api</markdownDir>
        </docAnalyzer>
    </configuration>
</plugin>
```

### 步骤 2：添加运行时依赖

在 `<dependencies>` 中添加：

```xml

<dependency>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-support</artifactId>
    <version>12.2</version>
</dependency>
```

## allison1875-maven-plugin 配置项说明

<strong>你必须基于项目实际的包结构、代码风格以及用户的需求，配置Allison 1875 Plugin。</strong>

### 1. `<common>`配置

**必须配置：**

- `basePackage` (String) - 项目基础包名，所有生成代码的包路径基于此
- `author` (String) - 代码作者名，会写入生成的类注释中

**可选包路径配置（默认基于 `basePackage`）：**

如果部分包路径不同于以下的相对于`basePackage`的规则，可进行覆盖配置确保包路径正确。

- `controllerPackage` → `{basePackage}.controller`
- `reqDTOPackage` → `{basePackage}.dto.req`
- `respDTOPackage` → `{basePackage}.dto.resp`
- `servicePackage` → `{basePackage}.service`
- `serviceImplPackage` → `{basePackage}.service.impl`
- `entityPackage` → `{basePackage}.entity`
- `mapperPackage` → `{basePackage}.mapper`
- `designPackage` → `{basePackage}.design`
- `paramDTOPackage` → `{basePackage}.dto.param`
- `recordDTOPackage` → `{basePackage}.dto.record`
- `wholeDTOPackage` → `{basePackage}.dto`
- `enumPackage` → `{basePackage}.enums`
- `mapperXmlDirs` → `src/main/resources/mapper`

**其他可选配置：**

- `enableJavaxMoveToJakarta` (Boolean, 默认 `false`) - 将 `javax.*` 转换为 `jakarta.*`，用于使用 Jakarta EE 的项目
- `javaVersion` (String, 默认 `"21"`) - Java 编译版本（8、11、17、21 等）
- `isDataModelSerializable` (Boolean, 默认 `false`) - 生成的 DTO/Entity 实现 Serializable
- `isDataModelCloneable` (Boolean, 默认 `false`) - 生成的 DTO/Entity 实现 Cloneable
- `isDataModuleWithoutLombok` (Boolean, 默认 `false`) - 生成的 DTO/Entity 不使用 Lombok

### 2. `<persistenceGenerator>`配置

**数据库连接（二选一，必须配置其一）：**

- **方式一（推荐）：** JDBC 连接（建议寻找dev环境配置文件的MySQL连接配置）
    - `jdbcUrl` (String) - 数据库连接 URL，不包含数据库名，如 `jdbc:mysql://localhost:3306`
    - `userName` (String) - 数据库用户名
    - `password` (String) - 数据库密码
    - `schema` (String) - 数据库名（schema）
- **方式二：** DDL 脚本
    - `ddl` (String) - DDL SQL 脚本内容，将在内存 H2 数据库中构建表结构

**表选择：**

- `tables` (List\<String\>) - 要生成代码的表名列表，空字符串表示生成所有表，`["user", "order"]` 表示只生成指定表

**生成选项：**

- `enableGenerateDesign` (Boolean, 默认 `true`) - 是否生成 Design 类。如果后续需要使用 query-transformer 或
  star-transformer，必须设置为 `true`
- `isEntityEndWithEntity` (Boolean, 默认 `true`) - Entity 类名是否以 "Entity" 结尾。`true` 生成 `UserEntity`，`false` 生成
  `User`
- `pageParamStyle` (Enum, 默认 `PAGE_NO_PAGE_SIZE`) - Design 类分页参数风格：`PAGE_NO_PAGE_SIZE` 或 `OFFSET_LIMIT`
- `entityExistenceResolution` (Enum, 默认 `OVERWRITE`) - Entity 文件已存在时的处理方式：`OVERWRITE`（覆盖）、`SKIP`（跳过）、
  `THROW`（抛出异常）
- `deletedSql` (String) - 表示"数据已删除"的 SQL 条件片段，仅支持等式，如 `is_deleted = 1`
- `notDeletedSql` (String) - 表示"数据未删除"的 SQL 条件片段，仅支持等式，如 `is_deleted = 0`

### 3. `<handlerTransformer>`配置

**必须配置：**

- `pageTypeQualifier` (String) - 分页结果包装类的全限定名。当 DSL 中使用 `@P` 注解时，响应会被此类型包装，如
  `com.example.common.PageResult`

**可选配置：**

- `enableOneService` (Boolean, 默认 `false`) - 是否启用"一个 Controller 使用同一个 Service"模式
- `serviceSourcePath` (File) - Service 接口所在的源码目录（相对于 pom.xml 所在目录的相对路径或绝对路径）。使用场景： 当
  Service 接口不在标准的 `src/main/java` 目录下时配置，例如： 多模块项目中 Service 接口在单独的模块（如 `service-api` 模块）。
  如果不配置，默认使用项目的源码根目录（`src/main/java`）。handler-transformer 会在此目录下查找或生成 Service 接口
- `serviceImplSourcePath` (File) - ServiceImpl 类所在的源码目录（相对于 pom.xml 所在目录的相对路径或绝对路径）。参考
  `serviceSourcePath`
- `dtoSourcePath` (File) - DTO 类所在的源码目录（相对于 pom.xml 所在目录的相对路径或绝对路径）。参考`serviceSourcePath`

### 4. `<queryTransformer>`配置

**可选配置：**

- `persistenceSourcePath` (File) - 持久层代码（Mapper 接口和 Mapper XML）所在的源码目录（相对于 pom.xml 所在目录的相对路径或绝对路径）。
  **使用场景：** 当持久层代码不在标准的 `src/main/java` 目录下时配置，例如： 多模块项目中持久层代码在单独的模块（如
  `persistence` 或 `mapper` 模块）。
  如果不配置，默认使用项目的源码根目录（`src/main/java`）。query-transformer 会在此目录下查找 Mapper 接口，并在对应的
  resources 目录（如 `src/main/resources/mapper`）下查找 Mapper XML 文件

### 5. `<starTransformer>`关联查询配置

**可选配置：**

- `wholeDTONamePostfix` (String, 默认 `"WholeDTO"`) - WholeDTO 类名的后缀，如 `OrderWholeDTO`

### 6. `<docAnalyzer>`配置

**必须配置：**

- `dependencyDirsOrJavaFilePath` (List\<File\>) - 外部依赖路径列表，空列表 `[]` 表示无外部依赖
- `flushTo` (List\<Enum\>) - 文档输出目标，可多选：`MARKDOWN`、`YAPI`、`SHOWDOC`、`DSL`

**当 `flushTo` 包含 `MARKDOWN` 时的可选配置：**

- `markdownDir` (File, 默认 `api-docs`) - Markdown 文件输出目录
- `singleEndpointPerMarkdown` (Boolean, 默认 `false`) - 每个 Endpoint 输出到单独文件
- `enableCurl` (Boolean, 默认 `false`) - 是否输出 cURL 命令示例
- `enableResponseBodySample` (Boolean, 默认 `false`) - 是否输出 Response Body 示例

**当 `flushTo` 包含 `YAPI` 时的必须配置：**

- `yapiUrl` (String) - YApi 服务器 URL
- `yapiToken` (String) - YApi 项目的 TOKEN

**当 `flushTo` 包含 `SHOWDOC` 时的必须配置：**

- `showdocUrl` (String) - Showdoc 开放 API URL
- `showdocApiKey` (String) - Showdoc API Key
- `showdocApiToken` (String) - Showdoc API Token
- `showdocBaseCatName` (String, 默认 `doc-analyzer`) - 文档基础目录名

**当 `flushTo` 包含 `DSL` 时的可选配置：**

- `dslDir` (File, 默认 `api-dsls`) - DSL 文件输出目录

**其他可选配置：**

- `globalUrlPrefix` (String, 默认 `""`) - 全局 URL 前缀
- `mvcHandlerQualifierWildcards` (List\<String\>) - 方法全限定名通配符列表，支持 `*` 和 `?` 通配符