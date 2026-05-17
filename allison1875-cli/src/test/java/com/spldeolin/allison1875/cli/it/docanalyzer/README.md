# doc-analyzer 集成测试用例说明

本目录包含 doc-analyzer 工具的全部集成测试用例。每个测试通过基类 `DocAnalyzerItBaseTest` 将测试资源拷贝到临时目录、改写配置为绝对路径，然后调用
`Bootstrap.main()` 执行 doc-analyzer，最后在生成的 Markdown 或 JSON 文件上进行断言。

---

## 输出模式

### BasicMarkdownItTest

- 验证 MARKDOWN 模式的基本功能
- 生成的 md 文件名取自 Controller 的 Javadoc 首行（如「用户管理.md」）
- 文档中包含 GET/POST 的 HTTP 方法、URL、handler 描述及子描述
- Request Body 字段（字段名、字段注释）和校验注解（@NotBlank、@NotNull）被正确文档化
- Response Body 字段和 `List` 返回类型渲染为 `Object Array`
- Query Param 参数及其 required 状态被正确呈现
- Markdown 结构标记（`### URL`、`### Query Param`、`### Request Body`、`### Response Body`、`---`）完整

### BasicDslItTest

- 验证 DSL（JSON）模式的基本功能
- 生成 JSON 文件到 `api-dsls/` 目录，文件名取自 Controller Javadoc 首行
- JSON 根节点为数组，每个 endpoint 包含 `urls`、`httpMethod`、`descriptionLines`、`requestBodyDescribe`、
  `responseBodyDescribe`、`requestBodyJsonSchema`、`pathParams` 等结构
- 不会同时生成 `api-docs/` 目录（仅 DSL 模式时无 Markdown 输出）

### MarkdownAndDslItTest

- 验证同时启用 MARKDOWN + DSL 双输出模式
- `api-docs/` 和 `api-dsls/` 两个目录均被生成
- CRUD 五个 handler（GET/POST/PUT/DELETE + GET detail）均出现在 Markdown 和 DSL 中
- Markdown 包含 globalUrlPrefix（`/v1`）拼接后的完整 URL、PathVariable、嵌套 DTO 字段
- DSL 中 endpoint 总数为 5

### SingleEndpointMarkdownItTest

- 验证 `singleEndpointPerMarkdown = true` 时，每个 handler 生成独立 md 文件
- 验证 `globalUrlPrefix` 不以 `/` 开头时自动补齐斜杠
- 验证 `@RequestParam(name = "q")` 别名在文档中正确显示
- 验证 `@RequestParam(defaultValue = "1")` 默认值出现在文档中
- 验证 `#API-DOC-IGNORE#` 标记的字段不出现在文档中
- 验证 `void` 返回类型的 handler 不生成 Response Body 区域

---

## 控制器识别与 URL 解析

### ControllerWithResponseBodyItTest

- 验证 `@Controller + @ResponseBody`（非 `@RestController`）场景
- 带有 `@ResponseBody` 注解的 handler 方法能被正确识别并生成文档
- 两个带 `@ResponseBody` 的 handler 各自拥有独立的 Response Body 区域

### NoControllerMappingItTest

- 验证没有类级 `@RequestMapping` 的 Controller 场景
- URL 直接使用 method 级路径（如 `GET /ping`、`GET /version`），无 controller 前缀

### ComposedAnnotationValueItTest

- 验证 `@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`、`@PatchMapping` 等组合注解的 value 属性被正确解析
- 五种 HTTP 方法均能生成正确的组合 URL（controller 前缀 + method 路径）

### RequestMappingParamsItTest

- 验证 `@RequestMapping` 的 `params` 条件被拼接到 URL 中
- controller 级 `params` 和 method 级 `params` 合并呈现（如 `GET /api/config?module=system&action=read`）

### MultipleUrlsPerHandlerItTest

- 验证单个 handler 映射多个 URL 路径（如 `@GetMapping({"/a", "/b"})`）时，文档中以「或」连接显示
- controller 和 method 层 path 的笛卡尔组合逻辑正确

### NoPathControllerAndMethodItTest

- 验证 controller 和 method 都没有 path 属性时 URL 回退为 `/`
- 如 `@RestController` 无 `@RequestMapping` + `@GetMapping` 无 value/path，URL 渲染为 `GET /`

### FallbackHttpVerbItTest

