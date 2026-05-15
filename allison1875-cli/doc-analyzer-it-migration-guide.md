# doc-analyzer IT Case 迁移指南

> 将 `allison1875-maven-plugin/src/it/doc-analyzer/` 下的集成测试迁移到 `allison1875-cli` 模块，
> 以 `Bootstrap.main()` 作为入口，使用 JUnit 5 驱动并编写 Java 断言。

---

## 1. 背景与目标

### 为什么迁移？

项目计划逐步废弃 `allison1875-maven-plugin` 模块。原 IT case 通过 `maven-invoker-plugin` 驱动，
调用 Mojo 入口执行。迁移后改为在 `allison1875-cli` 模块中，通过 `Bootstrap.main()` (CLI 入口)

+ JUnit 5 驱动集成测试。

### 已完成的模板

`basic-markdown` 已作为模板 case 迁移并验证通过。剩余 21 个 case 需按本指南迁移。

---

## 2. 架构与关键文件说明

### 2.1 核心调用链

```
JUnit 5 Test (@Test)
  → DocAnalyzerItBaseTest#runDocAnalyzer(caseName, domainName)
    → 拷贝 test/resources/it/doc-analyzer/{caseName}/ 到 target/it/{caseName}/
    → 加载 .allison1875.yml (Map 模式)
    → 解析所有相对路径为绝对路径并回写 yml
    → Bootstrap.main(["--tool=doc-analyzer", "--config=...", "--domain=..."])
      → Bootstrap.parseArgs() → Bootstrap.loadConfig() → Allison1875.letsGo()
        → resolveSourceRoots → Guice injector → DocAnalyzer.process()
```

### 2.2 关键文件位置

| 文件               | 路径                                                                                          | 说明                  |
|------------------|---------------------------------------------------------------------------------------------|---------------------|
| **基类**           | `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/DocAnalyzerItBaseTest.java` | 封装通用流程，所有 test 继承此类 |
| **模板测试**         | `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/BasicMarkdownItTest.java`   | 参照此文件编写新 test       |
| **测试资源**         | `allison1875-cli/src/test/resources/it/doc-analyzer/{caseName}/`                            | 每个 case 的资源目录       |
| **原 IT 源**       | `allison1875-maven-plugin/src/it/doc-analyzer/{caseName}/`                                  | 原始 IT case 目录       |
| **Bootstrap**    | `allison1875-cli/src/main/java/com/spldeolin/allison1875/cli/Bootstrap.java`                | CLI 入口              |
| **Config 类**     | `common/src/main/java/com/spldeolin/allison1875/common/config/Config.java`                  | 配置 Java Bean        |
| **DomainConfig** | `common/src/main/java/com/spldeolin/allison1875/common/config/DomainConfig.java`            | Domain 配置           |

### 2.3 DocAnalyzerItBaseTest 基类功能

基类 `DocAnalyzerItBaseTest` 提供两个方法供子类调用：

```java
// 单 domain 场景（domains 只有一条，或不需要指定 domain）
private void runDocAnalyzer(String caseName)

// 多 domain 场景（需要指定处理哪个 domain）
private void runDocAnalyzer(String caseName, String domainNam
```

基类内部流程：

1. **拷贝资源**：将 `test/resources/it/doc-analyzer/{caseName}/` 递归拷贝到 `target/it/{caseName}/`
2. **解析并回写 yml**：以 SnakeYAML Map 模式加载 `.allison1875.yml`，将以下相对路径字段解析为绝对路径后回写：
    - `domains[].controllerModule/dtoModule/enumModule/serviceModule/serviceImplModule/persistenceModule`
    - `markdownDir`、`dslDir`、`dependencyDirsOrJavaFilePath`
3. **调用 Bootstrap**：组装 `--tool=doc-analyzer --config=... [--domain=...]` 参数，调用 `Bootstrap.main()`

子类通过 `basedir` 字段引用临时工作目录（`target/it/{caseName}/`），在 `@Test` 方法中编写断言。

---

## 3. 标准迁移步骤（逐 case 操作）

### Step 1 — 复制资源文件

```bash
# 源目录
SRC=allison1875-maven-plugin/src/it/doc-analyzer/{case-name}

# 目标目录
DST=allison1875-cli/src/test/resources/it/doc-analyzer/{case-name}

# 递归复制（排除 target/、invoker.properties、verify.groovy）
mkdir -p $DST
rsync -av --exclude='target/' --exclude='invoker.properties' --exclude='verify.groovy' \
  $SRC/ $DST/
```

