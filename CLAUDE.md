# Allison 1875 — AI Coding 规约

## 项目概览

Java AST 源码分析与转换工具集，以 CLI fat jar (`allison1875-cli`) 分发，面向 Spring Boot + MyBatis 目标项目。
从 Java DSL、数据库 Schema 或 YAML DSL 生成 Controller / Service / DTO / Mapper / XML 样板代码。

## 技术栈

Java 21 · Maven 多模块 · 版本 `13.0-SNAPSHOT`
DI: Google Guice 5.1.0（非 Spring）· 校验: jakarta.validation + Hibernate Validator（非 javax）
AST: JavaParser 3.28.1 · 日志: SLF4J + Logback · 测试: JUnit 5 + JaCoCo

## 模块地图

```
allison1875/                            parent POM
├── common/                             核心：AST forest、Config、DI、工具类、基础服务
├── allison1875-support/                目标项目的运行时依赖（@L, @P 注解）
├── allison1875-cli/                    CLI 入口 (Entrypoint.main) + 全部 JUnit 5 IT
├── handler-transformer/                Java DSL → Controller + Service + DTO
├── persistence-generator/              DB DDL → Entity + Mapper + Mapper XML + Design
├── query-transformer/                  QueryChain DSL → MyBatis CRUD + SQL
├── star-transformer/                   StarChain DSL → join 查询 + 数据组装
├── doc-analyzer/                       Controller → API 文档
├── form-generator/                     YAML form DSL → 完整 CRUD 栈（composite tool）
└── app-generator/                      app.yml DSL → fullstack jar（前后端一体）
```

## 构建与测试命令

```bash
# 编译
mvn compile

# 执行全部测试（单元测试 + 集成测试），并生成 JaCoCo 报告
mvn verify
# → allison1875-cli/target/site/jacoco-aggregate/index.html

# 执行单个测试类
mvn test -pl allison1875-cli -am -Dtest=BasicMarkdownItTest
```

## 关键安全规则

### 绝对禁止

1. 工具自身源码中使用 `javax.validation` — 必须用 `jakarta.validation`
2. 假设工具运行在 Spring 上 — 它用 Google Guice
3. 未理解完整 Guice + DomainContext 生命周期就修改 `Allison1875.java`、`Allison1875Module.java`、`Entrypoint.java`
4. 跳过 `importExprService.extractQualifiedTypeToImport(cu)` 直接调 `CompilationUnitUtils.writeJava(cu)`
5. 跳过 `DomainContext.set(domainConfig)` — 所有 tool 依赖 `DomainContext.get()` 非 null
6. 在生产代码中使用 `System.out.println()` — 用 `log.info/debug/warn/error`
7. 使用泛型异常 — 用 `Allison1875Exception`
8. 手写 Java 源文件 — 必须通过 `CompilationUnitUtils.writeJava(cu)` 持久化 AST 变更
9. 发明不存在的 API、类、注解、配置项

### 必须遵守

1. 新 tool 必须同时注册到 `ToolEnum` 和 `Config.<tool>Module`
2. 迭代 `AstForestContext.get()` 前检查其非空
3. IO 异常包装为 `UncheckedIOException` 或 `Allison1875Exception`
4. `FileSnapshotUtils.createSnapshot()` 每次 `process()` 最多调一次

### 性能

- AST 迭代（`for (CompilationUnit cu : AstForestContext.get())`）是热路径，循环内避免冗余解析
- 定向查找用 `tryFindCu(qualifiedName)` 而非遍历全部 CU
- `FileSnapshotUtils.createSnapshot()` 会拷贝整棵目录树，开销大，仅在确需回滚时使用

## 代码风格

### 命名

