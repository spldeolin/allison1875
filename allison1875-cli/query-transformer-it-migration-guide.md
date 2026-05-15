# query-transformer IT Case 迁移指南

> 将 `allison1875-maven-plugin/src/it/query-transformer/` 下的集成测试迁移到 `allison1875-cli` 模块，
> 以 `Bootstrap.main()` 作为入口，使用 JUnit 5 驱动并编写 Java 断言。

---

## 1. 背景与目标

### 为什么迁移？

与 persistence-generator IT 迁移同理：项目计划逐步废弃 `allison1875-maven-plugin` 模块。
原 IT case 通过 `maven-invoker-plugin` 驱动，迁移后改为在 `allison1875-cli` 模块中，
通过 `Bootstrap.main()` (CLI 入口) + JUnit 5 驱动集成测试。

### 迁移进度

| #  | Case 名              | 状态    |
|----|---------------------|-------|
| 1  | `select-basic`      | ✅ 已完成 |
| 2  | `select-properties` | ✅ 已完成 |
| 3  | `select-page`       | ✅ 已完成 |
| 4  | `select-map-group`  | ✅ 已完成 |
| 5  | `all-operators`     | ✅ 已完成 |
| 6  | `order-by`          | ✅ 已完成 |
| 7  | `update-delete`     | ✅ 已完成 |
| 8  | `param-threshold`   | ✅ 已完成 |
| 9  | `where-forced`      | ✅ 已完成 |
| 10 | `join-basic`        | ✅ 已完成 |

---

## 2. 核心难点：query-transformer 依赖 persistence-generator 的输出

### 2.1 原 invoker 模式的执行流程

在 `invoker.properties` 中声明：

```
invoker.goals=allison1875:persistence-generator process-classes compile allison1875:query-transformer
```

执行顺序为：

```
allison1875:persistence-generator  →  生成 Entity/Design/Mapper/XML 到 src/main/java/
         ↓
process-classes + compile          →  编译生成的 Design 类 + 拷贝 java-qt 源码到 src/main/java/
         ↓
allison1875:query-transformer      →  解析 Service 中的 Design DSL 链 → 转换为 Mapper 调用
```

其中 `java-qt` 到 `java` 的拷贝由 `pom.xml` 中 `maven-resources-plugin` 的 `copy-qt-sources` execution 完成。

### 2.2 CLI 模式的等效流程

在 `QueryTransformerItBaseTest` 基类中实现等效流程：

```
① 拷贝资源到 target/it/qt-{caseName}/
② 解析 yml 相对路径 → 绝对路径并回写
③ Bootstrap.main(--tool=persistence-generator)   →  生成 Entity/Design/Mapper/XML
④ 拷贝 src/main/java-qt/ → src/main/java/       →  模拟 maven-resources-plugin 的 copy-qt-sources
⑤ Bootstrap.main(--tool=query-transformer)        →  内部 mvn compile + 解析 DSL 链
```

关键点：

- **步骤③⑤**都调用 `Bootstrap.main()`，分别传 `--tool=persistence-generator` 和 `--tool=query-transformer`
- **步骤④**在基类中手动实现 `java-qt` 到 `java` 的递归文件拷贝
- **步骤⑤**中 `QueryTransformer.process()` 内部会通过 `MavenProjectClassLoaderUtils.buildClassLoader()` 执行
  `mvn compile`，自动编译所有源码（包括 Design 和 Service）

### 2.3 java-qt 目录的作用

`src/main/java-qt/` 存放的是含有 Design DSL 链的 Service 源码。这些文件在编译阶段**不能和 Design 放在同一个 source root**
，因为 Service 中引用了 `TXxxDesign`，而 Design 是由 persistence-generator 动态生成的。

原 invoker 模式通过分阶段执行解决：先生成 Design → 编译 → 拷贝 Service → 再编译 + 运行 query-transformer。

CLI 模式下，`QueryTransformer.process()` 会自行调用 `mvn compile`（包含 Design + Service），因此只要确保在运行
query-transformer 前，`src/main/java/` 下同时存在 Design 文件和 Service 文件即可。

---

## 3. 架构与关键文件说明

### 3.1 关键文件位置

