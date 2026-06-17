---
name: integrate-allison1875
description: Configure .allison1875.yml for a Spring Boot project and install the allison1875 CLI. Use when the user asks to integrate allison1875, configure .allison1875.yml, or run allison1875 tools against a Spring Boot project.
---

# 接入 Allison 1875

Allison 1875 是基于 Java AST 的代码生成 CLI 工具，面向 Spring Boot + MyBatis 项目，从 DSL / 数据库表 / YAML 生成
Controller、Service、DTO、Mapper、XML 等样板代码。

接入流程：收集项目信息 → 编写 `.allison1875.yml`。

## 一、信息收集方法论

按以下顺序分析目标项目，先定骨架再填细节：

```
pom.xml → 源码目录结构 → Controller → application.properties → Mapper XML
```

### 1.1 从 pom.xml 确定全局参数

| 关注点            | 推理目标                               |
|-----------------|------------------------------------|
| 子模块列表          | → 识别 api/application/domain/infrastructure 分层 |
| lombok 依赖      | → `isDataModelWithoutLombok`       |
| Spring Boot 版本 | → 3.x 则 `enableJavaxMoveToJakarta: true` |
| java.version    | → `javaHome`（若目标项目 JDK ≠ 系统默认 JDK）  |

### 1.2 从源码目录结构确定 domains

> **按需配置**：若用户明确指出了某个业务、模块或领域，`domains` 只需配置该指定 domain，不必列出项目所有领域。只有用户未指定时才需要自行识别并询问。

沿 `src/main/java` 包树识别**用户指定领域**的各层包名：

| 要找的东西               | 怎么找                           | 映射到                                |
|---------------------|-------------------------------|------------------------------------|
| `@RestController` 类 | grep `@RestController`        | `controllerPackage`                |
| req/resp DTO        | 包名含 `dto`、`req`、`resp`        | `reqDTOPackage` / `respDTOPackage` |
| 枚举类                 | 包名含 `enums`                   | `enumPackage`                      |
| Service 接口          | 包名含 `service`                 | `servicePackage`                   |
| ServiceImpl         | 包名含 `service.impl`            | `serviceImplPackage`               |
| Mapper 接口           | 包名含 `mapper`                  | `mapperPackage`                    |
| Entity/Model        | 包名含 `entity`/`model`          | `entityPackage`                    |
| Mapper XML          | `src/main/resources/mapper` 下 | `mapperXmlDirs`                    |

> **`*Module` 字段必须用绝对路径**，不是包名。

### 1.3 从 Controller 推理 codeSnippet

```bash
# 找统一返回类
grep "import.*Result\|import.*Response" controller/*.java

# 查看类定义确认泛型形式
# 例如：public class ApiBaseResult<T> { ... T result; }
# → requestResultQualifier: "com.company.proj.common.ApiBaseResult"
# → requestResultTypeDeclaration: "ApiBaseResult<${dataType}>"

# 查看静态工厂方法
# → requestResultSuccessNoData:  "ApiBaseResult.successRet()"
# → requestResultSuccessWithData: "ApiBaseResult.successRet(${data})"
```

> **requestResult 四件套（qualifier / typeDeclaration / successNoData / successWithData）是全有或全无的关系**——配了其中任何一个，其余三个必须同时配。未配任何一个时表示不使用统一返回类。

```bash
# 找业务异常类
grep -rn "throw new.*Exception\|import.*BizException\|import.*BusinessException" service/**/*.java | head -10
# → codeSnippet.bizExceptionQualifier（如 com.company.proj.common.BizException）
```

```bash
# jakarta vs javax
grep "import javax\.\|import jakarta\." controller/*.java
# → enableJavaxMoveToJakarta
```

### 1.4 从 application.properties 推理持久层

| properties 键                   | 映射到        |
|--------------------------------|------------|
| `spring.datasource.*.jdbc-url` | `jdbcUrl`  |
| `spring.datasource.*.username` | `userName` |
| `spring.datasource.*.password` | `password` |
| JDBC URL 中 `/dbname?`          | `schema`   |

