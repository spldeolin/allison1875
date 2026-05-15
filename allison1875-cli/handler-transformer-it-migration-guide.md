# handler-transformer IT Case 迁移指南

> 将 `allison1875-maven-plugin/src/it/handler-transformer/` 下的集成测试迁移到 `allison1875-cli` 模块，
> 以 `Bootstrap.main()` 作为入口，使用 JUnit 5 驱动并编写 Java 断言。

---

## 1. 背景与目标

### 为什么迁移？

项目计划逐步废弃 `allison1875-maven-plugin` 模块。原 IT case 通过 `maven-invoker-plugin` 驱动，
调用 Mojo 入口执行。迁移后改为在 `allison1875-cli` 模块中，通过 `Bootstrap.main()` (CLI 入口)

+ JUnit 5 驱动集成测试。

### 已完成的模板

`basic-post` 已作为模板 case 迁移并验证通过。剩余 17 个 case 需按本指南迁移。

---

## 2. 架构与关键文件说明

### 2.1 核心调用链

```
JUnit 5 Test (@Test)
  → HandlerTransformerItBaseTest#runHandlerTransformer(caseName)
    → 拷贝 test/resources/it/handler-transformer/{caseName}/ 到 target/it/{caseName}/
    → 加载 .allison1875.yml (Map 模式)
    → 解析所有 *Module 相对路径为绝对路径并回写 yml
    → Bootstrap.main(["--tool=handler-transformer", "--config=..."])
      → Bootstrap.parseArgs() → Bootstrap.loadConfig() → Allison1875.letsGo()
        → resolveSourceRoots → Guice injector → HandlerTransformer.process()
```

### 2.2 关键文件位置

| 文件            | 路径                                                                                                 | 说明                  |
|---------------|----------------------------------------------------------------------------------------------------|---------------------|
| **基类**        | `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/HandlerTransformerItBaseTest.java` | 封装通用流程，所有 test 继承此类 |
| **模板测试**      | `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/BasicPostItTest.java`              | 参照此文件编写新 test       |
| **测试资源**      | `allison1875-cli/src/test/resources/it/handler-transformer/{caseName}/`                            | 每个 case 的资源目录       |
| **原 IT 源**    | `allison1875-maven-plugin/src/it/handler-transformer/{caseName}/`                                  | 原始 IT case 目录       |
| **Bootstrap** | `allison1875-cli/src/main/java/com/spldeolin/allison1875/cli/Bootstrap.java`                       | CLI 入口              |

### 2.3 HandlerTransformerItBaseTest 基类功能

基类 `HandlerTransformerItBaseTest` 提供两个方法供子类调用：

```java
// 单 domain 场景（domains 只有一条，或不需要指定 domain）
private void runHandlerTransformer(String caseName)

// 多 domain 场景（需要指定处理哪个 domain）
protected void runHandlerTransformer(String caseName, String domainNam
```

基类内部流程：

1. **拷贝资源**：将 `test/resources/it/handler-transformer/{caseName}/` 递归拷贝到 `target/it/{caseName}/`
2. **解析并回写 yml**：以 SnakeYAML Map 模式加载 `.allison1875.yml`，将 `*Module` 相对路径字段解析为绝对路径后回写
3. **调用 Bootstrap**：组装 `--tool=handler-transformer --config=... [--domain=...]` 参数，调用 `Bootstrap.main()`

子类通过 `basedir` 字段引用临时工作目录（`target/it/{caseName}/`），在 `@Test` 方法中编写断言。

### 2.4 与 doc-analyzer 基类的区别

| 差异点             | DocAnalyzerItBaseTest                                 | HandlerTransformerItBaseTest                  |
|-----------------|-------------------------------------------------------|-----------------------------------------------|
| **资源前缀**        | `it/doc-analyzer/{caseName}/`                         | `it/handler-transformer/{caseName}/`          |
| **CLI tool 参数** | `--tool=doc-analyzer`                                 | `--tool=handler-transformer`                  |
| **额外路径字段解析**    | `markdownDir`、`dslDir`、`dependencyDirsOrJavaFilePath` | 无（handler-transformer 直接写回 source tree）       |
| **输出验证对象**      | 生成的 markdown/JSON 文件                                  | 改写后的 Controller + 生成的 DTO/Service/ServiceImpl |

---

## 3. 标准迁移步骤（逐 case 操作）

### Step 1 — 复制资源文件

