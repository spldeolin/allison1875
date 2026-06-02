---
name: integrate-allison1875
description: Configure .allison1875.yml for a Spring Boot project and install the allison1875 CLI. Use when the user asks to integrate allison1875, configure .allison1875.yml, or run allison1875 tools against a Spring Boot project.
---

# 接入 Allison 1875

Allison 1875 是基于 Java AST 的代码生成 CLI 工具，面向 Spring Boot + MyBatis 项目，从 DSL / 数据库表 / YAML 生成
Controller、Service、DTO、Mapper、XML 等样板代码。

接入流程：安装 CLI → 收集项目信息 → 编写 `.allison1875.yml` → 运行工具。

## 一、安装 CLI

如果 `allison1875` 命令可用，则跳过这步。
在 allison1875 仓库根目录执行：

```bash
mvn install -DskipTests
./install-cli.sh
```

安装后可在任意目录使用 `allison1875` 命令。

## 二、信息收集方法论

按以下顺序分析目标项目，先定骨架再填细节：

```
pom.xml → 源码目录结构 → Controller → application.properties → Mapper XML
```

### 2.1 从 pom.xml 确定全局参数

| 关注点                       | 推理目标                                          |
|---------------------------|-----------------------------------------------|
| 子模块列表                     | → 识别 api/application/domain/infrastructure 分层 |
| lombok 依赖                 | → `isDataModelWithoutLombok`                  |
| Spring Boot 版本            | → 3.x 则 `enableJavaxMoveToJakarta: true`      |

### 2.2 从源码目录结构确定 domains

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

### 2.3 从 Controller 推理 codeSnippet

```bash
# 找统一返回类
grep "import.*Result\|import.*Response" controller/*.java

# 查看类定义确认泛型形式
# 例如：public class ApiBaseResult<T> { ... T result; }
# → requestResultTypeDeclaration: "ApiBaseResult<${dataType}>"

# 查看静态工厂方法
# → requestResultSuccessNoData:  "ApiBaseResult.successRet()"
# → requestResultSuccessWithData: "ApiBaseResult.successRet(${data})"

# 找分页类
grep "import.*Page" controller/*.java

# 查看构造器/工厂方法
# → constructPageResult:      "new PageResult<>(pageNo, pageSize, (int) ${total}, ${dtos})"
# → constructEmptyPageResult: "new PageResult<>(pageNo, pageSize, 0, java.util.Collections.emptyList())"
```

> `${total}` 和 `${dtos}` 是固定占位符，必须原样使用。构造方式要与项目实际的分页类构造器/工厂方法严格匹配。

```bash
# jakarta vs javax
grep "import javax\.\|import jakarta\." controller/*.java
# → enableJavaxMoveToJakarta
```

### 2.4 从 application.properties 推理持久层

| properties 键                   | 映射到        |
|--------------------------------|------------|
| `spring.datasource.*.jdbc-url` | `jdbcUrl`  |
| `spring.datasource.*.username` | `userName` |
| `spring.datasource.*.password` | `password` |
| JDBC URL 中 `/dbname?`          | `schema`   |

### 2.5 从 Mapper XML 推理逻辑删除

```bash
grep "delete_flag\|is_deleted" mapper/*.xml | head -20
```

观察 `SET delete_flag = 1` 和 `AND delete_flag = 0` → 得出 `deletedSql` / `notDeletedSql`。

## 三、跨模块项目的 Module/Package 映射

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

- `designPackage` — Design 类
- `paramDTOPackage` — Mapper 方法 Param 类
- `recordDTOPackage` — Mapper 方法 Record 类
- `wholeDTOPackage` — WholeDTO 类

在 `persistenceModule` 对应的业务子包下规划占位包名（如 `xxx.spec.param`），allison1875 运行时自动创建。

## 四、配置 `.allison1875.yml`

在**工程根目录**创建 `.allison1875.yml`。YAML 键名使用 camelCase，与 Java 字段名一致。

### 配置方式约定