| 元素  | 规则                     | 示例                    |
|-----|------------------------|-----------------------|
| 类   | UpperCamelCase         | `HandlerTransformer`  |
| 接口  | + `Service` 后缀         | `DTOService`          |
| 实现  | + `Impl` 后缀            | `DTOServiceImpl`      |
| 枚举  | + `Enum` 后缀            | `FlushToEnum`         |
| DTO | + `Arg`/`Retval`/`DTO` | `DataModelArg`        |
| 工具类 | 复数名词 + `Utils`         | `JsonUtils`           |
| 常量  | UPPER_SNAKE_CASE       | `SINGLE_INDENT`       |
| 包名  | 全小写无分隔                 | `handlertransformer`  |
| 测试  | + `Test` / `ItTest`    | `BasicMarkdownItTest` |

方法命名动词前缀：`detect*`, `generate*`, `analyze*`, `validate*`

### DTO 模板

```java

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XxxArg {              // 入参: *Arg

    @NotNull
    Type field;           // jakarta.validation.constraints.*

}

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XxxRetval {           // 出参: *Retval

    Type field;

}
```

### Lombok 用法

- DTO: `@Data`, `@Accessors(chain = true)`, `@FieldDefaults(level = AccessLevel.PRIVATE)`
- Config: `@Data`, `@FieldDefaults(level = AccessLevel.PRIVATE)`
- Enum: `@Getter`, `@AllArgsConstructor`
- Service impl / main class: `@Slf4j`
- Module: `@Slf4j`, `@ToString`

### Import 顺序

`java.*` → `javax.*` → `jakarta.*` → 第三方 (`org.*`, `com.*`) → 项目内 (`com.spldeolin.allison1875.*`) → `lombok.*`

无通配符导入。

### Javadoc

`/** @author Deolin yyyy-MM-dd */`

### 工具类

私有构造函数抛 `UnsupportedOperationException("Never instantiate me.")`，仅含静态方法。

## DI 模式

- 接口用 `@ImplementedBy(XxxServiceImpl.class)`，实现用 `@Singleton` + `@Slf4j`
- 通过 `@Inject` 字段注入，Module 必须有单参数 `Config` 构造函数（反射实例化）
- 详细代码模板见 `common/CLAUDE.md`

## AST 处理管道

处理顺序：detect → analyze → generate → modify CU → `importExprService.extractQualifiedTypeToImport(cu)` →
`CompilationUnitUtils.writeJava(cu)` → log REFORMAT。详细模板见 `common/CLAUDE.md`。

## ExpansionService 模式

接口用 `@ImplementedBy` 声明默认实现，通过 Guice 模块绑定覆盖。用于跨工具扩展而不引入硬依赖。

**机制：** 工具 A（如 form-generator）定义 ExpansionService 接口 + 无操作默认实现。工具 B（如 app-generator）通过 `Allison1875.prepareDomain()` + 自行构建注射器 + `Modules.override()` 注入自定义实现。

**`prepareDomain(Config, String)` 用法：** 调用此方法完成 domain 解析 + sourceRoot 解析 + DomainContext 设置后，调用方可自行构建注射器（跳过 `letsGo()` 的标准流程）。

**现有实例：**
- `ServiceLayerExpansionService` — handler-transformer 的 Service 层代码扩展点
- `CommonItemsExpansionService` — form-generator 的公共字段注入扩展点
- `MutationExpansionService` — form-generator 的 Create/Update/List 方法体扩展点

## 开发工作流

1. **新 IT 用例**：必须继承对应的 `<Tool>ItBaseTest`，资源放 `allison1875-cli/src/test/resources/it/<tool>/<caseName>/`
2. **`.allison1875.yml` 中的 `*Module` 路径**：IT 中必须用相对路径（base class 自动转绝对路径）
3. **IT 中不要直接调 `Entrypoint.main`**：用 base class 的 wrapper（它会保存/恢复 ClassLoader）
4. **`target/it/<caseName>/`** 每次测试重建，不要跨测试引用

### 新增 Tool 模块的 Wiring 步骤

