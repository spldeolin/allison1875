# persistence-generator IT Case 迁移指南

> 将 `allison1875-maven-plugin/src/it/persistence-generator/` 下的集成测试迁移到 `allison1875-cli` 模块，
> 以 `Bootstrap.main()` 作为入口，使用 JUnit 5 驱动并编写 Java 断言。

---

## 1. 背景与目标

### 为什么迁移？

项目计划逐步废弃 `allison1875-maven-plugin` 模块。原 IT case 通过 `maven-invoker-plugin` 驱动，
调用 Mojo 入口执行。迁移后改为在 `allison1875-cli` 模块中，通过 `Bootstrap.main()` (CLI 入口)

+ JUnit 5 驱动集成测试。

### 迁移进度

| #  | Case 名                    | 状态    |
|----|---------------------------|-------|
| 1  | `basic-ddl`               | ✅ 已完成 |
| 2  | `all-column-types`        | ✅ 已完成 |
| 3  | `all-not-null`            | ✅ 已完成 |
| 4  | `composite-index`         | ✅ 已完成 |
| 5  | `composite-primary-key`   | ✅ 已完成 |
| 6  | `disable-design`          | ✅ 已完成 |
| 7  | `entity-no-suffix`        | ✅ 已完成 |
| 8  | `existing-mapper`         | ✅ 已完成 |
| 9  | `existing-xml-markers`    | ✅ 已完成 |
| 10 | `multi-table`             | ✅ 已完成 |
| 11 | `no-index-list-all`       | ✅ 已完成 |
| 12 | `no-lombok`               | ✅ 已完成 |
| 13 | `no-modify-announce-off`  | ✅ 已完成 |
| 14 | `no-primary-key`          | ✅ 已完成 |
| 15 | `offset-limit-page-style` | ✅ 已完成 |
| 16 | `soft-delete`             | ✅ 已完成 |

---

## 2. 架构与关键文件说明

### 2.1 核心调用链

```
JUnit 5 Test (@Test)
  → PersistenceGeneratorItBaseTest#runPersistenceGenerator(caseName)
    → 拷贝 test/resources/it/persistence-generator/{caseName}/ 到 target/it/{caseName}/
    → 加载 .allison1875.yml (Map 模式)
    → 解析所有 *Module 相对路径为绝对路径并回写 yml
    → 解析 mapperXmlDirs 相对路径为绝对路径并回写 yml
    → Bootstrap.main(["--tool=persistence-generator", "--config=..."])
      → Bootstrap.parseArgs() → Bootstrap.loadConfig() → Allison1875.letsGo()
        → resolveSourceRoots → Guice injector → PersistenceGenerator.process()
```

### 2.2 关键文件位置

| 文件            | 路径                                                                                                                        | 说明                         |
|---------------|---------------------------------------------------------------------------------------------------------------------------|----------------------------|
| **基类**        | `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/persistencegenerator/PersistenceGeneratorItBaseTest.java` | 封装通用流程，所有 test 继承此类        |
| **测试类**       | `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/persistencegenerator/XxxItTest.java`                      | 参照 BasicDdlItTest 编写新 test |
| **测试资源**      | `allison1875-cli/src/test/resources/it/persistence-generator/{caseName}/`                                                 | 每个 case 的资源目录              |
| **原 IT 源**    | `allison1875-maven-plugin/src/it/persistence-generator/{caseName}/`                                                       | 原始 IT case 目录              |
| **Bootstrap** | `allison1875-cli/src/main/java/com/spldeolin/allison1875/cli/Bootstrap.java`                                              | CLI 入口                     |

### 2.3 PersistenceGeneratorItBaseTest 基类功能

基类 `PersistenceGeneratorItBaseTest` 提供两个方法供子类调用：

```java
// 单 domain 场景（domains 只有一条，或不需要指定 domain）
private void runPersistenceGenerator(String caseName)

// 多 domain 场景（需要指定处理哪个 domain）
protected void runPersistenceGenerator(String caseName, String domainNam
```

基类内部流程：