- **可推断**：通过扫描项目得到，直接填写并简要说明依据。
- **需询问**：无法从代码可靠推断时（如密码、逻辑删除、分页类型等），列出问题询问用户。
- **不可省略**：即便部分字段有默认值，也必须在 yml 中显式声明。
- **按需配置 domains**：若用户已明确指出业务/模块/领域，`domains` 只配置用户指定的 domain；未指定时再自行识别并询问。

### 完整字段示例

```yaml
# ==================== 业务领域 ====================
# *Module 字段为绝对路径。
# 只需配置用户指定的领域，无需列出项目所有领域。
# 用户未指定时，如果项目结构较为复杂，识别并询问后再决定配哪些 domain。
domains:
  - name: user

    # -- 控制层 --
    controllerModule: /abs/path/to/my-web
    controllerPackage: com.company.proj.user.controller

    # -- DTO 层 --
    dtoModule: /abs/path/to/my-web
    reqDTOPackage: com.company.proj.user.dto.req
    respDTOPackage: com.company.proj.user.dto.resp

    # -- 枚举层 --
    enumModule: /abs/path/to/my-common
    enumPackage: com.company.proj.user.enums

    # -- 业务层 --
    serviceModule: /abs/path/to/my-service
    servicePackage: com.company.proj.user.service
    serviceImplModule: /abs/path/to/my-service
    serviceImplPackage: com.company.proj.user.service.impl

    # -- 持久层 --
    persistenceModule: /abs/path/to/my-dao
    mapperPackage: com.company.proj.user.mapper
    entityPackage: com.company.proj.user.entity
    designPackage: com.company.proj.user.design
    paramDTOPackage: com.company.proj.user.dto.param
    recordDTOPackage: com.company.proj.user.dto.record
    mapperXmlDirs:
      - src/main/resources/mapper

    # -- WholeDTO 层 --
    wholeDTOModule: /abs/path/to/my-service
    wholeDTOPackage: com.company.proj.user.dto

# ==================== 公共配置 ====================
author: Allister                    # git config user.name 或代码中常见 @author
enableJavaxMoveToJakarta: false     # Spring Boot 3.x → true
isDataModelWithoutLombok: false     # 项目是否不用 Lombok
enableNoModifyAnnounce: true        # 一律 true

# ==================== handler-transformer ====================
enableOneService: true              # 一个 Controller 对应一个 Service

# ==================== persistence-generator ====================
jdbcUrl: jdbc:mysql://127.0.0.1:3306
userName: root
password: root
schema: my_database
tables: # 空 = schema 下全部表
  - teacher
  - student
ddl: null                           # DDL 在 H2 构建（与 jdbcUrl 二选一）
enableGenerateDesign: true
pageParamStyle: PAGE_NO_PAGE_SIZE   # PAGE_NO_PAGE_SIZE | OFFSET_LIMIT
isEntityEndWithEntity: true
deletedSql: 'delete_flag = 1'       # 逻辑删除已删条件
notDeletedSql: 'delete_flag = 0'    # 逻辑删除未删条件

# ==================== star-transformer ====================
wholeDTONamePostfix: WholeDTO

# ==================== doc-analyzer ====================
dependencyDirsOrJavaFilePath: [ ]
globalUrlPrefix: ''
flushTo:
  - MARKDOWN
markdownDir: api-docs
dslDir: api-dsls
showdocBaseCatName: doc-analyzer
yapiUrl: null
yapiToken: null
showdocUrl: null
showdocApiKey: null
showdocApiToken: null
singleEndpointPerMarkdown: false
mvcHandlerQualifierWildcards: [ ]
getEnumCodeMethodName: getCode
getEnumTitleMethodName: getTitle

# ==================== form-generator ====================
dslPath: ./forms.yml

# ==================== codeSnippet ====================
codeSnippet:
  pageTypeQualifier: com.company.proj.common.PageInfo
  requestResultQualifier: com.company.proj.common.RequestResult
  requestResultTypeDeclaration: RequestResult<${dataType}>
  requestResultSuccessNoData: RequestResult.success()
  requestResultSuccessWithData: RequestResult.success(${data})
  controllerRequestMapping: /api/v1/${formName}
  shortUuidGeneration: UUID.randomUUID().toString().replaceAll("-", "").toLowerCase()
  collectionEmptyCheck: ${list} == null || ${list}.isEmpty()
  constructPageResult: "new PageResult<>(${total}, ${dtos})"
  constructEmptyPageResult: "new PageResult<>()"
```