```bash
# 源目录
SRC=allison1875-maven-plugin/src/it/handler-transformer/{case-name}

# 目标目录
DST=allison1875-cli/src/test/resources/it/handler-transformer/{case-name}

# 递归复制（排除 target/、invoker.properties、verify.groovy）
mkdir -p $DST
rsync -av --exclude='target/' --exclude='invoker.properties' --exclude='verify.groovy' \
  $SRC/ $DST/
```

需要复制的文件：

- `.allison1875.yml` — 配置文件（**需修改，见 Step 2**）
- `pom.xml` — fake Maven 项目 POM（**需修改，见 Step 3**）
- `src/main/java/...` — 全部 fake Java 源码

**不需要复制**的文件：

- `invoker.properties` — maven-invoker 专用
- `verify.groovy` — 将用 Java 重写
- `target/` — 编译产物

### Step 2 — 修改 `.allison1875.yml`

#### 2a. 补齐 `*Module` 字段

原 yml 中 `DomainConfig` **缺少** `*Module` 字段。在 Mojo 模式下，`Allison1875Mojo.resolveDomainSourceRootsForMojo()`
会在 `*Module` 为 null/空时自动用 basedir 填充。在 CLI/Bootstrap 模式下不做此处理，
但基类 `HandlerTransformerItBaseTest` 在回写 yml 时会把 null/空/`.` 解析为 basedir 绝对路径。

**规则**：对于每个 domain，检查并补齐以下 6 个字段：

| 字段                  | 原 yml 值 | 迁移后填写规则    |
|---------------------|---------|------------|
| `controllerModule`  | 通常缺失    | 单模块填 `"."` |
| `dtoModule`         | 通常缺失    | 单模块填 `"."` |
| `enumModule`        | 通常缺失    | 单模块填 `"."` |
| `serviceModule`     | 通常缺失    | 单模块填 `"."` |
| `serviceImplModule` | 通常缺失    | 单模块填 `"."` |
| `persistenceModule` | 通常缺失    | 单模块填 `"."` |

**示例（所有 handler-transformer case 均为单模块）**：

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

对照 `Config.java` 中的 `@NotEmpty` 字段：

- [ ] `codeSnippet.constructPageResult` — 必填
- [ ] `codeSnippet.constructEmptyPageResult` — 必填

### Step 3 — 修改 `pom.xml`

1. **删除整个 `<build>` 节点**（包含 `allison1875-maven-plugin` 声明）
2. **替换 `@allison1875.version@` 占位符**：将 `allison1875-support` 依赖的版本从 `@allison1875.version@` 替换为
   `13.0-SNAPSHOT`
3. **保留 `<dependencies>`**：spring-webmvc、validation-api、lombok、allison1875-support 等，
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

在 `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/` 下创建测试类，
类名建议用 `{UpperCamelCaseName}ItTest`（如 `basic-post` → `BasicPostItTest`）。

**模板**：

```java
package com.spldeolin.allison1875.cli.it;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {case-name} 集成测试。
 *
 * @author Deolin {yyyy-MM-dd}
 */
public class XxxItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("{case-name}");

        // === 以下断言从 verify.groovy 翻译 ===

        // 1. 验证 Controller 被改写
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/XxxController.java");
        assertTrue(controllerFile.exists(), "XxxController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块应该被移除
        assertFalse(controllerContent.contains("..."), "...");

        // 应生成 handler 方法
        assertTrue(controllerContent.contains("..."), "...");

        // 2. 验证生成的 DTO / Service / ServiceImpl
        // ...
    }

}
```

### Step 5 — Groovy 断言 → Java 断言翻译对照表

| Groovy 断言                                     | Java JUnit 5 断言                                |
|-----------------------------------------------|------------------------------------------------|
| `assert x.contains("y") : "msg"`              | `assertTrue(x.contains("y"), "msg")`           |
| `assert !x.contains("y") : "msg"`             | `assertFalse(x.contains("y"), "msg")`          |
| `assert x.size() == N : "msg"`                | `assertEquals(N, x.size(), "msg")`             |
| `assert x == "y" : "msg"`                     | `assertEquals("y", x, "msg")`                  |
| `assert x.exists() : "msg"`                   | `assertTrue(x.exists(), "msg")`                |
| `def files = dir.listFiles().findAll { ... }` | `File[] files = dir.listFiles((d, n) -> ...);` |

**handler-transformer 特有的断言模式**：