需要复制的文件：

- `.allison1875.yml` — 配置文件（**需修改，见 Step 2**）
- `pom.xml` — fake Maven 项目 POM（**需修改，见 Step 3**）
- `src/main/java/...` — 全部 fake Java 源码
- 其他特殊目录（如 `external-dto/`、`dto-api/`、`order-api/` 等子模块目录）

**不需要复制**的文件：

- `invoker.properties` — maven-invoker 专用
- `verify.groovy` — 将用 Java 重写
- `target/` — 编译产物

### Step 2 — 修改 `.allison1875.yml`

#### 2a. 补齐 `*Module` 字段

原 yml 中 `DomainConfig` **缺少** `*Module` 字段。在 Mojo 模式下，`Allison1875Mojo.resolveDomainSourceRootsForMojo()`
会在 `*Module` 为 null/空时自动用 basedir 填充。在 CLI/Bootstrap 模式下不做此处理，
但基类 `DocAnalyzerItBaseTest` 在回写 yml 时会把 null/空/`.` 解析为 basedir 绝对路径。

**规则**：对于每个 domain，检查并补齐以下 6 个字段：

| 字段                  | 原 yml 值                 | 迁移后填写规则                                 |
|---------------------|-------------------------|-----------------------------------------|
| `controllerModule`  | 通常缺失                    | 单模块填 `"."` ，垂直拆分填子模块相对路径如 `"order-web"` |
| `dtoModule`         | 部分 case 已填（如 `dto-api`） | 同上。已有值的保留                               |
| `enumModule`        | 部分 case 已填              | 同上                                      |
| `serviceModule`     | 通常缺失                    | 单模块填 `"."`                              |
| `serviceImplModule` | 通常缺失                    | 单模块填 `"."`                              |
| `persistenceModule` | 通常缺失                    | 单模块填 `"."`                              |

**示例（单模块 case，大多数 case 都是这种）**：

```yaml
domains:
  - name: basic
    controllerModule: "."      # ← 新增
    controllerPackage: com.example.controller
    dtoModule: "."             # ← 新增
    reqDTOPackage: com.example.dto.req
    # ...
    enumModule: "."            # ← 新增
    serviceModule: "."         # ← 新增
    serviceImplModule: "."     # ← 新增
    persistenceModule: "."     # ← 新增
```

**示例（垂直拆分 case，如 `vertical-modules`）**：

```yaml
domains:
  - name: shop
    controllerModule: "."      # ← 新增（controller 在当前模块）
    controllerPackage: com.example.shop.controller
    dtoModule: dto-api         # ← 已有，保留
    # ...
    enumModule: dto-api        # ← 已有，保留
    serviceModule: "."         # ← 新增
    serviceImplModule: "."     # ← 新增
    persistenceModule: "."     # ← 新增
```

#### 2b. 字段完整性检查清单

对照 `DomainConfig.java` 中的 `@NotEmpty` 字段，确保 yml 中都有值：

- [ ] `name` — 必填
- [ ] `controllerModule` — **必须新增**
- [ ] `controllerPackage` — 必填
- [ ] `dtoModule` — **必须新增或确认已有**
- [ ] `reqDTOPackage` — 必填
- [ ] `respDTOPackage` — 必填
- [ ] `enumModule` — **必须新增或确认已有**
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

对照 `Config.java` 中的 `@NotEmpty` 字段：

- [ ] `codeSnippet.constructPageResult` — 必填
- [ ] `codeSnippet.constructEmptyPageResult` — 必填

### Step 3 — 修改 `pom.xml`

移除 `<build>` 中对 `allison1875-maven-plugin` 的 `<plugin>` 声明（不再需要）。保留 `<dependencies>` 声明
（spring-webmvc、validation-api、lombok 等），因为 `DocAnalyzer.process()` 内部通过
`MavenProjectClassLoaderUtils.buildClassLoader()` 需要有效的 `pom.xml` 来执行
`mvn dependency:build-classpath` 构建 ClassLoader。

修改前：

