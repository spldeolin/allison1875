# 编写 .allison1875.yml 方法论

## 1. 分析顺序

```
pom.xml → 源码目录结构 → Controller → application.properties → Mapper XML → Config.java
```

**按此顺序推理，先定骨架再填细节。**

### 1.1 从 pom.xml 确定全局参数

| 关注点                       | 推理目标                                          |
|---------------------------|-----------------------------------------------|
| `<maven.compiler.source>` | → `javaVersion`                               |
| 子模块列表                     | → 识别 api/application/domain/infrastructure 分层 |
| lombok 依赖                 | → `isDataModelWithoutLombok`                  |

### 1.2 从源码目录结构确定 domains

**核心思路**：沿着 `src/main/java` 的包树，识别各层的包名。

| 要找的东西               | 怎么找                                    | 映射到                                |
|---------------------|----------------------------------------|------------------------------------|
| `@RestController` 类 | grep `@RestController`                 | `controllerPackage`                |
| req/resp DTO        | 按包名 `dto`、`req`、`resp`                 | `reqDTOPackage` / `respDTOPackage` |
| 枚举类                 | 包名含 `enums`                            | `enumPackage`                      |
| Service 接口          | 包名含 `service`                          | `servicePackage`                   |
| ServiceImpl         | 包名含 `service.impl` 或 service 子包下的 impl | `serviceImplPackage`               |
| Mapper 接口           | 包名含 `mapper`                           | `mapperPackage`                    |
| Entity/Model        | 包名含 `entity`/`module`/`model`          | `entityPackage`                    |
| Mapper XML          | `src/main/resources/mapper` 下          | `mapperXmlDirs`                    |

> **注意**：`*Module` 字段必须用**绝对路径**，不是包名。

### 1.3 从 Controller 确定 codeSnippet

grep Controller 文件中的 import 语句：

```bash
grep "import.*ApiBaseResult\|import.*PageResult\|import.*RequestResult" **/*.java
```

- 统一返回类 → `requestResultQualifier` + `requestResultTypeDeclaration`
- 分页类 → `pageTypeQualifier` + `constructPageResult`
- `jakarta.*` vs `javax.*` → `enableJavaxMoveToJakarta`

### 1.4 从 application.properties 推理持久层

| properties 键                   | 映射到        |
|--------------------------------|------------|
| `spring.datasource.*.jdbc-url` | `jdbcUrl`  |
| `spring.datasource.*.username` | `userName` |
| `spring.datasource.*.password` | `password` |
| JDBC URL 中 `/dbname?`          | `schema`   |

### 1.5 从 Mapper XML 推理逻辑删除

搜索 Mapper XML 中的 `delete_flag` / `is_deleted` 模式：

```bash
grep "delete_flag\|is_deleted" mapper/*.xml | head -20
```

观察 `SET delete_flag = 1` 和 `AND delete_flag = 0` → 得出 `deletedSql` / `notDeletedSql`。

## 2. 常见陷阱

| 陷阱                                       | 说明                                                   | 解法                                                         |
|------------------------------------------|------------------------------------------------------|------------------------------------------------------------|
| **Module 路径写成相对路径**                      | allison1875 DomainConfig 的 `*Module` 字段要求绝对路径        | 始终用完整的文件系统路径                                               |
| **DTO 分散在多个子包**                          | 项目按子领域拆分 DTO（如 `dto/benchmark/req`），但配置只接受一个包名       | 统一配为上层包（如 `application.dto`）                               |
| **serviceImplPackage 不一定是 service.impl** | 有些项目 impl 分布在各 service 子包下                           | 如果 impl 没有统一包，serviceImplPackage 可与 servicePackage 相同      |
| **entityPackage 不一定叫 entity**            | 部分项目用的是 `module` 包名                                  | 实际看项目结构，不要想当然                                              |
| **Config 有默认值 ≠ 可以不写**                   | 显式写出所有配置项更利于维护和 review                               | 即便是默认值也写出来                                                 |
| **mapperXmlDirs 默认是 `mapper/`**          | 项目可能在 `mapper/mysql/` 子目录                            | 检查实际 resources 目录结构                                        |
| **跨模块持久层**                               | 持久层（Mapper/Entity/XML）不在当前模块而在公共模块                   | `persistenceModule` 指向持久层所在的 Maven 模块绝对路径，不要指向当前模块         |
| **混合持久层（MongoDB + MyBatis）**             | 项目同时用 MongoDB 和 MyBatis，DAO 包和 Mapper 包分属不同技术        | allison1875 管理的是 MyBatis 持久层，MongoDB DAO 不映射到 DomainConfig |
| **entity 包名叫 model**                     | `entityPackage` 的值来自实际包名，不受字段名暗示                     | grep `package.*model` 确认真实包名                               |
| **占位包（param/record/wholeDTO 不存在）**       | DomainConfig 中这些字段是 `@NotEmpty`，即使项目尚未使用也必须填         | 在持久层 module 的业务子包下规划占位包名，allison1875 运行时自动创建               |
| **Design 包已存在但为空**                       | persistence-generator 生成 Design 类时需要 designPackage   | 可直接配为已有空包，无需额外创建                                           |
| **多数据源项目的 mapperXmlDirs**                | 项目按数据源拆分 XML 目录（如 `mapper/mysql/lcap/`），spec 与其他领域共享 | 配为共享目录即可，结合 `tables` 字段限定 spec 表                           |
| **application.properties 位于其他模块**        | 多模块项目中 properties 可能在 app 启动模块而非当前模块                 | 沿着模块依赖链找到 app 模块中的 properties                              |