| 文件            | 路径                                                                                                                | 说明                            |
|---------------|-------------------------------------------------------------------------------------------------------------------|-------------------------------|
| **基类**        | `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/querytransformer/QueryTransformerItBaseTest.java` | 封装通用流程，所有 test 继承此类           |
| **测试类**       | `allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/querytransformer/XxxItTest.java`                  | 参照 SelectBasicItTest 编写新 test |
| **测试资源**      | `allison1875-cli/src/test/resources/it/query-transformer/{caseName}/`                                             | 每个 case 的资源目录                 |
| **原 IT 源**    | `allison1875-maven-plugin/src/it/query-transformer/{caseName}/`                                                   | 原始 IT case 目录                 |
| **Bootstrap** | `allison1875-cli/src/main/java/com/spldeolin/allison1875/cli/Bootstrap.java`                                      | CLI 入口                        |

### 3.2 QueryTransformerItBaseTest 基类功能

基类提供两个方法供子类调用：

```java
// 单 domain 场景
private void runQueryTransformer(String caseName)

// 多 domain 场景
protected void runQueryTransformer(String caseName, String domainNam
```

内部流程：

1. **拷贝资源**：`it/query-transformer/{caseName}/` → `target/it/qt-{caseName}/`（使用 `qt-` 前缀避免与
   persistence-generator case 冲突）
2. **解析并回写 yml**：补齐 `*Module` 字段的绝对路径、`mapperXmlDirs` 的绝对路径
3. **运行 persistence-generator**：`Bootstrap.main(--tool=persistence-generator)`
4. **拷贝 java-qt → java**：递归将 `src/main/java-qt/` 下所有文件拷贝到 `src/main/java/`
5. **恢复 classloader**：persistence-generator 可能修改了 context classloader
6. **运行 query-transformer**：`Bootstrap.main(--tool=query-transformer)`

### 3.3 与 PersistenceGeneratorItBaseTest 的区别

| 差异点              | PersistenceGeneratorItBaseTest         | QueryTransformerItBaseTest                      |
|------------------|----------------------------------------|-------------------------------------------------|
| **资源前缀**         | `it/persistence-generator/{caseName}/` | `it/query-transformer/{caseName}/`              |
| **工作目录前缀**       | `target/it/{caseName}/`                | `target/it/qt-{caseName}/`                      |
| **Bootstrap 调用** | 仅一次（persistence-generator）             | 两次（先 persistence-generator，再 query-transformer） |
| **额外步骤**         | 无                                      | 拷贝 `java-qt/` → `java/`                         |
| **输出验证对象**       | Entity/Mapper/XML/Design               | Mapper（增加方法）/XML（增加SQL）/Service（DSL→Mapper调用）   |
| **源码目录**         | 通常为空（代码由工具全量生成）                        | 有预置的 Service（java-qt 下）                         |
| **pom.xml 额外依赖** | 无                                      | `mybatis`（Service 中用到 `@Param` 注解）              |

---

## 4. 标准迁移步骤（逐 case 操作）

### Step 1 — 复制资源文件

```bash
SRC=allison1875-maven-plugin/src/it/query-transformer/{case-name}
DST=allison1875-cli/src/test/resources/it/query-transformer/{case-name}
mkdir -p $DST
rsync -av --exclude='target/' --exclude='invoker.properties' --exclude='verify.groovy' --exclude='README.md' \
  $SRC/ $DST/
```

需要复制的文件：

- `.allison1875.yml` — 配置文件（**需修改，见 Step 2**）
- `pom.xml` — fake Maven 项目 POM（**需修改，见 Step 3**）
- `src/main/java-qt/` — 含 Design DSL 链的 Service 源码（**直接复制，不修改**）
- `src/main/resources/mapper/` — Mapper XML 输出目录（通常为空）

**不需要复制**的文件：

- `invoker.properties` — maven-invoker 专用
- `verify.groovy` — 将用 Java 重写
- `README.md` — 说明文档，不影响测试
- `target/` — 编译产物

### Step 2 — 修改 `.allison1875.yml`

与 persistence-generator 迁移完全一致：补齐 6 个 `*Module: "."` 字段。

### Step 3 — 修改 `pom.xml`

1. **删除整个 `<build>` 节点**（包含 `allison1875-maven-plugin` 和 `maven-resources-plugin` 声明）
2. **替换 `@allison1875.version@`** → `13.0-SNAPSHOT`
3. **保留 `<dependencies>`**：lombok、allison1875-support、**mybatis**（query-transformer case 需要）

### Step 4 — 创建 JUnit 5 测试类

```java
package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

public class XxxItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("{case-name}");

        // 1. Mapper 接口验证
        // 2. Mapper XML 验证
        // 3. Service 文件验证（Design 链被替换为 Mapper 调用）
    }

}
```

### Step 5 — 运行验证