1. **拷贝资源**：将 `test/resources/it/persistence-generator/{caseName}/` 递归拷贝到 `target/it/{caseName}/`
2. **解析并回写 yml**：以 SnakeYAML Map 模式加载 `.allison1875.yml`，将 `*Module` 相对路径字段解析为绝对路径、
   将 `mapperXmlDirs` 列表中的相对路径解析为基于 basedir 的绝对路径，然后回写
3. **调用 Bootstrap**：组装 `--tool=persistence-generator --config=... [--domain=...]` 参数，调用 `Bootstrap.main()`

子类通过 `basedir` 字段引用临时工作目录（`target/it/{caseName}/`），在 `@Test` 方法中编写断言。

### 2.4 与 handler-transformer 基类的区别

| 差异点             | HandlerTransformerItBaseTest                  | PersistenceGeneratorItBaseTest             |
|-----------------|-----------------------------------------------|--------------------------------------------|
| **资源前缀**        | `it/handler-transformer/{caseName}/`          | `it/persistence-generator/{caseName}/`     |
| **CLI tool 参数** | `--tool=handler-transformer`                  | `--tool=persistence-generator`             |
| **额外路径字段解析**    | 无                                             | `mapperXmlDirs`（相对路径列表→绝对路径列表）             |
| **输出验证对象**      | 改写后的 Controller + 生成的 DTO/Service/ServiceImpl | 新生成的 Entity + Mapper + Mapper XML + Design |
| **输入特点**        | Controller Java 源码中有 init 块                   | yml 中包含 `ddl` 字段或通过 JDBC 连接获取表结构           |
| **源码目录**        | 预置 Controller/DTO Java 源文件                    | 通常为空目录（代码由工具全量生成）                          |

---

## 3. 标准迁移步骤（逐 case 操作）

### Step 1 — 复制资源文件

```bash
# 源目录
SRC=allison1875-maven-plugin/src/it/persistence-generator/{case-name}

# 目标目录
DST=allison1875-cli/src/test/resources/it/persistence-generator/{case-name}

# 递归复制（排除 target/、invoker.properties、verify.groovy）
mkdir -p $DST
rsync -av --exclude='target/' --exclude='invoker.properties' --exclude='verify.groovy' \
  $SRC/ $DST/
```

需要复制的文件：

- `.allison1875.yml` — 配置文件（**需修改，见 Step 2**）
- `pom.xml` — fake Maven 项目 POM（**需修改，见 Step 3**）
- `src/main/java/...` — 包目录结构（大部分 case 下为**空目录**）
- `src/main/resources/mapper/` — Mapper XML 输出目录（通常为空）
- 如有预置 Java 文件（如 `existing-mapper` case 中的 `TAccountMapper.java`），需一并复制

**不需要复制**的文件：

- `invoker.properties` — maven-invoker 专用
- `verify.groovy` — 将用 Java 重写
- `target/` — 编译产物

### Step 2 — 修改 `.allison1875.yml`

#### 2a. 补齐 `*Module` 字段

原 yml 中 `DomainConfig` **缺少** `*Module` 字段。在 CLI/Bootstrap 模式下，
基类 `PersistenceGeneratorItBaseTest` 在回写 yml 时会把 null/空/`.` 解析为 basedir 绝对路径。

**规则**：对于每个 domain，检查并补齐以下 6 个字段：

| 字段                  | 原 yml 值 | 迁移后填写规则    |
|---------------------|---------|------------|
| `controllerModule`  | 通常缺失    | 单模块填 `"."` |
| `dtoModule`         | 通常缺失    | 单模块填 `"."` |
| `enumModule`        | 通常缺失    | 单模块填 `"."` |
| `serviceModule`     | 通常缺失    | 单模块填 `"."` |
| `serviceImplModule` | 通常缺失    | 单模块填 `"."` |
| `persistenceModule` | 通常缺失    | 单模块填 `"."` |

**示例（所有 persistence-generator case 均为单模块）**：