## 3. 跨模块项目的 Module/Package 映射策略

在多模块 Maven 项目中，一个业务领域的代码往往分布在多个模块中。DomainConfig 的各 `*Module` 字段允许指向不同模块，是专门为此设计的。

### 3.1 典型跨模块布局

```
project-root/
├── module-api/          ← Controller / DTO / Enum
├── module-service/      ← Service / ServiceImpl
└── module-core/         ← Mapper / Entity / Design / Mapper XML
```

**映射规则**：

| DomainConfig 字段                                 | 指向模块             |
|-------------------------------------------------|------------------|
| `controllerModule` / `dtoModule` / `enumModule` | `module-api`     |
| `serviceModule` / `serviceImplModule`           | `module-service` |
| `persistenceModule`                             | `module-core`    |

> 所有 `*Module` 字段**必须使用绝对路径**，且可以各自指向不同模块。

### 3.2 多模块项目的推理步骤

1. **从父 pom.xml 识别所有子模块**
2. **grep `@RestController` 定位 Controller 所在模块** → `controllerModule`
3. **grep `@Mapper` 或 `import.*mybatis` 定位 Mapper 所在模块** → `persistenceModule`
4. **检查 `@MapperScan` 注解确认 Mapper 扫描的包路径** → `mapperPackage`
5. **grep `package.*entity` 或 `package.*model` 定位 Entity 包名** → `entityPackage`
6. **application.properties 可能在启动模块（如 `module-app`），而非业务模块** — 沿模块依赖链查找

### 3.3 占位包处理

DomainConfig 的以下字段是 `@NotEmpty` 必填的，但项目中可能尚未使用：

- `designPackage` — Design 类（query-transformer 使用）
- `paramDTOPackage` — Mapper 方法的 Param 类
- `recordDTOPackage` — Mapper 方法的 Record 类
- `wholeDTOPackage` — star-transformer 的 WholeDTO 类

**处理策略**：在 `persistenceModule` 对应的业务子包下规划占位包名（如 `xxx.spec.param`），allison1875 运行时会自动创建。

## 4. codeSnippet 推理方法

### 4.1 统一返回类

```bash
# 1. 找到统一返回类的全限定名
grep "import.*Result\|import.*Response" controller/*.java

# 2. 查看类定义，确认泛型形式
# 例如：public class ApiBaseResult<T> { ... T result; }
# → requestResultTypeDeclaration: "ApiBaseResult<${dataType}>"

# 3. 查看静态工厂方法
# 例如：public static <T> ApiBaseResult<T> successRet() { ... }
#       public static <T> ApiBaseResult<T> successRet(T data) { ... }
# → requestResultSuccessNoData:  "ApiBaseResult.successRet()"
# → requestResultSuccessWithData: "ApiBaseResult.successRet(${data})"
```

### 4.2 分页类

```bash
# 1. 找到分页类的全限定名
grep "import.*Page" controller/*.java

# 2. 查看构造器/工厂方法
# 例如：new PageResult(pageNo, pageSize, total, data)
# → constructPageResult:      "new PageResult<>(pageNo, pageSize, (int) ${total}, ${dtos})"
# → constructEmptyPageResult: "new PageResult<>(pageNo, pageSize, 0, java.util.Collections.emptyList())"
```

> **注意**：`${total}` 和 `${dtos}` 是 allison1875 的固定占位符，必须原样使用。构造方式要与项目实际的分页类构造器/工厂方法严格匹配。