```bash
mvn test -pl allison1875-cli -am -Dtest=XxxItTest -Dsurefire.failIfNoSpecifiedTests=false
```

### Step 6 — 删除原 IT case 目录

```bash
rm -rf allison1875-maven-plugin/src/it/query-transformer/{case-name}/
```

---

## 5. query-transformer 特有的断言模式

### 5.1 Mapper 接口断言

验证 query-transformer 在已有 Mapper 接口中**追加**了新方法：

```java
assertTrue(mapperContent.contains("findById"), "Mapper should contain findById method");

assertTrue(mapperContent.contains("listAll"), "Mapper should contain listAll method");
```

### 5.2 Mapper XML 断言

验证 XML 中追加了对应的 SQL：

```java
// 注意 XML 中 id 属性使用单引号还是双引号取决于工具生成方式
assertTrue(xmlContent.contains("id='findById'") ||xmlContent.

contains("id=\"findById\""), ...);

assertTrue(xmlContent.contains("LIMIT 1"), "one() should generate LIMIT 1");

assertTrue(xmlContent.contains("COUNT(*)"), "count() should generate COUNT(*)");
```

### 5.3 Service 文件断言

验证 Design DSL 链被替换为 Mapper 调用：

```java
// Design 链不再出现
assertFalse(serviceContent.contains("TOrderDesign."), "Design chain should be replaced");

// Mapper 被注入
assertTrue(serviceContent.contains("TOrderMapper"), "Mapper should be injected");

assertTrue(serviceContent.contains("tOrderMapper"), "Mapper field should exist");

// 方法调用
assertTrue(serviceContent.contains("tOrderMapper.findById"), "Should call mapper.findById");
```

---

## 6. 迁移经验总结

### 6.1 两次 Bootstrap.main() 调用的 classloader 管理

query-transformer IT 需要在同一个 JUnit 测试方法中调用两次 `Bootstrap.main()`。每次调用都会触发
`DefaultAstForest` 构造，其中 `Thread.currentThread().setContextClassLoader(classLoader)` 会替换
当前线程的 context classloader。

基类在以下关键时机管理 classloader：

1. **进入前**：保存 `originalClassLoader`
2. **persistence-generator 执行后、query-transformer 执行前**：恢复 `originalClassLoader`，确保
   query-transformer 的 `MavenProjectClassLoaderUtils.buildClassLoader()` 从干净状态重新构建
3. **finally 块**：无论成功或失败，恢复 `originalClassLoader`

### 6.2 工作目录使用 qt- 前缀避免冲突

所有 query-transformer IT case 的工作目录使用 `target/it/qt-{caseName}/` 格式，
而非 `target/it/{caseName}/`，避免与 persistence-generator case 同名时的目录冲突。

### 6.3 pom.xml 需要保留 mybatis 依赖

query-transformer 的 IT case 的 `pom.xml` 比 persistence-generator 多一个 `mybatis` 依赖
（3.5.17）。这是因为 Service 中用到了 MyBatis 的 `@Param` 注解，在 `mvn compile` 时需要解析。

### 6.4 所有 case 的 yml 和 DDL 结构一致

所有 10 个 query-transformer IT case 使用相同的 DDL（`t_order` 表）和相同的 domain 结构，
差异仅在于 `src/main/java-qt/com/example/service/OrderService.java` 中的 Design DSL 链不同。

这意味着 yml 修改步骤完全相同（补齐 6 个 `*Module: "."`），可以批量脚本化处理。

### 6.5 query-transformer 自身执行 mvn compile

`QueryTransformer.process()` 内部调用 `MavenProjectClassLoaderUtils.buildClassLoader()`，
该方法会对目标项目执行 `mvn compile dependency:build-classpath`。这意味着不需要在基类中
手动编译 Design 文件——query-transformer 会自动完成。

只要确保在运行 query-transformer 前，`src/main/java/` 下同时存在：

- persistence-generator 生成的 Entity/Design/Mapper 文件
- 从 `java-qt/` 拷贝过来的 Service 文件

即可。

### 6.6 java-qt 目录是 query-transformer IT 特有的

persistence-generator IT 的 `src/main/java/` 通常为空（代码全量生成），
而 query-transformer IT 额外有 `src/main/java-qt/` 存放含 Design DSL 链的 Service 源码。

迁移时需要确保这个目录被完整复制到测试资源中，并且基类的 `copyJavaQtToJava()` 方法
能正确将其内容拷贝到 `src/main/java/`。

### 6.7 select-properties case 的 Record DTO 验证（2026-05-14）