1. **Controller 改写验证**：检查 init 块被移除、handler 方法被生成、Service 被注入
2. **DTO 生成验证**：检查 req/resp DTO 目录和文件存在、字段和注解保留
3. **Service 接口生成验证**：检查 interface 关键字、方法名
4. **ServiceImpl 生成验证**：检查 class 关键字、implements、方法名
5. **文件查找**：使用 `File.listFiles(FilenameFilter)` 匹配含特定关键词的 Java 文件

### Step 6 — 运行验证

```bash
mvn clean test
```

需确保 BUILD SUCCESS、没有 ERROR 级别日志、最好没有堆栈信息。

### Step 7 — 删除原 IT case 目录

验证通过后，删除 `allison1875-maven-plugin/src/it/handler-transformer/{case-name}/` 目录。

---

## 4. 全部 18 个 case 清单与特殊处理说明

### 4.1 标准 case（单 domain、单模块、无特殊配置）

以下 case 的 yml 只需补齐 6 个 `*Module: "."` 字段即可，其余结构与 basic-post 一致：

| #  | Case 名               | 测试类名建议                   | 特殊配置 | 状态    |
|----|----------------------|--------------------------|------|-------|
| 1  | `basic-post`         | `BasicPostItTest`        | 无    | ✅ 已完成 |
| 2  | `basic-get`          | `BasicGetItTest`         | 无    | ✅ 已完成 |
| 3  | `resp-only`          | `RespOnlyItTest`         | 无    | 待迁移   |
| 4  | `req-only`           | `ReqOnlyItTest`          | 无    | 待迁移   |
| 5  | `no-req-no-resp`     | `NoReqNoRespItTest`      | 无    | 待迁移   |
| 6  | `no-desc`            | `NoDescItTest`           | 无    | 待迁移   |
| 7  | `multiple-init-decs` | `MultipleInitDecsItTest` | 无    | 待迁移   |
| 8  | `nested-dto`         | `NestedDtoItTest`        | 无    | 待迁移   |
| 9  | `nest-dto-list`      | `NestDtoListItTest`      | 无    | 待迁移   |
| 10 | `multi-controller`   | `MultiControllerItTest`  | 无    | 待迁移   |
| 11 | `no-handler-skip`    | `NoHandlerSkipItTest`    | 无    | 待迁移   |
| 12 | `datetime-fields`    | `DatetimeFieldsItTest`   | 无    | 待迁移   |

### 4.2 特殊配置 case

| #  | Case 名                  | 测试类名建议                       | 特殊配置说明                                                   | 状态  |
|----|-------------------------|------------------------------|----------------------------------------------------------|-----|
| 13 | `enable-one-service`    | `EnableOneServiceItTest`     | yml 中 `enableOneService: true`                           | 待迁移 |
| 14 | `handler-alias`         | `HandlerAliasItTest`         | Controller init 块使用别名 URL                                | 待迁移 |
| 15 | `controller-annotation` | `ControllerAnnotationItTest` | Controller 使用 `@Controller` 而非 `@RestController`         | 待迁移 |
| 16 | `page-annotation`       | `PageAnnotationItTest`       | init 块中含分页相关注解                                           | 待迁移 |
| 17 | `list-annotation`       | `ListAnnotationItTest`       | init 块中含 `@L` 注解（列表返回）                                   | 待迁移 |
| 18 | `no-lombok`             | `NoLombokItTest`             | yml 中 `isDataModelWithoutLombok: true`，生成 DTO 不使用 Lombok | 待迁移 |

### 4.3 特殊 case 说明

#### `enable-one-service` — 合并 Service 模式

- **特殊点**：`enableOneService: true`，所有 handler 的 Service 方法合并到一个 Service 类而非每个 handler 一个。
- **yml 处理**：补齐 `*Module: "."` 即可，保留 `enableOneService: true`。
- **断言注意**：Service/ServiceImpl 文件数量和命名可能与标准模式不同。

#### `no-lombok` — 无 Lombok DTO

- **特殊点**：`isDataModelWithoutLombok: true`，生成的 DTO 不使用 `@Data` 等 Lombok 注解，而是手写 getter/setter。
- **yml 处理**：补齐 `*Module: "."`，保留 `isDataModelWithoutLombok: true`。
- **断言注意**：验证 DTO 中包含 getter/setter 方法而非 Lombok 注解。
- **pom.xml 注意**：可能不需要 lombok 依赖（但保留也无影响）。

#### `page-annotation` — 分页注解