```yaml
domains:
  - name: basic
    controllerModule: "."      # ← 新增
    controllerPackage: com.example.controller
    dtoModule: "."             # ← 新增
    reqDTOPackage: com.example.dto.req
    respDTOPackage: com.example.dto.resp
    enumModule: "."            # ← 新增
    enumPackage: com.example.enums
    serviceModule: "."         # ← 新增
    servicePackage: com.example.service
    serviceImplModule: "."     # ← 新增
    serviceImplPackage: com.example.service.impl
    persistenceModule: "."     # ← 新增
    mapperPackage: com.example.mapper
    entityPackage: com.example.entity
    designPackage: com.example.design
    paramDTOPackage: com.example.dto.param
    recordDTOPackage: com.example.dto.record
    wholeDTOPackage: com.example.dto
    mapperXmlDirs:
      - src/main/resources/mapper
```

#### 2b. 字段完整性检查清单

对照 `DomainConfig.java` 中的 `@NotEmpty` 字段，确保 yml 中都有值：

- [ ] `name` — 必填
- [ ] `controllerModule` — **必须新增**
- [ ] `controllerPackage` — 必填
- [ ] `dtoModule` — **必须新增**
- [ ] `reqDTOPackage` — 必填
- [ ] `respDTOPackage` — 必填
- [ ] `enumModule` — **必须新增**
- [ ] `enumPackage` — 必填
- [ ] `serviceModule` — **必须新增**
- [ ] `servicePackage` — 必填
- [ ] `serviceImplModule` — **必须新增**
- [ ] `serviceImplPackage` — 必填
- [ ] `persistenceModule` — **必须新增**
- [ ] `mapperPackage` — 必填
- [ ] `entityPackage` — 必填
- [ ] `designPackage` — 必填
- [ ] `paramDTOPackage` — 必填
- [ ] `recordDTOPackage` — 必填
- [ ] `wholeDTOPackage` — 必填
- [ ] `mapperXmlDirs` — persistence-generator 必填

对照 `Config.java` 中的 `@NotEmpty` 字段：

- [ ] `codeSnippet.constructPageResult` — 必填
- [ ] `codeSnippet.constructEmptyPageResult` — 必填

#### 2c. persistence-generator 特有字段

persistence-generator 的 yml 中通常还包含以下特有字段：

| 字段                      | 说明                      | 处理规则 |
|-------------------------|-------------------------|------|
| `ddl`                   | DDL 文本，直接解析表结构          | 保留   |
| `enableGenerateDesign`  | 是否生成 Design 文件          | 保留   |
| `isEntityEndWithEntity` | Entity 类名是否以 Entity 结尾  | 保留   |
| `mapperXmlDirs`         | Mapper XML 输出目录列表（相对路径） | 保留   |
| `deletedSql`            | 软删除 SQL 条件              | 保留   |
| `notDeletedSql`         | 非软删除 SQL 条件             | 保留   |

### Step 3 — 修改 `pom.xml`

1. **删除整个 `<build>` 节点**（包含 `allison1875-maven-plugin` 声明）
2. **替换 `@allison1875.version@` 占位符**：将 `allison1875-support` 依赖的版本从 `@allison1875.version@` 替换为
   `13.0-SNAPSHOT`
3. **保留 `<dependencies>`**：lombok、allison1875-support 等，
   因为 `MavenProjectClassLoaderUtils.buildClassLoader()` 需要有效的 `pom.xml` 来执行
   `mvn dependency:build-classpath` 构建 ClassLoader

修改前：

```xml

<dependency>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-support</artifactId>
    <version>@allison1875.version@</version>
</dependency>
    <!-- ... -->
<build>
<plugins>
    <plugin>
        <groupId>com.spldeolin.allison1875</groupId>
        <artifactId>allison1875-maven-plugin</artifactId>
        <version>@allison1875.version@</version>
    </plugin>
</plugins>
</build>
```

修改后：

```xml

<dependency>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-support</artifactId>
    <version>13.0-SNAPSHOT</version>
</dependency>
    <!-- 删除整个 <build> 节点 -->
```

### Step 4 — 创建 JUnit 5 测试类

在 `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/persistencegenerator/` 下创建测试类，
类名建议用 `{UpperCamelCaseName}ItTest`（如 `basic-ddl` → `BasicDdlItTest`）。

**模板**：