`select-properties` 是第一个需要验证**query-transformer 额外生成文件**的 case。

**6.7.1 单属性与多属性的差异**

| 场景        | DSL 示例                                                    | query-transformer 行为             | 断言方式                      |
|-----------|-----------------------------------------------------------|----------------------------------|---------------------------|
| 单属性       | `.orderNo.list()`                                         | 返回 `List<String>`，不生成 Record DTO | 仅验证 Mapper/XML/Service    |
| 多属性       | `.orderNo.userId.amount.list()`                           | 自动生成 Record DTO                  | 额外验证 `dto/record/` 下文件    |
| 多属性+where | `.orderNo.userId.amount.where().status.eq(status).list()` | 生成 Record DTO + where 条件         | 验证 Record DTO + XML where |

**6.7.2 Record DTO 验证方法**

Record DTO 的类名由工具自动生成（如 `ListOrderSummariesRecordDTO`），不可硬编码。
应使用以下模式验证：

```java
File recordDir = new File(basedir, "src/main/java/com/example/dto/record");

File[] recordFiles = recordDir.listFiles();

assertTrue(recordFiles !=null&&recordFiles.length>0, "...");

// 合并所有 Record DTO 内容，按字段名检查
StringBuilder recordContent = new StringBuilder();
for(

File f :recordFiles){
        recordContent.

append(new String(Files.readAllBytes(f.toPath()),StandardCharsets.UTF_8));
        }

assertTrue(recordContent.toString().

contains("orderNo"), "...");
```

这种方式避免了对自动生成的类名的依赖，更健壮。

### 6.8 select-page case 的 pageParamStyle 与 LIMIT 断言（2026-05-15）

`select-page` 是第一个验证 `.page()` 终止方法的 case。

**6.8.1 pageParamStyle 配置项**

yml 中包含 `pageParamStyle: PAGE_NO_PAGE_SIZE`，该配置决定分页 SQL 的生成格式：

- `PAGE_NO_PAGE_SIZE`：生成 `LIMIT #{offset}, #{limit}`（使用 offset/limit 两个参数）
- 迁移时需完整保留此配置，不可遗漏。

**6.8.2 LIMIT 语句的空格容错**

query-transformer 生成的分页 SQL 中，`LIMIT` 后面的参数分隔逗号前后可能有空格也可能没有：

- `LIMIT #{offset}, #{limit}`（有空格）
- `LIMIT #{offset},#{limit}`（无空格）

断言时应用 OR 条件兼容两种格式：

```java
assertTrue(xmlContent.contains("LIMIT #{offset}, #{limit}")
        ||xmlContent.

contains("LIMIT #{offset},#{limit}"),
        "page() should generate LIMIT #{offset}, #{limit}");
```

**6.8.3 count 方法命名**

page 方法对应的 count 方法命名规则为 `count` + 首字母大写的方法名（如 `pageAll` → `countPageAll`，
`pageByUserId` → `countPageByUserId`）。断言时应对 count 方法名做宽松匹配（同时检查
`countPageAll`/`countAll` 等变体），因为工具可能在不同版本中调整命名策略。

**6.8.4 count 方法返回 long**

分页 count 方法返回 `long` 类型，Mapper 接口断言中可通过 `contains("long")` 验证，
XML 中 count 语句包含 `SELECT COUNT(*)`。

### 6.9 select-map-group case 的 MAP/GROUP 返回风格（2026-05-15）

`select-map-group` 验证 `map()` 和 `group()` 两种聚合终止方法。

**6.9.1 MAP 返回风格：@MapKey 注解**

当 DSL 链以 `.mapByXxx()` 终止时，query-transformer 在 Mapper 接口方法上追加 `@MapKey("xxx")` 注解，
MyBatis 可利用该注解将查询结果映射为 `Map<Key, Entity>`。

断言模式：

```java
assertTrue(mapperContent.contains("@MapKey"), "MAP return style should generate @MapKey annotation");

assertTrue(mapperContent.contains("\"userId\""), "@MapKey should reference userId");
```

注意：`@MapKey` 中的 key 值是**带引号**的字符串（如 `"userId"`），断言时需包含双引号。

**6.9.2 GROUP 返回风格：Collectors.groupingBy**

当 DSL 链以 `.groupByXxx()` 终止时，query-transformer 在 Service 中生成
`Collectors.groupingBy(...)` 调用，按指定字段分组。

断言模式：

```java
assertTrue(serviceContent.contains("groupingBy"),
        "GROUP return style should generate Collectors.groupingBy call");
```