- 验证裸 `@RequestMapping`（无 method 属性）时，combinedVerbs 回退为所有 HTTP 方法
- 文档中 HTTP 方法区域显示 GET, POST, PUT, DELETE, PATCH 等所有方法

### NoHandlerDetectedItTest

- 验证项目中无任何 `@RequestMapping` handler 时，工具正常结束并输出空文档，而非报错

---

## 参数与返回值处理

### PathvarAndReqparamAliasesItTest

- 验证 `@PathVariable` 的 `value` / `name` 别名被正确提取为文档中的参数名
- 验证 `@RequestParam` 的 `value` / `name` 别名被正确提取
- 验证 `@RequestParam` 的 `defaultValue` 出现在文档中
- 验证 primitive `boolean` 类型推导为 `Boolean`
- 验证 Query Param 的 Javadoc `@param` 描述被提取

### PrimitiveAndSimpleReturnItTest

- 验证 `String`、`Integer`、`Boolean` 等简单类型返回值的正确处理
- 验证 `@return` Javadoc 描述被提取并展示在 Response Body 中
- 验证纯 GET 无 `@RequestBody` 的 handler 不生成 Request Body 区域

### JavaRecordItTest

- 验证 Java Record 类型的 DTO 作为请求体和响应体时能被正确解析
- Record component 的字段名和 Javadoc `@param` 注释在文档中正确呈现

### JsonPropertyAccessItTest

- 验证 `@JsonProperty(access = READ_ONLY)` 字段出现在 Response Body 中
- 验证 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")` 格式信息出现在文档中
- 验证同名字段在 Request Body 和 Response Body 中均存在

### TopLevelArrayResponseItTest

- 验证 handler 返回值为 `List<SomeDTO>` 时，根节点为 ArraySchema 的 Markdown 渲染
- Response Body 根节点渲染为 `Object Array`，子字段正常展示

### TopLevelSimpleValueBodyItTest

- 验证 Request/Response Body 根节点为简单值类型（如 `String`、`Integer`）时的 Markdown 渲染
- 简单值类型不渲染字段表格，仅显示类型名称

### IntegerSubtypeRenderItTest

- 验证 `Long`、`Short`、`Byte` 等不同整数类型在 Markdown 中渲染为对应的 JSON 类型名称
- 不统一显示为 Integer，而是各自的实际类型名

---

## 校验注解文档化

### EnumAndValidationItTest

- 验证枚举字段的枚举项（通过 `getCode()` / `getTitle()` 方法）被解析为 `code : title` 格式
- 验证枚举项同时出现在 Request Body 和 Response Body 中
- 验证 `@NotBlank`、`@Size(min, max)`、`@NotNull`、`@Min`、`@Max`、`@Pattern` 等校验注解的描述

### AdvancedValidationItTest

- 验证高级校验注解：`@Length`、`@DecimalMin`、`@DecimalMax`、`@Digits`、`@Future`、`@Positive`
- 验证集合元素级校验（如 `List<@NotBlank @Length(max=20) String>`）的描述以「列表内元素」为前缀

### JakartaValidationItTest

- 验证 `jakarta.validation.constraints.*` 命名空间下的校验注解（而非 `javax.validation`）
- 覆盖 `@NotBlank`、`@Size`、`@NotNull`、`@Min`、`@Max`、`@Past` 的 Jakarta 版本

### NotEmptyAndNegativeValidationItTest

- 验证 `@NotEmpty`、`@Negative`、`@FutureOrPresent`、`@PastOrPresent` 等校验注解
- 与 `advanced-validation` 互补，覆盖其余未覆盖的 javax.validation 校验注解分支

---

## 元数据与兼容性标签

### DeprecatedAndSinceItTest

- 验证 handler 级和 controller 级的 `@since` Javadoc 标签渲染为「兼容性说明」区域
- 验证 handler 级 `@deprecated` 标签渲染为过时提示
- 验证字段级 `@since` 和 `@deprecated` 标签在字段行内正确展示

---

## Handler 过滤（Wildcards）

### MvcHandlerWildcardsItTest

- 验证 `mvcHandlerQualifierWildcards` 配置的过滤效果
- 只有匹配通配符（`*.list*` 或 `*.get{ById,Detail}`）的 handler 出现在文档中
- 不匹配的 handler（如 `createAnimal`、`deleteAnimal`）不出现在文档中

### GlobRegexBranchesItTest