### 1.5 从 Mapper XML 推理逻辑删除

```bash
grep "delete_flag\|is_deleted" mapper/*.xml | head -20
```

观察 `SET delete_flag = 1` 和 `AND delete_flag = 0` → 得出 `deletedSql` / `notDeletedSql`。

## 二、跨模块项目的 Module/Package 映射

多模块 Maven 项目中，一个业务领域的代码分布在多个模块。DomainConfig 的各 `*Module` 字段允许指向不同模块。

### 典型布局

```
project-root/
├── module-api/          ← Controller / DTO / Enum
├── module-service/      ← Service / ServiceImpl
└── module-core/         ← Mapper / Entity / Design / Mapper XML
```

| DomainConfig 字段                                 | 指向模块             |
|-------------------------------------------------|------------------|
| `controllerModule` / `dtoModule` / `enumModule` | `module-api`     |
| `serviceModule` / `serviceImplModule`           | `module-service` |
| `persistenceModule`                             | `module-core`    |

### 推理步骤

1. 从父 pom.xml 识别所有子模块
2. grep `@RestController` 定位 Controller 所在模块 → `controllerModule`
3. grep `@Mapper` 或 `import.*mybatis` 定位 Mapper 所在模块 → `persistenceModule`
4. 检查 `@MapperScan` 确认 Mapper 扫描包路径 → `mapperPackage`
5. grep `package.*entity` 或 `package.*model` 定位 Entity 包名 → `entityPackage`
6. application.properties 可能在启动模块而非业务模块 — 沿模块依赖链查找

### 占位包处理

以下字段是 `@NotEmpty` 必填的，但项目中可能尚未使用：

- `designPackage` — Design 类（query-transformer 使用）
- `paramDTOPackage` — Mapper 方法 Param 类
- `recordDTOPackage` — Mapper 方法 Record 类
- `wholeDTOPackage` — WholeDTO 类（star-transformer 使用）

在 `persistenceModule` 对应的业务子包下规划占位包名（如 `xxx.spec.param`），allison1875 运行时自动创建。

## 三、配置 `.allison1875.yml`

在**工程根目录**创建 `.allison1875.yml`。YAML 键名使用 camelCase，与 Java 字段名一致。

### 配置方式约定

- **可推断**：通过扫描项目得到，直接填写并简要说明依据。
- **需询问**：无法从代码可靠推断时（如密码、逻辑删除、flushTo 等），列出问题询问用户。
- **不可省略**：即便部分字段有默认值，也必须在 yml 中显式声明。
- **按需配置 domains**：若用户已明确指出业务/模块/领域，`domains` 只配置用户指定的 domain；未指定时再自行识别并询问。

### 完整字段示例