```java
package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * {case-name} 集成测试。
 *
 * @author Deolin {yyyy-MM-dd}
 */
public class XxxItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("{case-name}");

        // === 以下断言从 verify.groovy 翻译 ===

        // 1. 验证 Entity 文件生成
        File entityFile = new File(basedir, "src/main/java/com/example/entity/TXxxEntity.java");
        assertTrue(entityFile.exists(), "TXxxEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        // ...

        // 2. 验证 Mapper 接口生成
        // ...

        // 3. 验证 Mapper XML 文件生成
        // ...

        // 4. 验证 Design 文件生成（如果 enableGenerateDesign=true）
        // ...
    }

}
```

### Step 5 — Groovy 断言 → Java 断言翻译对照表

| Groovy 断言                                                            | Java JUnit 5 断言                                                |
|----------------------------------------------------------------------|----------------------------------------------------------------|
| `assert x.contains("y") : "msg"`                                     | `assertTrue(x.contains("y"), "msg")`                           |
| `assert !x.contains("y") : "msg"`                                    | `assertFalse(x.contains("y"), "msg")`                          |
| `assert x.size() == N : "msg"`                                       | `assertEquals(N, x.size(), "msg")`                             |
| `assert x.exists() : "msg"`                                          | `assertTrue(x.exists(), "msg")`                                |
| `def files = dir.listFiles()?.findAll { it.name.endsWith(".java") }` | `File[] files = dir.listFiles((d, n) -> n.endsWith(".java"));` |
| `files.size() > 0`                                                   | `files != null && files.length > 0`                            |

**persistence-generator 特有的断言模式**：

1. **Entity 生成验证**：检查字段名（lowerCamelCase）、Java 类型映射（Long/String/BigDecimal/LocalDateTime等）、包声明
2. **Mapper 接口验证**：检查 `interface` 关键字、基础 CRUD
   方法（insert/batchInsert/updateById/deleteById/queryById/queryByIds）、索引方法
3. **Mapper XML 验证**：检查 resultMap、表名引用、SQL 方法、列名映射
4. **Design 文件验证**：检查 package 声明、类型声明存在

### Step 6 — 运行验证

```bash
mvn test -pl allison1875-cli -am -Dtest=XxxItTest -Dsurefire.failIfNoSpecifiedTests=false
```

需确保 BUILD SUCCESS、没有 ERROR 级别日志、最好没有堆栈信息。

注意：由于 `-am` 会编译所有依赖模块，这些模块中没有 `XxxItTest` 测试类，
需要加 `-Dsurefire.failIfNoSpecifiedTests=false` 避免 surefire 在非 CLI 模块中报"未找到测试"错误。

### Step 7 — 删除原 IT case 目录

验证通过后，删除 `allison1875-maven-plugin/src/it/persistence-generator/{case-name}/` 目录。

---

## 4. 全部 16 个 case 清单与特殊处理说明

### 4.1 标准 DDL case（单 domain、单模块、通过 DDL 文本驱动）

以下 case 的 yml 只需补齐 6 个 `*Module: "."` 字段即可，其余结构与 basic-ddl 一致：

| # | Case 名                  | 测试类名建议                      | 特殊配置             | 状态    |
|---|-------------------------|-----------------------------|------------------|-------|
| 1 | `basic-ddl`             | `BasicDdlItTest`            | 无                | ✅ 已完成 |
| 2 | `all-column-types`      | `AllColumnTypesItTest`      | 测试所有 MySQL 列类型映射 | ✅ 已完成 |
| 3 | `all-not-null`          | `AllNotNullItTest`          | 所有字段 NOT NULL    | ✅ 已完成 |
| 4 | `composite-index`       | `CompositeIndexItTest`      | 复合索引             | ✅ 已完成 |
| 5 | `composite-primary-key` | `CompositePrimaryKeyItTest` | 复合主键             | ✅ 已完成 |
| 6 | `multi-table`           | `MultiTableItTest`          | 多张表的 DDL         | ✅ 已完成 |
| 7 | `no-primary-key`        | `NoPrimaryKeyItTest`        | 无主键表             | ✅ 已完成 |

### 4.2 特殊配置 case