- **特殊点**：init 块中使用 `@P` 注解标记分页场景。
- **yml 处理**：补齐 `*Module: "."`。
- **断言注意**：验证生成的 handler 方法包含分页参数和返回类型。

#### `list-annotation` — 列表注解

- **特殊点**：init 块中使用 `@L` 注解标记列表返回场景。
- **yml 处理**：补齐 `*Module: "."`。
- **断言注意**：验证生成的 handler 方法返回 List 类型。

#### `controller-annotation` — @Controller 注解

- **特殊点**：使用 `@Controller` + `@ResponseBody` 而非 `@RestController`。
- **yml 处理**：补齐 `*Module: "."`。
- **断言注意**：验证 handler-transformer 仍能正确检测和转换。

---

## 5. 常见问题

### Q1: 测试报 `pom.xml not found`

`HandlerTransformer.process()` 内部通过 `MavenProjectClassLoaderUtils.buildClassLoader(controllerModule, javaHome)`
构建 ClassLoader，该方法要求目录下存在 `pom.xml`。确保测试资源目录中包含 `pom.xml`。

### Q2: `mvn dependency:build-classpath` 失败

`MavenProjectClassLoaderUtils` 内部执行 `mvn compile dependency:build-classpath` 子进程。
确保：

1. 本地 Maven 环境可用（`mvn` 在 PATH 中）
2. pom.xml 中的依赖能在本地仓库或远程仓库中解析
3. `allison1875-support` 已 install 到本地仓库（`mvn install -pl allison1875-support`）

### Q3: `@allison1875.version@` 占位符未替换

原 IT case 通过 `maven-invoker-plugin` 的 `filterProperties` 自动替换 `@allison1875.version@`。
迁移后不再有此机制，必须手动将所有 `@allison1875.version@` 替换为实际版本 `13.0-SNAPSHOT`。

### Q4: handler-transformer 改写的文件在临时目录而非源码目录

handler-transformer 的输出直接写回 source tree（即 `target/it/{caseName}/src/main/java/...`）。
与 doc-analyzer（输出到 `api-docs/` 等独立目录）不同，断言需要检查源码文件本身的变化。

### Q5: SnakeYAML 回写 yml 后 Bootstrap 加载报错

基类使用 SnakeYAML 的 **Map 模式**加载和 dump yml。如果仍有问题，
检查 yml 中是否有不兼容的值类型（如 `!!` 标签）。

---

## 6. 附录：模板 case 文件参考

### `BasicPostItTest.java`（已验证通过）

```java
package com.spldeolin.allison1875.cli.it;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BasicPostItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("basic-post");

        // 1. 验证 Controller 被改写
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/OrderController.java");
        assertTrue(controllerFile.exists(), "OrderController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(controllerContent.contains("String handler = \"/create-order\""),
                "init block variable 'handler' should be removed");
        assertFalse(controllerContent.contains("String desc = \"创建订单\""),
                "init block variable 'desc' should be removed");
        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed from controller");
        assertFalse(controllerContent.contains("class Resp"), "inner class Resp should be removed from controller");

        assertTrue(controllerContent.contains("PostMapping"), "Controller should contain @PostMapping annotation");
        assertTrue(controllerContent.contains("/create-order"),
                "Controller should contain handler URL '/create-order'");
        assertTrue(controllerContent.contains("createOrder"), "Controller should contain method named 'createOrder'");
        assertTrue(controllerContent.contains("Autowired") || controllerContent.contains("@Inject")
                || controllerContent.contains("@Resource"), "Controller should have injected service field");

        // 2. 验证 Req DTO
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");
        File[] reqFiles = reqDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateOrder") && name.contains("Req"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate exactly 1 Req DTO file for CreateOrder");
        String reqContent = new String(Files.readAllBytes(reqFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(reqContent.contains("orderName"), "Req DTO should contain field 'orderName'");
        assertTrue(reqContent.contains("amount"), "Req DTO should contain field 'amount'");
        assertTrue(reqContent.contains("NotBlank"), "Req DTO should preserve @NotBlank annotation");
        assertTrue(reqContent.contains("NotNull"), "Req DTO should preserve @NotNull annotation");

        // 3. 验证 Resp DTO
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");
        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateOrder") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1,
                "Should generate exactly 1 Resp DTO file for CreateOrder");
        String respContent = new String(Files.readAllBytes(respFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(respContent.contains("orderId"), "Resp DTO should contain field 'orderId'");
        assertTrue(respContent.contains("status"), "Resp DTO should contain field 'status'");

        // 4. 验证 Service 接口
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");
        File[] serviceFiles = serviceDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateOrder") && name.contains("Service"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1,
                "Should generate exactly 1 Service interface file");
        String serviceContent = new String(Files.readAllBytes(serviceFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(serviceContent.contains("interface"), "Service file should be an interface");
        assertTrue(serviceContent.contains("createOrder"), "Service should contain method 'createOrder'");

        // 5. 验证 ServiceImpl
        File serviceImplDir = new File(basedir, "src/main/java/com/example/service/impl");
        assertTrue(serviceImplDir.exists(), "service/impl directory should exist");
        File[] serviceImplFiles = serviceImplDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateOrder") && name.contains("Impl"));
        assertTrue(serviceImplFiles != null && serviceImplFiles.length == 1,
                "Should generate exactly 1 ServiceImpl file");
        String serviceImplContent = new String(Files.readAllBytes(serviceImplFiles[0].toPath()),
                StandardCharsets.UTF_8);
        assertTrue(serviceImplContent.contains("class"), "ServiceImpl file should contain a class");
        assertTrue(serviceImplContent.contains("createOrder"), "ServiceImpl should contain method 'createOrder'");
        assertTrue(serviceImplContent.contains("implements"), "ServiceImpl should implement the service interface");
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
enableOneService: false

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
        <dependency>
            <groupId>com.spldeolin.allison1875</groupId>
            <artifactId>allison1875-support</artifactId>
            <version>13.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

</project>
```