1. 在 `Config` 中新增字段（如 `xxxModule = "com.spldeolin.allison1875.xxx.XxxModule"`）
2. 在 `ToolEnum` 中新增条目：`XXX("xxx", Config::getXxxModule, false)`
3. 在 `allison1875-cli/pom.xml` 的 `<dependencies>` 中添加新模块
4. （可选）在 `allison1875-cli/src/test/java/.../it/xxx/` 下新增 `XxxItBaseTest`

## 维护 CLAUDE.md

当改动涉及以下内容时，必须同步更新对应的 CLAUDE.md：

- 新增/删除/重命名模块 → 更新根 CLAUDE.md 的模块地图
- 新增/删除 tool → 更新模块地图 + 构建命令示例
- 变更 DSL 字段、类型、枚举 → 更新 `form-generator/CLAUDE.md`
- 变更 IT 基础设施（base class 行为、资源目录约定）→ 更新 `allison1875-cli/CLAUDE.md`
- 变更构建流程、测试命令、依赖版本 → 更新根 CLAUDE.md 对应段落
- 发现新的"AI 容易犯的错"→ 追加到安全规则
- 变更 `Config` / `DomainConfig` / `ConfigValidator` 字段 → 更新 `skills/integrate-allison1875/SKILL.md`

不需要更新的：具体业务逻辑实现、单个 bug fix、不影响约定的重构。

## 提交约定

### 基本原则

- 每个 commit 只包含**一件事**；如果当前未提交内容涉及多件事，必须拆分为多个 commit
- 提交信息使用全英文
- 不提交绝对路径、IDE 配置、.DS_Store

### 格式

```
<type>: <subject>

1. First functional change
2. Second functional change
3. ...
```

- 标题（第一行）：`<type>: <subject>`，不超过 50 字符
- 标题与正文之间空一行
- 正文条目描述**功能层面**的变化，无需详细到技术实现
- 正文条目使用数字 + 点作为序号（`1.` `2.` `3.`）
- 如变更内容简单到一句标题即可概括，正文可省略

### 类型

| 类型         | 含义            |
|------------|---------------|
| `feat`     | 新功能           |
| `fix`      | Bug 修复        |
| `refactor` | 重构（不改变外部行为）   |
| `perf`     | 性能优化          |
| `test`     | 测试相关          |
| `build`    | 构建/依赖变更       |
| `docs`     | 文档变更          |
| `chore`    | 杂项（不影响源码或测试）  |

### Breaking Change

不兼容变更在类型后加 `!`：

```
feat!: redesign form DSL schema

1. Replace flat field list with nested group structure
2. Remove deprecated shorthand syntax
```

### 示例

```
feat: support dynamic sorting in list API

1. Add sortBy and isAsc fields to list request DTO
2. Generate sort enum from sortable items in DSL
3. Remove hardcoded order clause from mapper layer
```

```
fix: prevent duplicate enum entries in sort generation
```

## Skills

- `/integrate-allison1875` — 在目标 Spring Boot 项目中接入 allison1875：安装 CLI、分析项目结构、生成 `.allison1875.yml` 配置。当用户要求对某个项目执行 allison1875 工具时使用。

## 渐进式参考

以下文件在对应目录工作时自动加载，提供更深入的上下文：

- `common/CLAUDE.md` — DI 模板、AST 管道模板、Config Validation、File Snapshot
- `form-generator/CLAUDE.md` — forms.yml DSL 完整语法参考
- `query-transformer/CLAUDE.md` — Design chain DSL 赋值规则（编译时 vs 转换时类型）
- `app-generator/CLAUDE.md` — app.yml DSL 参考、处理流程、骨架资源与 form-generator 委托；内含功能权限体系等子文档索引
- `allison1875-cli/CLAUDE.md` — IT 测试开发规范与模式
- `skills/integrate-allison1875/SKILL.md` — 接入配置指南（Config 字段参考、校验规则、推理方法论）