- 验证 `mvcHandlerQualifierWildcards` 中 `*`、`?`、`{,}` 等 glob 语法的正确匹配
- `*Controller.list*` 匹配所有 controller 的 list 方法
- `{Order,User}Controller.get?rder*` 仅匹配 `getOrderDetail`，不匹配 `getUserDetail`

---

## 多 Domain / 多模块场景

### HorizontalDomainsItTest

- 验证水平划分（同一模块内按包区分多个 domain）场景
- 通过 `--domain=user` 选择指定 domain 运行
- 在单模块水平划分下，同一 sourceRoot 中的所有 controller 均会被检测到

### VerticalModulesItTest

- 验证垂直划分（Controller 和 DTO 分属不同 Maven 子模块）场景
- `controllerModule` 和 `dtoModule` 配置能正确解析不同目录的 sourceRoot
- AstForest 能跨多个 sourceRoot 解析 Controller 和 DTO 的 AST

### MixedModuleDomainsItTest

- 验证混合划分（多 domain + 垂直拆分）场景
- user domain 的 DTO 在 `user-api/`，order domain 的 DTO 在 `order-api/`
- 通过 `--domain=order` 运行指定领域，跨模块 DTO 字段被正确解析

---

## 外部依赖目录

### DependencyDirsItTest

- 验证 `dependencyDirsOrJavaFilePath` 配置能正确加载项目外部目录中的 Java 源码
- 外部目录（`external-dto/`）中的 DTO 字段在文档中正确呈现

---

## 层级目录输出

### HierarchicalCategoriesItTest

- 验证 Controller Javadoc 中包含目录分隔符时，md 文件输出到对应的层级子目录
- 如 `api-docs/后台管理模块/系统设置.md`，而非直接放在 `api-docs/` 根目录

---

## 嵌套与复杂结构

### NestedObjectAndArrayItTest

- 验证多层嵌套 Object 和 Object Array 的 Request/Response Body 在 Markdown 表格中的缩进层级渲染
- 3 层以上嵌套 DTO（如 `Order → List<Item> → ItemDetail`）的字段层级正确，被多处引用的共享 DTO 触发「数据结构同」引用路径
- 断言涵盖 `&emsp;` 缩进层级和「数据结构同 XXX」引用文本

### RecursiveReferenceSchemaItTest

- 验证 DTO 中存在自引用（如树形结构 `TreeNode.children: List<TreeNode>`）时不会无限展开或 StackOverflow
- 自引用字段正确渲染为引用路径

---

## 枚举与集合

### EnumInCollectionFieldItTest

- 验证 `List<SomeEnum>` 类型字段的枚举项解析（Collection 泛型递归）
- 枚举项 `code : title` 出现在该字段的描述中

### EnumArrayFieldItTest

- 验证 `SomeEnum[]` 数组类型字段的枚举项解析
- 枚举项出现在数组字段的描述中

### EnumWithoutCodeTitleItTest

- 验证枚举类不具备 `getCode()`/`getTitle()` 方法时，枚举项不出现在文档中（而非报错）
- 枚举字段在文档中出现但无枚举项，不抛异常

---

## 其他边缘场景

### HandlerWithoutJavadocItTest

- 验证 handler 方法没有 Javadoc 时，描述回退为 `ControllerName.methodName` 格式
- 而非空白或报错

---

## 不纳入集成测试的模块

以下 3 个 Service 在 JaCoCo 报告中覆盖率接近 0%，但**不计划通过常规集成测试覆盖**：

- **YApiServiceImpl**（指令覆盖 0%，分支覆盖 0%）
- **YApiOpenApiServiceImpl**（指令覆盖 1%，分支覆盖 0%）
- **ShowdocServiceImpl**（指令覆盖 5%，分支覆盖 0%）

**原因：**

1. 这三个类的核心逻辑是通过 HTTP 请求将文档推送到外部平台（YApi / Showdoc），运行时依赖真实的远程服务实例和有效的 API Key /
   Token
2. 集成测试环境无法也不应启动这些外部服务，硬编码凭证会引入安全风险
3. 若引入 Mock HTTP Server（如 WireMock），测试维护成本高且无法验证真实协议兼容性，投入产出比低
4. 这些类的业务逻辑较薄（主要是 HTTP 调用 + JSON 解析），核心风险在于远程服务的契约变更，适合通过手动冒烟测试或独立的端到端测试验证