---

## 7. 迁移经验总结

### 7.1 handler-transformer 与 doc-analyzer 迁移的关键差异

| 差异维度                       | doc-analyzer                        | handler-transformer                  |
|----------------------------|-------------------------------------|--------------------------------------|
| **输出位置**                   | 独立目录（`api-docs/`、`api-dsls/`）       | 直接写回 source tree                     |
| **验证方式**                   | 检查生成的 md/JSON 文件                    | 检查改写后的 Controller + 新生成的 DTO/Service |
| **基类额外路径解析**               | markdownDir、dslDir、dependencyDirs   | 无额外路径                                |
| **allison1875-support 依赖** | 不需要                                 | 需要（init 块中可能用到 `@L`、`@P` 注解）         |
| **pom.xml 版本占位符**          | 无 `@allison1875.version@`（大多数 case） | 有 `@allison1875.version@`（需手动替换）     |

### 7.2 allison1875-support 依赖处理

handler-transformer 的 IT case pom.xml 中通常包含 `allison1875-support` 依赖（用于 `@L`、`@P` 等注解），
版本使用 `@allison1875.version@` 占位符。迁移时必须：

1. 将 `@allison1875.version@` 替换为 `13.0-SNAPSHOT`
2. 确保运行测试前 `allison1875-support` 已 install 到本地仓库

### 7.3 handler-transformer 输出是源码变更而非新文件目录

doc-analyzer 的断言主要围绕"检查新目录中是否生成了正确的文件"。handler-transformer 的断言需要：

1. **验证原文件被正确改写**：init 块被移除、handler 方法被添加、Service 被注入
2. **验证新文件被正确生成**：DTO、Service、ServiceImpl 在正确的包路径下生成
3. **使用 `File.listFiles(FilenameFilter)` 查找生成的文件**：因为文件名由工具自动推导

### 7.4 原 yml 中的 doc-analyzer 遗留字段

部分 handler-transformer 的原始 `.allison1875.yml` 中可能残留 `flushTo` 和 `markdownDir` 等
doc-analyzer 专用字段（例如 `basic-get`）。这些字段对 handler-transformer 无影响，但为了
配置整洁，迁移时应一并移除。**检查清单**：

- [ ] 移除 `flushTo`
- [ ] 移除 `markdownDir`
- [ ] 移除 `dslDir`（如有）

### 7.5 批量迁移的高效流程

标准 case 的迁移高度模式化，推荐以下流程：

1. **批量复制资源**：使用循环 rsync 复制
2. **统一修改 yml**：补齐 6 个 `*Module: "."`
3. **统一修改 pom.xml**：删除 `<build>` 节点 + 替换版本占位符
4. **逐个翻译 verify.groovy → Java Test**：这是唯一需要逐 case 细看的步骤
5. **批量运行测试**：`mvn clean test`