```yaml
# ==================== 业务领域 ====================
# *Module 字段必须为绝对路径，且目录必须存在。
# *Package 字段必须与实际源码包名一致。
# 只需配置用户指定的领域，无需列出项目所有领域。
# 单模块项目中所有 *Module 字段填同一个绝对路径。
domains:                               # @NotEmpty @Valid — 至少配一个 domain
  - name: user                         # @NotEmpty — 领域名，CLI --domain=xxx 匹配用

    # -- 控制层 --
    controllerModule: /abs/path/to/my-web        # @NotEmpty 绝对路径
    controllerPackage: com.company.proj.user.controller  # @NotEmpty

    # -- DTO 层（reqDTO + respDTO 共用 module）--
    dtoModule: /abs/path/to/my-web               # @NotEmpty 绝对路径
    reqDTOPackage: com.company.proj.user.dto.req   # @NotEmpty — @RequestBody 类型所在包
    respDTOPackage: com.company.proj.user.dto.resp # @NotEmpty — @ResponseBody 业务数据类型所在包

    # -- 枚举层 --
    enumModule: /abs/path/to/my-common           # @NotEmpty 绝对路径
    enumPackage: com.company.proj.user.enums     # @NotEmpty

    # -- 业务层 --
    serviceModule: /abs/path/to/my-service       # @NotEmpty 绝对路径
    servicePackage: com.company.proj.user.service          # @NotEmpty
    serviceImplModule: /abs/path/to/my-service   # @NotEmpty 绝对路径（可与 serviceModule 相同）
    serviceImplPackage: com.company.proj.user.service.impl # @NotEmpty（可与 servicePackage 相同）

    # -- 持久层（mapper/entity/design/paramDTO/recordDTO/XML 共用 module）--
    persistenceModule: /abs/path/to/my-dao       # @NotEmpty 绝对路径
    mapperPackage: com.company.proj.user.mapper    # @NotEmpty
    entityPackage: com.company.proj.user.entity    # @NotEmpty — 部分项目用 model 包名，grep 确认
    designPackage: com.company.proj.user.design    # @NotEmpty — 可为占位，运行时自动创建
    paramDTOPackage: com.company.proj.user.dto.param  # @NotEmpty — 可为占位
    recordDTOPackage: com.company.proj.user.dto.record # @NotEmpty — 可为占位
    mapperXmlDirs:                       # @NotEmpty — 相对于 persistenceModule basedir
      - src/main/resources/mapper

    # -- WholeDTO --
    wholeDTOPackage: com.company.proj.user.dto   # @NotEmpty — 可为占位

# ==================== 公共配置 ====================
author: Allister                       # @NotEmpty 默认"Allison 1875" — 取 git config user.name 或问用户
enableJavaxMoveToJakarta: false        # @NotNull 默认false — Spring Boot 3.x → true
isDataModelWithoutLombok: false        # @NotNull 默认false — pom.xml 有 lombok 依赖 → false
enableNoModifyAnnounce: true           # @NotNull 默认true — 一律 true
javaHome: null                         # 可选 — 执行mvn时使用的JDK路径，目标项目JDK≠系统默认时需配

# ==================== handler-transformer ====================
enableOneService: true                 # @NotNull 默认false — 一个Controller所有Handler调用同一个Service

# ==================== persistence-generator ====================
# 校验: jdbcUrl 和 ddl 至少配一个才能使用 persistence-generator（两者都不配时不报错但该工具不可用）
# 校验: 配了 jdbcUrl 则 userName + password + schema 三个必须同时非空
jdbcUrl: jdbc:mysql://127.0.0.1:3306   # 条件必填 — 从 application.properties 推理
userName: root                         # jdbcUrl非空时必填
password: root                         # jdbcUrl非空时必填 — ⚠️ 需询问用户
schema: my_database                    # jdbcUrl非空时必填 — JDBC URL 中的数据库名
ddl: null                              # 与 jdbcUrl 二选一 — DDL文本用H2内存库构建
tables: []                             # 可选 默认[] — 空=schema下全部表，问用户或留空
enableGenerateDesign: true             # @NotNull 默认true — 是否生成 Design 类
isEntityEndWithEntity: true            # @NotNull 默认true — Entity类名是否以Entity结尾，看项目现有命名
deletedSql: 'delete_flag = 1'          # 可选 — 逻辑删除"已删"条件，从 Mapper XML grep
notDeletedSql: 'delete_flag = 0'       # 可选 — 逻辑删除"未删"条件，从 Mapper XML grep

# ==================== star-transformer ====================
wholeDTONamePostfix: WholeDTO          # @NotNull 默认"WholeDTO"

# ==================== doc-analyzer ====================
dependencyDirsOrJavaFilePath: []       # @NotNull 默认[] — Handler签名依赖的外部项目路径，通常留空
globalUrlPrefix: ''                    # @NotNull 默认"" — 看 server.servlet.context-path
flushTo:                               # @NotEmpty 默认[MARKDOWN] — 可选 MARKDOWN/YAPI/SHOWDOC/DSL
  - MARKDOWN
# 校验: flushTo含MARKDOWN → markdownDir非空
markdownDir: api-docs                  # 默认"api-docs"
# 校验: flushTo含DSL → dslDir非空
dslDir: api-dsls                       # 默认"api-dsls"
# 校验: flushTo含YAPI → yapiUrl + yapiToken 非空
yapiUrl: null                          # 需询问用户
yapiToken: null                        # 需询问用户
# 校验: flushTo含SHOWDOC → showdocUrl + showdocApiKey + showdocApiToken 非空
showdocUrl: null                       # 需询问用户
showdocApiKey: null                    # 需询问用户
showdocApiToken: null                  # 需询问用户
showdocBaseCatName: doc-analyzer       # 默认"doc-analyzer"
singleEndpointPerMarkdown: false       # 默认false — 每个Endpoint单独一个Markdown文件
mvcHandlerQualifierWildcards: null     # 可选 — 限定分析的Handler方法通配符，支持*和?
getEnumCodeMethodName: getCode         # @NotNull 默认"getCode" — 看项目枚举基类
getEnumTitleMethodName: getTitle       # @NotNull 默认"getTitle" — 看项目枚举基类

# ==================== app-generator ====================
appDslPath: ./app.yml                  # @NotNull 默认"./app.yml"
appGeneratorOutputDir: ./output        # @NotNull 默认"./output"

# ==================== form-generator ====================
dslPath: ./forms.yml                   # @NotNull 默认"./forms.yml"

# ==================== codeSnippet（代码片段模板）====================
# 校验: requestResult 四件套（qualifier/typeDeclaration/successNoData/successWithData）
#       要么全不配，要么全配。配了任一个其余三个必须同时配。
codeSnippet:
  requestResultQualifier: com.company.proj.common.RequestResult  # 统一返回类全限定名 — Controller import推理
  requestResultTypeDeclaration: RequestResult<${dataType}>       # 统一返回类型声明，占位符${dataType}
  requestResultSuccessNoData: RequestResult.success()            # 成功无数据的构造片段
  requestResultSuccessWithData: RequestResult.success(${data})   # 成功有数据的构造片段，占位符${data}
  controllerRequestMapping: /api/v1/${formName}  # @NotEmpty 默认"/api/v1/${formName}" — 看项目URL规范
  shortUuidGeneration: UUID.randomUUID().toString().replaceAll("-", "").toLowerCase()  # @NotEmpty
  collectionEmptyCheck: ${list} == null || ${list}.isEmpty()  # @NotEmpty — 占位符${list}
  bizExceptionQualifier: com.company.proj.common.BizException  # @NotEmpty 默认"java.lang.RuntimeException" — grep项目业务异常类

# ==================== Guice Module 配置（通常不需要改）====================
# 各工具对应的 Guice Module 实现类全限定名，均有合理默认值，仅自定义 Module 时覆盖：
# docAnalyzerModule / handlerTransformerModule / persistenceGeneratorModule
# queryTransformerModule / starTransformerModule / formGeneratorModule / appGeneratorModule
```

## 四、常见陷阱

| 陷阱 | 解法 |
|------|------|
| Module 路径写成相对路径 | 始终用完整文件系统路径 |
| DTO 分散在多个子包 | 统一配为上层包 |
| serviceImplPackage 不一定是 service.impl | 可与 servicePackage 相同 |
| entityPackage 不一定叫 entity | grep 实际包名确认 |
| Config 有默认值 ≠ 可以不写 | 即便默认值也写出 |
| mapperXmlDirs 默认是 `mapper/` | 检查实际 resources 目录 |
| 跨模块持久层 | `persistenceModule` 指向持久层所在模块 |
| 混合持久层（MongoDB + MyBatis） | 只映射 MyBatis 部分 |
| 占位包不存在 | 规划占位包名，运行时自动创建 |
| application.properties 位于其他模块 | 沿模块依赖链查找 |
| requestResult 四件套缺项 | 要么全不配，要么全配 |
| bizExceptionQualifier 留默认值 | grep 项目业务异常类 |

### Module 路径校验脚本

```bash
grep "Module:" .allison1875.yml | grep "^[^#]" | awk -F': ' '{print $2}' | while read dir; do
  [ -d "$dir" ] && echo "✅ $dir" || echo "❌ MISSING: $dir"
done
```