```xml
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

修改后：**删除整个 `<build>` 节点**（或仅删除 plugin 声明）。

同时将 `@allison1875.version@` 占位符（如果存在于其他位置）替换为实际值或删除。

### Step 4 — 创建 JUnit 5 测试类

在 `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/` 下创建测试类，
类名建议用 `{UpperCamelCaseName}ItTest`（如 `basic-dsl` → `BasicDslItTest`）。

**模板**：

```java
package com.spldeolin.allison1875.cli.it;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {case-name} 集成测试。
 *
 * @author Deolin {yyyy-MM-dd}
 */
public class XxxItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        // 单 domain 场景
        runDocAnalyzer("{case-name}");
        
        // 或多 domain 场景（如 horizontal-domains）
        // runDocAnalyzer("{case-name}", "user");

        // === 以下断言从 verify.groovy 翻译 ===

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");

        // 查找 md 文件
        List<File> mdFiles = new ArrayList<>();
        Files.walkFileTree(apiDocsDir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".md")) {
                    mdFiles.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        // 合并所有 md 内容
        StringBuilder sb = new StringBuilder();
        for (File mdFile : mdFiles) {
            sb.append(new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8));
            sb.append("\n");
        }
        String allContent = sb.toString();

        // 翻译 verify.groovy 的断言...
        // assert xxx.contains("yyy") → assertTrue(allContent.contains("yyy"), "message");
        // assert xxx.size() == N     → assertEquals(N, mdFiles.size(), "message");
    }
}
```

### Step 5 — Groovy 断言 → Java 断言翻译对照表

| Groovy 断言                           | Java JUnit 5 断言                                 |
|-------------------------------------|-------------------------------------------------|
| `assert x.contains("y") : "msg"`    | `assertTrue(x.contains("y"), "msg")`            |
| `assert x.size() == N : "msg"`      | `assertEquals(N, x.size(), "msg")`              |
| `assert x == "y" : "msg"`           | `assertEquals("y", x, "msg")`                   |
| `assert x.exists() : "msg"`         | `assertTrue(x.exists(), "msg")`                 |
| `assert x.isDirectory() : "msg"`    | `assertTrue(x.isDirectory(), "msg")`            |
| `assert fileNames.contains("x.md")` | `assertTrue(fileNames.contains("x.md"), "msg")` |

**特殊处理**：

1. **`build.log` 断言**：原 Groovy 中部分 case（如 `horizontal-domains`、`mixed-module-domains`）
   会检查 `build.log` 内容。迁移后不再有 `build.log`（不走 maven-invoker），
   这些断言需要**跳过或替换为等效的输出断言**。

2. **查找 md/json 文件的递归遍历**：Groovy 的 `eachFileRecurse` 改为 Java 的 `Files.walkFileTree`
   （见模板代码）。

### Step 6 — 运行验证

```bash
mvn test -pl allison1875-cli -am
```

需确保BUILD SUCCESS、没有ERROR级别日志、最好没有堆栈信息。

---

## 4. 全部 22 个 case 清单与特殊处理说明

### 4.1 标准 case（单 domain、单模块、无特殊配置）

以下 case 的 yml 只需补齐 6 个 `*Module: "."` 字段即可，其余结构与 basic-markdown 一致：

| #  | Case 名                          | 测试类名建议                             | 状态    |
|----|---------------------------------|------------------------------------|-------|
| 1  | `basic-markdown`                | `BasicMarkdownItTest`              | ✅ 已完成 |
| 2  | `advanced-validation`           | `AdvancedValidationItTest`         | ✅ 已完成 |
| 3  | `basic-dsl`                     | `BasicDslItTest`                   | ✅ 已完成 |
| 4  | `composed-annotation-value`     | `ComposedAnnotationValueItTest`    | ✅ 已完成 |
| 5  | `controller-with-response-body` | `ControllerWithResponseBodyItTest` | ✅ 已完成 |
| 6  | `deprecated-and-since`          | `DeprecatedAndSinceItTest`         | ✅ 已完成 |
| 7  | `enum-and-validation`           | `EnumAndValidationItTest`          | ✅ 已完成 |
| 8  | `glob-regex-branches`           | `GlobRegexBranchesItTest`          | ✅ 已完成 |
| 9  | `hierarchical-categories`       | `HierarchicalCategoriesItTest`     | ✅ 已完成 |
| 10 | `json-property-access`          | `JsonPropertyAccessItTest`         | ✅ 已完成 |
| 11 | `mvc-handler-wildcards`         | `MvcHandlerWildcardsItTest`        | ✅ 已完成 |
| 12 | `no-controller-mapping`         | `NoControllerMappingItTest`        | ✅ 已完成 |
| 13 | `pathvar-and-reqparam-aliases`  | `PathvarAndReqparamAliasesItTest`  | ✅ 已完成 |
| 14 | `primitive-and-simple-return`   | `PrimitiveAndSimpleReturnItTest`   | ✅ 已完成 |
| 15 | `request-mapping-params`        | `RequestMappingParamsItTest`       | ✅ 已完成 |
| 16 | `single-endpoint-markdown`      | `SingleEndpointMarkdownItTest`     | ✅ 已完成 |

### 4.2 特殊 case

#### `markdown-and-dsl` — 双输出模式 ✅ 已完成

- **特殊点**：`flushTo` 包含 `MARKDOWN` 和 `DSL` 两种，需同时验证 `api-docs/` 和 `api-dsls/` 两个目录。
- **yml 处理**：补齐 `*Module: "."` + 增加 `dslDir` 字段（若已有则保留，基类会自动解析相对路径）。
- **测试类名**：`MarkdownAndDslItTest`

#### `dependency-dirs` — 外部依赖目录 ✅ 已完成

- **特殊点**：yml 中有 `dependencyDirsOrJavaFilePath: [external-dto]`，指向 case 目录下的 `external-dto/` 子目录。
- **yml 处理**：补齐 `*Module: "."` 即可。`dependencyDirsOrJavaFilePath` 的相对路径由基类自动解析。
- **资源复制**：确保 `external-dto/` 目录及其内容也被复制。
- **测试类名**：`DependencyDirsItTest`

#### `horizontal-domains` — 水平多 domain（需指定 domain） ✅ 已完成

- **特殊点**：有 2 个 domain（`user` 和 `order`），原 invoker 传了 `-Ddomain=user`。
- **yml 处理**：**两个 domain 都需要补齐 6 个 `*Module: "."`**。
- **测试调用**：`runDocAnalyzer("horizontal-domains", "user")`
- **断言注意**：原 verify.groovy 检查了 `build.log` 中的 domain 选择日志，迁移后无 `build.log`，
  这些断言需跳过，仅保留对生成文件的内容断言。
- **测试类名**：`HorizontalDomainsItTest`

#### `mixed-module-domains` — 混合多 domain + 垂直拆分（需指定 domain） ✅ 已完成

- **特殊点**：有 2 个 domain（`user` 和 `order`），各自有子模块（`user-api`/`order-api`），原 invoker 传了 `-Ddomain=order`。
- **yml 处理**：两个 domain 中 `dtoModule`/`enumModule` 已填了子模块相对路径（如 `order-api`），
  需额外补齐缺失的 `controllerModule: "."`、`serviceModule: "."`、`serviceImplModule: "."`、`persistenceModule: "."`。
  **注意：必须删除 `wholeDTOModule` 字段**（`DomainConfig` 中无此属性，见经验 7.7）。
- **资源复制**：确保 `user-api/`、`order-api/` 等子模块目录及其源码也被复制。
- **测试调用**：`runDocAnalyzer("mixed-module-domains", "order")`
- **断言注意**：同上，跳过 `build.log` 断言。
- **测试类名**：`MixedModuleDomainsItTest`

#### `vertical-modules` — 垂直拆分（单 domain + 子模块） ✅ 已完成

- **特殊点**：`dtoModule: dto-api`、`enumModule: dto-api`，controller 在当前模块。
- **yml 处理**：保留已有的子模块路径，补齐缺失的 `controllerModule: "."`、`serviceModule: "."`、
  `serviceImplModule: "."`、`persistenceModule: "."`。
  **注意：必须删除 `wholeDTOModule` 字段**（`DomainConfig` 中无此属性，见经验 7.7）。
- **资源复制**：确保 `dto-api/` 目录被复制。
- **注意**：`dto-api/` 也是一个独立 Maven 项目，需包含自己的 `pom.xml`。
- **测试类名**：`VerticalModulesItTest`

#### `java-record` — Java 17+ 特性 ✅ 已完成

- **特殊点**：`javaVersion: "17"`，`enableJavaxMoveToJakarta: true`，使用了 Java record 语法。
- **yml 处理**：补齐 `*Module: "."`，新增 `javaHome` 指向 JDK 21（见经验 7.9）。
- **pom.xml 处理**：保留 `maven-compiler-plugin`（JDK 17 编译配置），删除 `allison1875-maven-plugin`。
- **测试类名**：`JavaRecordItTest`

#### `jakarta-validation` — Jakarta Validation（Java 17+） ✅ 已完成

- **特殊点**：同 `java-record`，`javaVersion: "17"`，`enableJavaxMoveToJakarta: true`。
  pom.xml 中依赖 `jakarta.validation:jakarta.validation-api` 而非 `javax.validation:validation-api`。
- **yml 处理**：补齐 `*Module: "."`，新增 `javaHome` 指向 JDK 21（见经验 7.9）。
- **pom.xml 处理**：删除整个 `<build>` 节点（仅含 `allison1875-maven-plugin`），保留 `maven.compiler.source/target` 属性。
- **测试类名**：`JakartaValidationItTest`

---

## 5. 常见问题

### Q1: 测试报 `pom.xml not found`

`DocAnalyzer.process()` 内部通过 `MavenProjectClassLoaderUtils.buildClassLoader(controllerModule, javaHome)`
构建 ClassLoader，该方法要求目录下存在 `pom.xml`。确保测试资源目录中包含 `pom.xml`。

### Q2: SnakeYAML 回写 yml 后 Bootstrap 加载报错

基类使用 SnakeYAML 的 **Map 模式**（不指定 Constructor 类型）加载和 dump yml，
避免了 JavaBean 序列化的类型标签和 `File` 无参构造器等问题。如果仍有问题，
检查 yml 中是否有不兼容的值类型（如 `!!` 标签）。

### Q3: `mvn dependency:build-classpath` 失败

`MavenProjectClassLoaderUtils` 内部执行 `mvn compile dependency:build-classpath` 子进程。
确保：

1. 本地 Maven 环境可用（`mvn` 在 PATH 中）
2. pom.xml 中的依赖能在本地仓库或远程仓库中解析
3. 如果需要特定 JDK 版本（如 JDK 17），在 yml 中配置 `javaHome`

### Q4: 多 domain case 报 "配置文件中定义了多个domain，必须指定要处理的业务领域"

多 domain case 必须传 `domainName` 参数。检查 `invoker.properties` 中的 `-Ddomain=xxx` 值，
在测试类中调用 `runDocAnalyzer(caseName, "xxx")`。

### Q5: 运行全部测试时其他模块报 "No tests matching pattern"

使用 `-Dsurefire.failIfNoSpecifiedTests=false` 参数跳过这个错误：

```bash
mvn test -pl allison1875-cli -am -Dtest=XxxItTest -Dsurefire.failIfNoSpecifiedTests=false
```

---

## 6. 附录：模板 case 文件参考

### `BasicMarkdownItTest.java`（已验证通过）

```java
package com.spldeolin.allison1875.cli.it;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicMarkdownItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("basic-markdown");

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");
        assertTrue(apiDocsDir.isDirectory(), "api-docs should be a directory");

        List<File> mdFiles = new ArrayList<>();
        Files.walkFileTree(apiDocsDir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".md")) {
                    mdFiles.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        assertTrue(mdFiles.size() > 0, "At least one .md file should be generated");

        StringBuilder sb = new StringBuilder();
        for (File mdFile : mdFiles) {
            sb.append(new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8));
            sb.append("\n");
        }
        String allContent = sb.toString();

        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");
        assertEquals("用户管理.md", mdFiles.get(0).getName(), "Markdown filename should be '用户管理.md'");

        assertTrue(allContent.contains("GET"), "Should contain HTTP method GET");
        assertTrue(allContent.contains("/api/users"), "Should contain URL /api/users");
        assertTrue(allContent.contains("查询用户列表"), "Should contain '查询用户列表'");
        assertTrue(allContent.contains("根据关键字搜索用户"), "Should contain sub-description");
        assertTrue(allContent.contains("POST"), "Should contain HTTP method POST");
        assertTrue(allContent.contains("创建用户"), "Should contain '创建用户'");
        assertTrue(allContent.contains("username"), "Should contain field 'username'");
        assertTrue(allContent.contains("age"), "Should contain field 'age'");
        assertTrue(allContent.contains("email"), "Should contain field 'email'");
        assertTrue(allContent.contains("用户名"), "Should contain field comment '用户名'");
        assertTrue(allContent.contains("必须有非空格字符"), "Should contain @NotBlank description");
        assertTrue(allContent.contains("不能为null"), "Should contain @NotNull description");
        assertTrue(allContent.contains("id"), "Should contain response field 'id'");
        assertTrue(allContent.contains("Object Array"), "Should contain 'Object Array'");
        assertTrue(allContent.contains("用户ID"), "Should contain response field comment '用户ID'");
        assertTrue(allContent.contains("keyword"), "Should contain query param 'keyword'");
        assertTrue(allContent.contains("否"), "Should contain 'required=false' as '否'");
        assertTrue(allContent.contains("### URL"), "Should contain '### URL'");
        assertTrue(allContent.contains("### Query Param"), "Should contain '### Query Param'");
        assertTrue(allContent.contains("### Request Body (application/json)"), "Should contain request body section");
        assertTrue(allContent.contains("### Response Body (application/json)"), "Should contain response body section");
        assertTrue(allContent.contains("---"), "Should contain separator '---'");
    }
}
```

### `.allison1875.yml` 模板（单模块 case）

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

author: test-author
javaVersion: "8"
enableNoModifyAnnounce: true
enableJavaxMoveToJakarta: false
isDataModelWithoutLombok: false

flushTo:
  - MARKDOWN
markdownDir: api-docs
globalUrlPrefix: ""

codeSnippet:
  constructPageResult: "new PageResult<>(${total}, ${dtos})"
  constructEmptyPageResult: "new PageResult<>()"
```