| #  | Case 名                    | 测试类名建议                       | 特殊配置说明                                              | 状态    |
|----|---------------------------|------------------------------|-----------------------------------------------------|-------|
| 8  | `disable-design`          | `DisableDesignItTest`        | `enableGenerateDesign: false`，不生成 Design 文件         | ✅ 已完成 |
| 9  | `entity-no-suffix`        | `EntityNoSuffixItTest`       | `isEntityEndWithEntity: false`，Entity 不加 Entity 后缀  | ✅ 已完成 |
| 10 | `existing-mapper`         | `ExistingMapperItTest`       | 预置 Mapper 文件，验证增量生成                                 | ✅ 已完成 |
| 11 | `existing-xml-markers`    | `ExistingXmlMarkersItTest`   | 预置 Mapper XML 文件，验证标记保留                             | ✅ 已完成 |
| 12 | `no-index-list-all`       | `NoIndexListAllItTest`       | 无索引时生成 listAll 方法                                   | ✅ 已完成 |
| 13 | `no-lombok`               | `NoLombokItTest`             | `isDataModelWithoutLombok: true`，生成手写 getter/setter | ✅ 已完成 |
| 14 | `no-modify-announce-off`  | `NoModifyAnnounceOffItTest`  | `enableNoModifyAnnounce: false`，不加"请勿修改"注释          | ✅ 已完成 |
| 15 | `offset-limit-page-style` | `OffsetLimitPageStyleItTest` | `pageParamStyle: OFFSET_LIMIT`                      | ✅ 已完成 |
| 16 | `soft-delete`             | `SoftDeleteItTest`           | 配置 `deletedSql`/`notDeletedSql`，生成软删除 SQL           | ✅ 已完成 |

### 4.3 特殊 case 说明

#### `existing-mapper` — 预置 Mapper 文件

- **特殊点**：`src/main/java/com/example/mapper/` 下预置了 `TAccountMapper.java`，验证工具在已有 Mapper 文件时的行为。
- **yml 处理**：补齐 `*Module: "."` 即可。
- **断言注意**：需验证预置文件是否被正确保留或覆盖，取决于 `entityExistenceResolution` 配置。

#### `existing-xml-markers` — 预置 XML 标记

- **特殊点**：`src/main/resources/mapper/TPaymentMapper.xml` 中预置了自定义 XML 标记内容。
- **yml 处理**：补齐 `*Module: "."` 即可。
- **断言注意**：验证工具生成的 XML 保留了原有自定义标记区域。

#### `soft-delete` — 软删除

- **特殊点**：yml 中配置 `deletedSql` 和 `notDeletedSql`，工具在生成的 SQL 中自动添加软删除条件。
- **yml 处理**：补齐 `*Module: "."`，保留 `deletedSql` 和 `notDeletedSql` 字段。
- **断言注意**：验证 Mapper XML 中的查询 SQL 包含软删除条件。

#### `no-lombok` — 无 Lombok

- **特殊点**：`isDataModelWithoutLombok: true`，生成的 Entity 不使用 Lombok 注解。
- **yml 处理**：补齐 `*Module: "."`，保留 `isDataModelWithoutLombok: true`。
- **断言注意**：验证 Entity 中包含 getter/setter 方法而非 `@Data` 注解。

---

## 5. 常见问题

### Q1: 测试报 `测试资源目录不存在`

基类通过 `ClassLoader.getResource("it/persistence-generator/{caseName}")` 定位资源目录。
确保：

1. 测试资源目录存在于 `allison1875-cli/src/test/resources/it/persistence-generator/{caseName}/`
2. 目录下至少有 `.allison1875.yml` 和 `pom.xml` 两个文件

**空目录问题**：Git 不跟踪空目录。persistence-generator 的 case 中许多包目录
（如 `com/example/entity/`、`com/example/mapper/`）是空的。rsync 复制后目录存在，
但 Git 不会跟踪。可以在空目录中添加 `.gitkeep` 文件，或者在测试中不依赖空目录的存在
（工具会自动创建缺失的目录）。

### Q2: `mvn dependency:build-classpath` 失败

