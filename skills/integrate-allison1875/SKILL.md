---
name: integrate-allison1875
description: Configure .allison1875.yml for a Spring Boot project so allison1875 tools can run against it. Use when the user asks to integrate allison1875, configure .allison1875.yml, or set up a domain for allison1875 tools.
---

# 接入 Allison 1875

Allison 1875 是基于 Java AST 的代码生成 CLI，面向 Spring Boot + MyBatis 项目。接入的唯一产物是工程根目录下的 `.allison1875.yml`——它描述了每个业务领域各层代码的位置与项目的代码风格。本技能只负责正确地生成这份配置。

## 步骤

1. **扫描项目、推断字段**：按 `pom.xml → 源码包树 → Controller → application.properties → Mapper XML` 的顺序分析项目。每个字段如何从项目推断，见下方[全字段示例](#全字段-yml-示例)的行内注释。
2. **确定领域范围**：若用户已明确指出某业务/模块/领域，`domains` **只配该领域**，不必列出项目所有领域；用户未指定时再自行识别并询问。
3. **写入 .allison1875.yml**：在工程根目录写入，键名 camelCase 与 Java 字段名一致。可推断的字段直接填并简述依据；无法可靠推断的（密码、逻辑删除 SQL、flushTo 目标等）列出问题**询问用户**。
4. **校验配置**：allison1875 在运行任何工具时都会执行 `Config.fromYaml()` → 校验，失败抛 `Allison1875Exception` 并列出具体错误项（形如 `domains[0].xxx must not be empty`）。写完后先用下方[路径检查脚本](#module-路径检查脚本)确认所有 `*Module` 目录存在，再按报错修正直至通过。

## 关键约定

- **`*Module` 字段填绝对路径（指向 Maven 模块根目录），`*Package` 字段填实际源码包名**——两者别搞混。
- **单模块项目**：所有 `*Module` 填同一个绝对路径。**多模块项目**：各 `*Module` 可指向不同模块（如 Controller/DTO 在 web 模块、Mapper/Entity 在 dao 模块），按代码实际所在模块分别填写。
- **有默认值的字段可省略**——`applyDefaults()` 会自动填充。下方示例把它们都列出仅为参考，注释标了默认值。
- **占位包**：`designPackage`/`paramDTOPackage`/`recordDTOPackage`/`wholeDTOPackage` 是必填的，但项目里可能尚未使用。规划一个占位包名（如 `xxx.design`），运行时会自动创建目录。

## 全字段 .allison1875.yml 示例

字段含义、推断方法、必填性、默认值、校验规则都写在注释里。校验规则与源码 `Config.validate()` / `DomainConfig.validate()` 一一对应，违反将导致运行失败。

```yaml
# ==================== domains：业务领域 ====================
# 必填，至少一个领域。每个领域的全部 *Module / *Package / mapperXmlDirs / wholeDTOPackage 均为必填。
# 只配用户指定的领域即可。
domains:
  - name: user                  # 必填，领域名，与 CLI 的 --domain=xxx 匹配

    # -- 控制层：grep @RestController 定位 --
    controllerModule: /abs/path/to/web-module            # 必填，绝对路径
    controllerPackage: com.company.proj.user.controller  # 必填，@RestController 类所在包

    # -- DTO 层：reqDTO 与 respDTO 共用一个 module --
    dtoModule: /abs/path/to/web-module                   # 必填，绝对路径
    reqDTOPackage: com.company.proj.user.dto.req         # 必填，@RequestBody 类型所在包
    respDTOPackage: com.company.proj.user.dto.resp       # 必填，@ResponseBody 业务数据类型所在包

    # -- 枚举层 --
    enumModule: /abs/path/to/common-module               # 必填，绝对路径
    enumPackage: com.company.proj.user.enums             # 必填，枚举类所在包

    # -- 业务层：service 接口与 impl 可在同一 module/package --
    serviceModule: /abs/path/to/service-module           # 必填，绝对路径
    servicePackage: com.company.proj.user.service        # 必填，Service 接口所在包
    serviceImplModule: /abs/path/to/service-module       # 必填，绝对路径（可与 serviceModule 相同）
    serviceImplPackage: com.company.proj.user.service.impl  # 必填（impl 不一定在 .impl 子包，grep 确认）

    # -- 持久层：mapper/entity/design/param/record/XML 共用一个 module --
    persistenceModule: /abs/path/to/dao-module           # 必填，绝对路径（grep @Mapper 或 @MapperScan 定位）
    mapperPackage: com.company.proj.user.mapper          # 必填，Mapper 接口所在包
    entityPackage: com.company.proj.user.entity          # 必填，Entity 所在包（也可能叫 model，grep 确认）
    designPackage: com.company.proj.user.design          # 必填，可为占位（query-transformer 用）
    paramDTOPackage: com.company.proj.user.dto.param     # 必填，可为占位（Mapper 方法 Param 类）
    recordDTOPackage: com.company.proj.user.dto.record   # 必填，可为占位（Mapper 方法 Record 类）
    mapperXmlDirs:                                       # 必填，默认 [src/main/resources/mapper]
      - src/main/resources/mapper                        #   相对 persistenceModule basedir 的相对路径或绝对路径
    wholeDTOPackage: com.company.proj.user.dto.whole     # 必填，可为占位（star-transformer 用）

# ==================== 公共配置 ====================
author: Deolin                  # 可选，默认 "Allison 1875"；取 git config user.name 或问用户
isDataModelWithoutLombok: false # 可选，默认 false；pom.xml 有 lombok 依赖则 false
enableNoModifyAnnounce: true    # 可选，默认 true；生成代码顶部追加"可能被覆盖"声明
enableJavaxMoveToJakarta: false # 可选，默认 false；Spring Boot 3.x（用 jakarta.*）则 true
javaHome: null                  # 可选，默认 null（用系统 JDK）；目标项目 JDK ≠ 系统默认时填 JDK 安装目录

# ==================== handler-transformer ====================
enableOneService: false         # 可选，默认 false；true=一个 Controller 的所有 Handler 调同一个 Service

# ==================== persistence-generator ====================
# 校验：jdbcUrl 与 ddl 至少配一个。
# 校验：jdbcUrl 非空时，userName + password + schema 三者必须同时非空。
jdbcUrl: jdbc:mysql://127.0.0.1:3306  # 条件必填，从 application.properties 的 spring.datasource.*.jdbc-url 推断
userName: root                  # jdbcUrl 非空时必填
password: null                  # jdbcUrl 非空时必填
schema: my_database             # jdbcUrl 非空时必填，JDBC URL 中 /dbname? 的库名
ddl: null                       # 与 jdbcUrl 二选一；提供 DDL 文本则用内存 H2 构建表结构
tables: []                      # 可选，默认 []（= schema 下所有表）；可问用户或留空
enableGenerateDesign: true      # 可选，默认 true；是否生成 Design 类
isEntityEndWithEntity: true     # 可选，默认 true；Entity 类名是否以 Entity 结尾，看项目现有命名
deletedSql: null                # 可选；逻辑删除"已删"条件，只支持等式 SQL，从 Mapper XML grep（如 delete_flag = 1）
notDeletedSql: null             # 可选；逻辑删除"未删"条件（如 delete_flag = 0）

# ==================== star-transformer ====================
wholeDTONamePostfix: WholeDTO   # 可选，默认 "WholeDTO"

# ==================== doc-analyzer ====================
dependencyDirsOrJavaFilePath: [] # 可选，默认 []；Handler 签名依赖的外部项目路径，通常留空
globalUrlPrefix: ''             # 可选，默认 ""；看 server.servlet.context-path
flushTo:                        # 可选，默认 [MARKDOWN]；可选值 MARKDOWN / YAPI / SHOWDOC / DSL
  - MARKDOWN
markdownDir: api-docs           # 可选，默认 "api-docs"；校验：flushTo 含 MARKDOWN 时不能为 null
dslDir: api-dsls                # 可选，默认 "api-dsls"；校验：flushTo 含 DSL 时不能为 null
yapiUrl: null                   # 校验：flushTo 含 YAPI 时必填，⚠️ 询问用户
yapiToken: null                 # 校验：flushTo 含 YAPI 时必填，⚠️ 询问用户
showdocUrl: null                # 校验：flushTo 含 SHOWDOC 时必填，⚠️ 询问用户
showdocApiKey: null             # 校验：flushTo 含 SHOWDOC 时必填，⚠️ 询问用户
showdocApiToken: null           # 校验：flushTo 含 SHOWDOC 时必填，⚠️ 询问用户
showdocBaseCatName: doc-analyzer # 可选，默认 "doc-analyzer"
singleEndpointPerMarkdown: false # 可选，默认 false；每个 Endpoint 单独输出一个 Markdown 文件
mvcHandlerQualifierWildcards: null # 可选；限定分析的 Handler 方法全限定名，支持 * 和 ?
getEnumCodeMethodName: getCode  # 可选，默认 "getCode"；看项目枚举基类取 code 的方法名
getEnumTitleMethodName: getTitle # 可选，默认 "getTitle"；看项目枚举基类取 title 的方法名

# ==================== app-generator ====================
appDslPath: ./app.yml           # 可选，默认 "./app.yml"
appGeneratorOutputDir: ./output # 可选，默认 "./output"

# ==================== form-generator ====================
dslPath: ./forms.yml            # 可选，默认 "./forms.yml"

# ==================== codeSnippet：代码片段模板 ====================
codeSnippet:
  # requestResult 四件套：要么全不配（不使用统一返回类），要么四个全配。
  # 校验：配了其中任意一个，其余三个必须同时非空。从 Controller 的 import 推断统一返回类。
  requestResultQualifier: com.company.proj.common.RequestResult  # 统一返回类全限定名
  requestResultTypeDeclaration: RequestResult<${dataType}>       # 类型声明，${dataType} 为数据类型占位符
  requestResultSuccessNoData: RequestResult.success()            # 成功无数据的构造片段
  requestResultSuccessWithData: RequestResult.success(${data})   # 成功有数据，${data} 为数据对象占位符
  controllerRequestMapping: /api/v1/${formName}  # 可选，默认 "/api/v1/${formName}"；${formName} 为表单名占位符
  shortUuidGeneration: UUID.randomUUID().toString().replaceAll("-", "").toLowerCase()  # 可选，默认同此
  collectionEmptyCheck: ${list} == null || ${list}.isEmpty()  # 可选，默认同此；${list} 为列表占位符
  bizExceptionQualifier: com.company.proj.common.BizException # 可选，默认 "java.lang.RuntimeException"；grep 项目业务异常类

# ==================== Guice Module（通常不配）====================
# 以下字段均有默认全限定类名，仅在自定义 Module 时才需覆盖：
# docAnalyzerModule / handlerTransformerModule / persistenceGeneratorModule
# queryTransformerModule / starTransformerModule / formGeneratorModule / appGeneratorModule
```

## Module 路径检查脚本

写完后运行，确认所有 `*Module` 绝对路径真实存在：

```bash
grep "Module:" .allison1875.yml | grep "^[^#]" | awk -F': ' '{print $2}' | while read dir; do
  [ -d "$dir" ] && echo "✅ $dir" || echo "❌ MISSING: $dir"
done
```