## 五、常见陷阱

| 陷阱                                   | 说明                              | 解法                            |
|--------------------------------------|---------------------------------|-------------------------------|
| Module 路径写成相对路径                      | `*Module` 字段要求绝对路径              | 始终用完整文件系统路径                   |
| DTO 分散在多个子包                          | 配置只接受一个包名                       | 统一配为上层包                       |
| serviceImplPackage 不一定是 service.impl | 有些项目 impl 分布在各 service 子包下      | 可与 servicePackage 相同          |
| entityPackage 不一定叫 entity            | 部分项目用 `module`/`model` 包名       | grep 实际包名确认                   |
| Config 有默认值 ≠ 可以不写                   | 显式声明所有字段利于维护                    | 即便默认值也写出                      |
| mapperXmlDirs 默认是 `mapper/`          | 项目可能在 `mapper/mysql/` 子目录       | 检查实际 resources 目录             |
| 跨模块持久层                               | Mapper/Entity/XML 不在当前模块        | `persistenceModule` 指向持久层所在模块 |
| 混合持久层（MongoDB + MyBatis）             | DAO 包和 Mapper 包分属不同技术           | 只映射 MyBatis 部分                |
| entity 包名叫 model                     | `entityPackage` 来自实际包名          | grep `package.*model` 确认      |
| 占位包不存在                               | designPackage 等是 `@NotEmpty` 必填 | 规划占位包名，运行时自动创建                |
| 多数据源的 mapperXmlDirs                  | 按数据源拆分 XML 目录                   | 配共享目录，结合 `tables` 限定          |
| application.properties 位于其他模块        | 多模块项目中 properties 在启动模块         | 沿模块依赖链查找                      |

## 六、校验清单

完成编写后逐项检查：

- [ ] `domains` 至少一个，`name` 不为空
- [ ] 所有 `*Module` 字段是绝对路径且目录存在
- [ ] 所有 `*Package` 字段与实际源码包名一致
- [ ] `enableJavaxMoveToJakarta` 与项目实际 import 一致
- [ ] `codeSnippet.constructPageResult` 和 `constructEmptyPageResult` 不为空
- [ ] `jdbcUrl` 可连通目标数据库（如果配了 persistence-generator）
- [ ] `deletedSql` / `notDeletedSql` 与 Mapper XML 写法一致
- [ ] `mapperXmlDirs` 路径相对于 persistenceModule 的 basedir
- [ ] Config.java 中每个字段都已在 yml 中显式声明
- [ ] 跨模块场景下 `persistenceModule` 指向 Mapper/Entity/XML 所在模块
- [ ] 占位包名符合项目包命名规范
- [ ] `requestResultTypeDeclaration` 泛型形式与项目统一返回类一致
- [ ] `constructPageResult` 参数顺序与项目 PageResult 构造器一致

### 自动化校验脚本

```bash
# 校验所有 *Module 路径
grep "Module:" .allison1875.yml | grep "^[^#]" | awk -F': ' '{print $2}' | while read dir; do
  [ -d "$dir" ] && echo "✅ $dir" || echo "❌ MISSING: $dir"
done
```

## 七、校验与后续

配置完成后，将 `examples/forms.yml` 拷贝到项目根目录，运行以下命令验证配置：

```bash
allison1875 --tool=form-generator --domain=<domainName> --config=<path/to/.allison1875.yml>
```

如因 Java 版本导致失败，尝试使用 `jenv` 切换正确的 Java 版本。