`MavenProjectClassLoaderUtils` 内部执行 `mvn compile dependency:build-classpath` 子进程。
确保：

1. 本地 Maven 环境可用（`mvn` 在 PATH 中）
2. pom.xml 中的依赖能在本地仓库或远程仓库中解析
3. `allison1875-support` 已 install 到本地仓库（`mvn install -pl allison1875-support`）

### Q3: `@allison1875.version@` 占位符未替换

原 IT case 通过 `maven-invoker-plugin` 的 `filterProperties` 自动替换 `@allison1875.version@`。
迁移后不再有此机制，必须手动将所有 `@allison1875.version@` 替换为实际版本 `13.0-SNAPSHOT`。

### Q4: surefire 在非 CLI 模块中报"未找到测试"错误

由于 `-am` 参数会编译所有依赖模块，surefire 默认在每个模块中查找指定的测试类。
使用 `-Dsurefire.failIfNoSpecifiedTests=false` 避免此错误。

### Q5: SnakeYAML 回写 yml 后 Bootstrap 加载报错

基类使用 SnakeYAML 的 **Map 模式**加载和 dump yml。DDL 多行文本在 dump 后可能
使用不同的块标量样式（`|` vs `>`），但语义相同，Bootstrap 的 `Constructor(Config.class)`
能正确解析。如果仍有问题，检查 yml 中是否有不兼容的值类型。

### Q6: mapperXmlDirs 路径解析失败

`mapperXmlDirs` 在原 yml 中是相对路径（如 `src/main/resources/mapper`）。基类在回写 yml 时
会将其解析为基于 basedir（`target/it/{caseName}/`）的绝对路径。如果回写后路径不正确，
检查 `resolveFileRelativeToBasedir()` 方法的逻辑。

---

## 6. 迁移经验总结

### 6.1 persistence-generator 与 handler-transformer 迁移的关键差异

| 差异维度                       | handler-transformer              | persistence-generator                         |
|----------------------------|----------------------------------|-----------------------------------------------|
| **输入来源**                   | Controller Java 源码中的 init 块      | yml 中的 `ddl` 文本或 JDBC 连接                      |
| **输出位置**                   | 直接写回 source tree（改写 Controller）  | 直接写回 source tree（新建 Entity/Mapper/XML/Design） |
| **源码目录状态**                 | 预置完整的 Controller Java 文件         | 通常为空目录（代码由工具全量生成）                             |
| **基类额外路径解析**               | 无                                | `mapperXmlDirs`（相对路径列表→绝对路径列表）                |
| **allison1875-support 依赖** | 需要（init 块中可能用到 `@L`、`@P` 注解）     | 需要（Design 文件中引用 PropertyName 等）               |
| **pom.xml 版本占位符**          | 有 `@allison1875.version@`（需手动替换） | 有 `@allison1875.version@`（需手动替换）              |
| **特有 yml 字段**              | `enableOneService`               | `ddl`、`enableGenerateDesign`、`deletedSql` 等   |

### 6.2 mapperXmlDirs 路径解析是核心差异

persistence-generator 的 `DomainConfig.mapperXmlDirs` 是一个相对路径列表（如 `["src/main/resources/mapper"]`），
在 Mojo 模式下由 `Allison1875.resolveSourceRoots()` 中的 `persistenceModule.resolve(dir.toPath())` 转换为绝对路径。

在 CLI 模式下，基类需要在 SnakeYAML Map 模式中手动解析这些相对路径为基于 basedir 的绝对路径，
然后回写到 yml 中。这是 `PersistenceGeneratorItBaseTest` 相比 `HandlerTransformerItBaseTest`
的唯一结构性差异。

### 6.3 空目录和 Git 跟踪

persistence-generator 的 IT case 中，大部分 `src/main/java/com/example/` 下的子目录
（entity/mapper/design/dto/param/record）是空的，因为代码完全由工具从 DDL 生成。
Git 不跟踪空目录，rsync 复制后虽然存在但不会被 Git 提交。

**解决方案**（选一）：

1. 在空目录中添加 `.gitkeep` 文件
2. 不依赖空目录——工具在写入文件时会自动创建缺失的目录（推荐）