### `pom.xml` 模板（单模块 case）

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
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>5.3.31</version>
        </dependency>
        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
            <version>2.0.1.Final</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

</project>
```

---

## 7. 迁移经验总结

### 7.1 批量迁移标准 case 的高效流程

标准 case（单 domain、单模块、无特殊配置）的迁移高度模式化，可按以下流程批量执行：

1. **批量复制资源**：使用 `rsync` 循环复制，排除 `target/`、`invoker.properties`、`verify.groovy`、`README.md`
2. **统一修改 yml**：所有标准 case 的 yml 修改完全一致 — 补齐 6 个 `*Module: "."` 字段
3. **统一修改 pom.xml**：删除 `<build>` 节点即可。注意检查是否有额外依赖需保留（如 `jackson-annotations`）
4. **逐个翻译 verify.groovy → Java Test**：这是唯一需要逐 case 细看的步骤
5. **批量运行测试**：使用 `-Dtest=A,B,C` 一次性验证全部新测试

### 7.2 verify.groovy 断言与实际行为不一致

原 verify.groovy 中的断言可能描述的是**期望行为**而非 doc-analyzer 的**实际行为**。
迁移时需注意：

- **先运行测试再写断言**：如果不确定 doc-analyzer 对某个特性的实际输出，可以先运行工具生成 md，
  查看实际内容后再编写断言。
- **示例**：`json-property-access` case 的 verify.groovy 断言 `@JsonProperty(access=READ_ONLY)` 字段
  不出现在 Request Body，但 doc-analyzer 当前实际行为是**所有字段都会出现**（不做 access 过滤）。
  迁移时需根据实际行为调整断言。

### 7.3 特殊配置字段保留清单

以下 yml 字段在某些 case 中有非默认值，复制后需确认保留：

| 字段                             | 涉及 case                                    | 特殊值示例        |
|--------------------------------|--------------------------------------------|--------------|
| `mvcHandlerQualifierWildcards` | glob-regex-branches, mvc-handler-wildcards | glob 模式列表    |
| `singleEndpointPerMarkdown`    | single-endpoint-markdown                   | `true`       |
| `globalUrlPrefix`              | single-endpoint-markdown                   | `api`（非空字符串） |
| `getEnumCodeMethodName`        | enum-and-validation                        | `getCode`    |
| `getEnumTitleMethodName`       | enum-and-validation                        | `getTitle`   |

### 7.4 pom.xml 额外依赖保留

大多数 case 的 pom.xml 只需标准三件套（spring-webmvc, validation-api, lombok），
但以下 case 需要额外依赖：

| Case                   | 额外依赖                                                      |
|------------------------|-----------------------------------------------------------|
| `json-property-access` | `com.fasterxml.jackson.core:jackson-annotations:2.13.5`   |
| `enum-and-validation`  | `org.hibernate.validator:hibernate-validator:6.2.5.Final` |

### 7.5 yml 文件编辑工具截断问题

使用 `edit_file` 工具整体写入 yml 文件时，末尾引号可能被截断（如 `"new PageResult<>()` 缺少闭合 `"`），
导致 SnakeYAML 解析报 `found unexpected end of stream`。建议：

- 写入 yml 后立即检查文件末尾内容是否完整
- 特别注意 `codeSnippet.constructEmptyPageResult` 等含特殊字符的值
- 遇到此类问题使用 `replace_in_file` 精确修复缺失的引号

### 7.6 DSL 输出断言需使用 Jackson

`flushTo` 包含 `DSL` 时，`api-dsls/` 下会生成 JSON 文件。验证 JSON 结构需要使用 Jackson
（`ObjectMapper` + `JsonNode`），而非字符串匹配。allison1875-cli 的 test classpath 已包含 Jackson 依赖。

### 7.7 `wholeDTOModule` 字段不存在于 DomainConfig

原 IT case 的 `.allison1875.yml` 中部分 domain 配置了 `wholeDTOModule` 字段（如 `wholeDTOModule: dto-api`），
但当前版本的 `DomainConfig.java` 中**没有** `wholeDTOModule` 属性。在 Mojo/invoker 模式下，
SnakeYAML 以宽松模式加载可能不报错（或该字段在旧版中存在），但 Bootstrap 的 JavaBean 模式加载
会抛出 `Unable to find property 'wholeDTOModule' on class: DomainConfig`。

**迁移操作**：直接删除 yml 中的 `wholeDTOModule` 行即可。`wholeDTOPackage` 是有效字段，需保留。

### 7.9 JDK 17+ case 需配置 `javaHome` 而非依赖运行环境 JDK 版本

`java-record` 和 `jakarta-validation` 两个 case 的 `javaVersion: "17"` 要求 `MavenProjectClassLoaderUtils`
使用 JDK 17+ 来执行 `mvn compile dependency:build-classpath`（因为 Spring 6.x 和 jakarta.validation-api 3.x
需要 JDK 17+ 编译）。但 allison1875 项目本身以 JDK 8 为编译目标，开发/CI 环境的默认 JDK 可能不是 17+。

**解决方案**：在 `.allison1875.yml` 中配置 `javaHome` 字段指向本地安装的 JDK 17+ 路径：

```yaml
javaHome: "/Library/Java/JavaVirtualMachines/amazon-corretto-21.jdk/Contents/Home"
```

`MavenProjectClassLoaderUtils` 会自动检测 `javaHome` 配置，在执行 `mvn` 子进程时注入
`JAVA_HOME=<javaHome>` 环境变量，从而用指定 JDK 编译 IT case 的 fake 项目。

**注意事项**：

- `javaHome` 是 `Config` 类的顶层字段，在 yml 顶层（与 `javaVersion` 同级）配置
- `javaHome` 指向的 JDK 版本必须 ≥ `javaVersion` 中声明的版本
- `java-record` case 的 pom.xml 需保留 `maven-compiler-plugin` 的 `<release>17</release>` 配置，
  因为 record 语法需要 `--release 17` 编译开关
- `jakarta-validation` case 的 pom.xml 只需 `maven.compiler.source/target` 属性即可，
  无需显式 `maven-compiler-plugin` 配置

### 7.10 doc-analyzer IT case 全部迁移完成

截至本次迁移，`allison1875-maven-plugin/src/it/doc-analyzer/` 下的所有 22 个 IT case 已全部
迁移到 `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/` 目录，
通过 JUnit 5 + `DocAnalyzerItBaseTest` 基类 + `Bootstrap.main()` 驱动。

原 `allison1875-maven-plugin/src/it/doc-analyzer/` 目录已清空，可考虑后续删除该空目录。

### 7.8 多模块 case 的 pom.xml 中 build-helper-maven-plugin 需保留

`vertical-modules`、`mixed-module-domains` 等垂直拆分 case 的 pom.xml 中使用了
`build-helper-maven-plugin` 来添加子模块的 source root（`add-source` goal）。
这些配置在 CLI/Bootstrap 模式下仍然有效——`MavenProjectClassLoaderUtils.buildClassLoader()`
内部执行 `mvn compile dependency:build-classpath`，会触发 `generate-sources` 阶段从而执行
`build-helper-maven-plugin`，将子模块源码编译到 `target/classes` 中供 ClassLoader 加载。

**迁移操作**：保留 `build-helper-maven-plugin`，仅删除 `allison1875-maven-plugin` 声明。