**6.9.3 MAP/GROUP 的共同特征**

- 两者都是按某个字段聚合，该字段作为 mapOrGroupKey
- MAP 返回 `Map<Key, Entity>`，GROUP 返回 `Map<Key, List<Entity>>`
- Mapper XML 中生成的 SQL 与普通 SELECT 相同（无特殊 SQL 语法）
- 差异仅在 Mapper 接口（`@MapKey`）和 Service（`groupingBy` 调用）层面

**6.9.4 无 pageParamStyle 配置**

`select-map-group` 的 yml 中**没有** `pageParamStyle` 配置项（与 `select-page` 不同），
因为 MAP/GROUP 不涉及分页。迁移时注意不要误添加此配置。

### 6.10 all-operators case 的 11 种比较运算符（2026-05-15）

`all-operators` 是断言最多（~40条）的 case，覆盖全部比较运算符。

**6.10.1 运算符与 SQL 对照**

| 运算符     | DSL 写法                    | 生成 SQL                      | XML 断言要点                                   |
|---------|---------------------------|-----------------------------|--------------------------------------------|
| eq      | `.id.eq(id)`              | `id = #{id}`                | 可能有 jdbcType 后缀（如 `#{id,jdbcType=BIGINT}`） |
| ne      | `.status.ne(status)`      | `status != #{status}`       | 直接比较                                       |
| gt      | `.amount.gt(minAmount)`   | `amount > #{minAmount}`     | 参数名可能是 arg 原名或 minAmount                   |
| ge      | `.amount.ge(minAmount)`   | `amount >= #{minAmount}`    | 同上                                         |
| lt      | `.amount.lt(maxAmount)`   | `amount &lt; #{maxAmount}`  | XML 转义 `&lt;`（不是 `<`）                      |
| le      | `.amount.le(maxAmount)`   | `amount &lt;= #{maxAmount}` | XML 转义 `&lt;=`                             |
| like    | `.orderNo.like(keyword)`  | `LIKE CONCAT('%',...,'%')`  | 检查 `LIKE CONCAT('%',`                      |
| in      | `.status.in(statusList)`  | `status IN (` + foreach     | 检查 `IN (` 和 `foreach`                      |
| nin     | `.status.nin(statusList)` | `status NOT IN (` + foreach | 检查 `NOT IN (`                              |
| notnull | `.remark.notnull()`       | `remark IS NOT NULL`        | 无参数                                        |
| isnull  | `.remark.isnull()`        | `remark IS NULL`            | 无参数                                        |

**6.10.2 XML 转义字符**

MyBatis XML 中 `<` 和 `<=` 必须使用 XML 转义：

- `<` → `&lt;`
- `<=` → `&lt;=`

断言时必须使用转义后的形式，不能直接写 `<`。

**6.10.3 eq 的 jdbcType 后缀容错**

eq 运算符生成的 `#{}` 占位符可能带 `jdbcType` 后缀（如 `#{id,jdbcType=BIGINT}`），
断言时用 OR 兼容两种格式：

```java
assertTrue(xmlContent.contains("id = #{id}") ||xmlContent.

contains("id = #{id,jdbcType=BIGINT}"),
        "eq should generate '= #{id}'");
```

**6.10.4 gt/ge/lt/le 的参数名可能变化**

对于 `gt(minAmount)` / `lt(maxAmount)` 等带语义化参数名的运算符，生成 SQL 中可能保留
原参数名（`minAmount`/`maxAmount`）也可能使用属性名（`amount`）。断言时需要 OR 兼容。

**6.10.5 pom.xml 可能有额外依赖**

`all-operators` 的 `OrderService.java` 中使用了 `com.google.common.collect.Lists`，
因此 pom.xml 中额外有 `guava` 依赖（33.0.0-jre）。迁移时必须保留该依赖，
否则 `mvn compile` 失败。

### 6.11 order-by case 的 ORDER BY 子句（2026-05-15）

`order-by` 验证排序功能，断言相对简单。

**关键断言要点：**

- `ORDER BY` 作为关键词出现
- 列名使用数据库列名（如 `created_at`、`amount`）
- `DESC` / `ASC` 关键字明确出现
- WHERE + ORDER BY 组合时，ORDER BY 在 WHERE 之后

### 6.12 update-delete case 的 UPDATE/DELETE 链（2026-05-15）

`update-delete` 验证增删改操作，与 SELECT 有本质区别。

**关键点：**