## 5. Config.java 完整字段清单

编写 yml 时，必须对照 `Config.java` 中的**全部字段**逐一写出，即便是默认值。以下是按分组的完整清单：

### 5.1 公共配置

| 字段                         | 类型      | 默认值              | 必填 | 说明                      |
|----------------------------|---------|------------------|----|-------------------------|
| `author`                   | String  | `"Allison 1875"` | ✅  | 为生成的代码指定作者              |
| `isDataModelWithoutLombok` | Boolean | `false`          | ✅  | 生成的DataModel是否不使用Lombok |
| `enableNoModifyAnnounce`   | Boolean | `true`           | ✅  | 是否生成"不要修改"声明            |
| `enableJavaxMoveToJakarta` | Boolean | `false`          | ✅  | javax → jakarta 命名空间迁移  |
| `javaVersion`              | String  | `"21"`           | ✅  | 编译版本                    |

### 5.2 Guice Module 配置

| 字段                           | 默认值                             | 说明                        |
|------------------------------|---------------------------------|---------------------------|
| `docAnalyzerModule`          | `...DocAnalyzerModule`          | doc-analyzer Guice Module |
| `handlerTransformerModule`   | `...HandlerTransformerModule`   | handler-transformer       |
| `persistenceGeneratorModule` | `...PersistenceGeneratorModule` | persistence-generator     |
| `queryTransformerModule`     | `...QueryTransformerModule`     | query-transformer         |
| `starTransformerModule`      | `...StarTransformerModule`      | star-transformer          |
| `formGeneratorModule`        | `...FormGeneratorModule`        | form-generator            |

### 5.3 handler-transformer 配置

| 字段                 | 类型      | 默认值     | 说明                       |
|--------------------|---------|---------|--------------------------|
| `enableOneService` | Boolean | `false` | 一个Controller调用同一个Service |

### 5.4 persistence-generator 配置

| 字段                          | 类型                          | 默认值                 | 必填 | 说明                       |
|-----------------------------|-----------------------------|---------------------|----|--------------------------|
| `jdbcUrl`                   | String                      | —                   |    | 数据库连接URL                 |
| `userName`                  | String                      | —                   |    | 数据库用户名                   |
| `password`                  | String                      | —                   |    | 数据库密码                    |
| `schema`                    | String                      | —                   |    | 数据库schema                |
| `ddl`                       | String                      | —                   |    | 使用DDL在H2中构建（与jdbcUrl二选一） |
| `tables`                    | List\<String\>              | `[]`                |    | 指定表（空=schema下所有表）        |
| `enableGenerateDesign`      | Boolean                     | `true`              | ✅  | 是否生成Design类              |
| `pageParamStyle`            | PageParamStyleEnum          | `PAGE_NO_PAGE_SIZE` | ✅  | 分页参数风格                   |
| `isEntityEndWithEntity`     | Boolean                     | `true`              | ✅  | Entity类名是否以Entity结尾      |
| `deletedSql`                | String                      | —                   |    | 逻辑删除-已删条件SQL             |
| `notDeletedSql`             | String                      | —                   |    | 逻辑删除-未删条件SQL             |
| `entityExistenceResolution` | FileExistenceResolutionEnum | `OVERWRITE`         | ✅  | Entity文件已存在时的处理方式        |

### 5.5 star-transformer 配置

| 字段                    | 类型     | 默认值          | 说明           |
|-----------------------|--------|--------------|--------------|
| `wholeDTONamePostfix` | String | `"WholeDTO"` | WholeDTO类的后缀 |

### 5.6 doc-analyzer 配置