### 6.4 DDL 多行文本在 SnakeYAML dump 后的格式变化

原 yml 中的 DDL 使用 `|`（literal block scalar）保持换行。SnakeYAML dump 回写后
可能改变块标量样式，但不影响语义。Bootstrap 的 `Constructor(Config.class)` 解析器
能正确处理。

### 6.5 批量迁移效率总结（2026-05-14）

本次完成 15 个 case 的批量迁移（#2-#16），以下为实操总结：

**6.5.1 统一度高，适合批处理**

所有 16 个 persistence-generator IT case 的 yml 结构高度一致：

- 单 domain（`name: basic`）、单模块（所有 `*Module` 填 `"."`）
- `*Package` 字段完全相同（`com.example.xxx`）
- DDL 驱动（通过 yml 中 `ddl` 字段，非 JDBC）
- pom.xml 结构完全相同（仅 `artifactId` 不同）

因此批量处理效率极高。推荐使用脚本批量完成资源拷贝、pom.xml 修改、yml 修改三个步骤。

**6.5.2 pom.xml 修改可全部用脚本完成**

所有 case 的 pom.xml 修改内容完全相同：

1. `@allison1875.version@` → `13.0-SNAPSHOT`
2. 删除 `<build>` 节点

可写一个脚本批量处理所有 pom.xml。本迁移使用 `sed + python3` 完成。

**6.5.3 yml 修改可全部用脚本完成**

所有 case 的新增字段完全相同（6 个 `*Module: "."`）。使用 `replace_in_file` 匹配
`domains:\n  - name: basic\n    controllerPackage:` 替换为带 Module 字段的版本。

**6.5.4 特殊 case 无需额外处理**

以下 case 有特殊配置或预置文件，但迁移步骤与标准 case 完全一致：

| Case                      | 特殊之处                             | 迁移影响        |
|---------------------------|----------------------------------|-------------|
| `existing-mapper`         | 预置 `TAccountMapper.java`         | 直接复制，无需修改   |
| `existing-xml-markers`    | 预置 `TPaymentMapper.xml`          | 直接复制，无需修改   |
| `no-lombok`               | `isDataModelWithoutLombok: true` | 原 yml 已有，保留 |
| `soft-delete`             | `deletedSql`/`notDeletedSql`     | 原 yml 已有，保留 |
| `offset-limit-page-style` | `pageParamStyle: OFFSET_LIMIT`   | 原 yml 已有，保留 |

这说明原 invoker-plugin 模式的 yml 与 CLI 模式需要的字段差异**仅在于 6 个 `*Module` 字段**。

**6.5.5 verify.groovy → Java 断言翻译经验**

- Groovy 的 `file.text` → Java `new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)`
- Groovy 的 `!file.exists()` → Java `assertFalse(file.exists(), "...")`
- Groovy 的 `def files = dir.listFiles()?.findAll { ... }` → Java `File[] files = dir.listFiles((d, n) -> ...);`
- Groovy 的 `assert files.size() >= N` → Java `assertTrue(files != null && files.length >= N, ...)`
- Groovy 的 `int pos1 = s.indexOf("a")` + `assert pos1 < pos2` → Java 一致

**6.5.6 context classloader 恢复机制**

基类 `PersistenceGeneratorItBaseTest` 在调用 `Bootstrap.main()` 前后保存/恢复当前线程的 context classloader。
这是因为 `Bootstrap → DefaultAstForest` 会用 IT case 的 URLClassLoader 替换它，导致后续测试中
`Resources.getResource()` 等 classpath 查找失败。此机制对所有 persistence-generator IT case 都适用。

---

## 7. 附录：模板 case 文件参考

### `BasicDdlItTest.java`（已验证通过）