- UPDATE 生成 `<update>` 标签（而非 `<select>`），验证 `UPDATE t_order` 和 `SET`
- DELETE 生成 `<delete>` 标签，验证 `DELETE FROM t_order`
- 方法返回 `int`（影响行数）
- `over()` 作为终止方法（而非 `one()`/`list()`/`page()`）
- 断言时对 `<update>`/`<delete>` 标签可宽松匹配（检查 `update` 或 `<update`）

### 6.13 param-threshold case 的 ParamDTO 生成（2026-05-15）

`param-threshold` 验证当 where 条件 ≥ 4 个时生成 ParamDTO。

**核心断言模式：**

1. **ParamDTO 文件验证**：在 `dto/param/` 下查找含 "Param" 的 `.java` 文件，用文件名过滤（`name.contains("Param")`）
2. **Mapper 方法行级断言**：按行遍历 Mapper 源码，找到含目标方法名的行，验证该行**不含** `@Param` 但**包含** ParamDTO 类名
3. **XML 引用验证**：`#{fieldName}` 出现在 SQL 中
4. **Service setter 验证**：`.setXxx(` 调用出现

**注意**：该 case 的 `assertFalse` 是针对方法签名行的（不可针对整个文件，因为文件中还有其他含 `@Param` 的方法）。

### 6.14 where-forced case 的 byForced 强制条件（2026-05-15）

`where-forced` 验证 `.whereEvenNull()` 模式下条件不被 `<if test>` 包裹。

**关键断言模式：**

- 提取特定 `<select>` 或 `<delete>` 片段（通过 `id='xxx'` 定位到 `</select>`/`</delete>`）
- 在片段内 assert `!contains("<if test")` 和 `contains("AND column = #{var}")`
- 这种方式比全文 contains 更精确，因为文件中可能还有其他非强制方法带有 `<if test>`

**提取 XML 片段的辅助方法：**

```java
private String extractSelectSection(String xml, String idValue) {
    int start = xml.indexOf("id='" + idValue + "'");
    if (start == -1)
        start = xml.indexOf("id=\"" + idValue + "\"");
    if (start == -1)
        return "";
    int end = xml.indexOf("</select>", start);
    return end > start ? xml.substring(start, end) : xml.substring(start);
}
```

### 6.15 join-basic case 的多表 JOIN（2026-05-15）

`join-basic` 是唯一涉及**多表**的 case。

**DDL 差异：**

- yml 的 `ddl` 字段包含**两个** `CREATE TABLE` 语句（`t_order` + `t_user`）
- `t_order` 表结构与其他 case 不同（无 `remark`、`updated_at` 列）
- 迁移时 DDL 必须原样保留，不可用其他 case 的 DDL 替换

**断言要点：**

- `LEFT JOIN` 关键词
- 表别名 `t1.` / `t2.`
- 被 JOIN 的表名 `t_user`
- Record DTO 包含 join 过来的字段（如 `userName`）
- Service 中 `TOrderDesign.select` 被替换（注意：这里检查的是 `.select(` 而非 `.TOrderDesign.`）

**特殊：该 case 的 verify.groovy 中 assertFalse 针对 `TOrderDesign.select`（含 `.select(`）而非 `TOrderDesign.`（含 `.`
），迁移时保持一致。**

---

## 7. 附录：全部 10 个 case 清单

| #  | Case 名              | 测试类名建议                   | 覆盖功能                                |
|----|---------------------|--------------------------|-------------------------------------|
| 1  | `select-basic`      | `SelectBasicItTest`      | one()/list()/count() 三种终止方法         |
| 2  | `select-properties` | `SelectPropertiesItTest` | select 指定属性（非全量 Entity）             |
| 3  | `select-page`       | `SelectPageItTest`       | 分页查询（page 终止方法）                     |
| 4  | `select-map-group`  | `SelectMapGroupItTest`   | map()/group() 聚合方法                  |
| 5  | `all-operators`     | `AllOperatorsItTest`     | 所有比较运算符（eq/ne/gt/lt/ge/le/in/like等） |
| 6  | `order-by`          | `OrderByItTest`          | orderBy 排序                          |
| 7  | `update-delete`     | `UpdateDeleteItTest`     | update/delete DSL                   |
| 8  | `param-threshold`   | `ParamThresholdItTest`   | 参数阈值（多参数时生成 DTO）                    |
| 9  | `where-forced`      | `WhereForcedItTest`      | byForced 强制条件                       |
| 10 | `join-basic`        | `JoinBasicItTest`        | 基本 JOIN 查询                          |