| 字段                             | 类型                  | 默认值              | 必填 | 说明                 |
|--------------------------------|---------------------|------------------|----|--------------------|
| `dependencyDirsOrJavaFilePath` | List\<File\>        | `[]`             | ✅  | 外部依赖项目的目录或Java文件路径 |
| `globalUrlPrefix`              | String              | `""`             | ✅  | 全局URL前缀            |
| `flushTo`                      | List\<FlushToEnum\> | `[MARKDOWN]`     | ✅  | 文档保存目标             |
| `yapiUrl`                      | String              | —                |    | YApi请求URL          |
| `yapiToken`                    | String              | —                |    | YApi项目TOKEN        |
| `markdownDir`                  | File                | `"api-docs"`     |    | Markdown输出目录       |
| `showdocBaseCatName`           | String              | `"doc-analyzer"` |    | ShowDoc基础目录名       |
| `showdocUrl`                   | String              | —                |    | ShowDoc API URL    |
| `showdocApiKey`                | String              | —                |    | ShowDoc api_key    |
| `showdocApiToken`              | String              | —                |    | ShowDoc api_token  |
| `dslDir`                       | File                | `"api-dsls"`     |    | DSL输出目录            |
| `singleEndpointPerMarkdown`    | Boolean             | `false`          |    | 每个Endpoint输出到单独文件  |
| `mvcHandlerQualifierWildcards` | List\<String\>      | —                |    | 限定handler范围（支持通配符） |
| `getEnumCodeMethodName`        | String              | `"getCode"`      | ✅  | 枚举Code方法名          |
| `getEnumTitleMethodName`       | String              | `"getTitle"`     | ✅  | 枚举Title方法名         |

### 5.7 form-generator 配置

| 字段                  | 类型      | 默认值             | 必填 | 说明                    |
|---------------------|---------|-----------------|----|-----------------------|
| `dslPath`           | File    | `"./forms.yml"` | ✅  | DSL.yml文件路径           |
| `enableDocAnalyzer` | Boolean | `false`         | ✅  | 是否同时用doc-analyzer生成文档 |

### 5.8 codeSnippet 配置

| 字段                             | 默认值                          | 必填 | 说明                            |
|--------------------------------|------------------------------|----|-------------------------------|
| `pageTypeQualifier`            | —                            |    | 分页类全限定名                       |
| `requestResultQualifier`       | —                            |    | 统一返回类全限定名                     |
| `requestResultTypeDeclaration` | —                            |    | 统一返回类型声明（含`${dataType}`占位符）   |
| `requestResultSuccessNoData`   | —                            |    | 无数据成功返回的代码片段                  |
| `requestResultSuccessWithData` | —                            |    | 有数据成功返回的代码片段（含`${data}`占位符）   |
| `controllerRequestMapping`     | `"/api/v1/${formName}"`      | ✅  | Controller路径模板                |
| `shortUuidGeneration`          | `"UUID.randomUUID()..."`     | ✅  | 短UUID生成代码片段                   |
| `collectionEmptyCheck`         | `"${list} == null \|\| ..."` | ✅  | 列表空判断代码片段                     |
| `constructPageResult`          | —                            | ✅  | 构造分页结果（含`${total}`和`${dtos}`） |
| `constructEmptyPageResult`     | —                            | ✅  | 构造空分页结果                       |

## 6. 校验清单

完成编写后，逐项检查：

- [ ] `domains` 至少一个，`name` 不为空
- [ ] 所有 `*Module` 字段是绝对路径且目录存在
- [ ] 所有 `*Package` 字段与实际源码包名一致
- [ ] `enableJavaxMoveToJakarta` 与项目实际 import 一致
- [ ] `javaVersion` 与 pom 的 compiler source 一致
- [ ] `codeSnippet.constructPageResult` 和 `constructEmptyPageResult` 不为空（Config 校验必填）
- [ ] `jdbcUrl` 可连通目标数据库（如果配了 persistence-generator）
- [ ] `deletedSql` / `notDeletedSql` 与 Mapper XML 中的写法一致
- [ ] `mapperXmlDirs` 路径相对于 persistenceModule 的 basedir
- [ ] Config.java 中的**每个字段**都已在 yml 中显式声明（包括有默认值的字段）
- [ ] 跨模块场景下，`persistenceModule` 指向的模块包含 Mapper/Entity/XML，而非 Controller 所在模块
- [ ] 占位包名（designPackage/paramDTOPackage/recordDTOPackage/wholeDTOPackage）符合项目包命名规范
- [ ] `requestResultTypeDeclaration` 中的泛型形式与项目统一返回类的类定义一致
- [ ] `constructPageResult` 的参数顺序与项目 PageResult 构造器/工厂方法的参数顺序一致

### 6.1 自动化校验脚本

可在工程根目录运行以下脚本快速校验 Module 路径和 Package 目录是否存在：

```bash
# 校验所有 *Module 路径（从 yml 中提取绝对路径并检查目录存在性）
grep "Module:" .allison1875.yml | grep "^[^#]" | awk -F': ' '{print $2}' | while read dir; do
  [ -d "$dir" ] && echo "✅ $dir" || echo "❌ MISSING: $dir"
done

# 校验关键 Package 对应的源码目录
# 需要将 package 名转为路径后拼接 Module 路径检查
```