```java
package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

public class BasicDdlItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("basic-ddl");

        // 1. 验证 Entity 文件生成
        File entityFile = new File(basedir, "src/main/java/com/example/entity/TOrderEntity.java");
        assertTrue(entityFile.exists(), "TOrderEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(entityContent.contains("id"), "Entity should contain field 'id'");
        assertTrue(entityContent.contains("orderNo"), "Entity should contain field 'orderNo'");
        assertTrue(entityContent.contains("userId"), "Entity should contain field 'userId'");
        assertTrue(entityContent.contains("amount"), "Entity should contain field 'amount'");
        assertTrue(entityContent.contains("status"), "Entity should contain field 'status'");
        assertTrue(entityContent.contains("createdAt"), "Entity should contain field 'createdAt'");
        assertTrue(entityContent.contains("updatedAt"), "Entity should contain field 'updatedAt'");

        assertTrue(entityContent.contains("Long"), "Entity should contain Long type");
        assertTrue(entityContent.contains("String"), "Entity should contain String type");
        assertTrue(entityContent.contains("BigDecimal"), "Entity should contain BigDecimal type");
        assertTrue(entityContent.contains("LocalDateTime"), "Entity should contain LocalDateTime type");
        assertTrue(entityContent.contains("package com.example.entity"), "Entity should have correct package");

        // 2. 验证 Mapper 接口生成
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("interface"), "Mapper should be an interface");
        assertTrue(mapperContent.contains("insert"), "Mapper should contain insert");
        assertTrue(mapperContent.contains("batchInsert"), "Mapper should contain batchInsert");
        assertTrue(mapperContent.contains("updateById"), "Mapper should contain updateById");
        assertTrue(mapperContent.contains("deleteById"), "Mapper should contain deleteById");
        assertTrue(mapperContent.contains("queryById"), "Mapper should contain queryById");
        assertTrue(mapperContent.contains("queryByIds"), "Mapper should contain queryByIds");
        assertTrue(mapperContent.contains("queryByOrderNo"), "Mapper should contain queryByOrderNo");
        assertTrue(mapperContent.contains("queryByUserId"), "Mapper should contain queryByUserId");

        // 3. 验证 Mapper XML 文件生成
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(xmlContent.contains("resultMap"), "XML should contain resultMap");
        assertTrue(xmlContent.contains("t_order"), "XML should reference table name");
        assertTrue(xmlContent.contains("insert"), "XML should contain insert SQL");
        assertTrue(xmlContent.contains("order_no"), "XML should contain column order_no");

        // 4. 验证 Design 文件生成
        File designDir = new File(basedir, "src/main/java/com/example/design");
        File[] designFiles = designDir.listFiles((d, n) -> n.endsWith(".java") && n.contains("TOrder"));
        assertTrue(designFiles != null && designFiles.length > 0, "Design file should be generated");
    }

}
```

### `.allison1875.yml` 模板（DDL 驱动的 case）

```yaml
# {case-name} integration test config
domains:
  - name: basic
    controllerModule: "."
    controllerPackage: com.example.controller
    dtoModule: "."
    reqDTOPackage: com.example.dto.req
    respDTOPackage: com.example.dto.resp
    enumModule: "."
    enumPackage: com.example.enums
    serviceModule: "."
    servicePackage: com.example.service
    serviceImplModule: "."
    serviceImplPackage: com.example.service.impl
    persistenceModule: "."
    mapperPackage: com.example.mapper
    entityPackage: com.example.entity
    designPackage: com.example.design
    paramDTOPackage: com.example.dto.param
    recordDTOPackage: com.example.dto.record
    wholeDTOPackage: com.example.dto
    mapperXmlDirs:
      - src/main/resources/mapper

author: test-author
javaVersion: "8"
enableNoModifyAnnounce: true
enableJavaxMoveToJakarta: false
isDataModelWithoutLombok: false
enableGenerateDesign: true
isEntityEndWithEntity: true

ddl: |
  CREATE TABLE `t_xxx` (
    ...
  ) ENGINE=InnoDB COMMENT='xxx表';

codeSnippet:
  constructPageResult: "new PageResult<>(${total}, ${dtos})"
  constructEmptyPageResult: "new PageResult<>()"
```

### `pom.xml` 模板

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.spldeolin.allison1875.it</groupId>
    <artifactId>{case-name}</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.spldeolin.allison1875</groupId>
            <artifactId>allison1875-support</artifactId>
            <version>13.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

</project>
```
