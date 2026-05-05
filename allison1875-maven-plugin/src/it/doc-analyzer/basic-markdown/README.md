# basic-markdown 集成测试

## 概述

验证 doc-analyzer 在最基本的 **MARKDOWN** 输出模式下的工作流程。

## 覆盖的功能

### 1. MVC Handler 检测（MvcHandlerDetectorServiceImpl）

- 扫描 `@RestController` 类（UserController）
- 识别 `@GetMapping` 和 `@PostMapping` 标注的 handler 方法
- 反射 controller 类并匹配 AST MethodDeclaration

### 2. MVC Handler 分析（MvcHandlerAnalyzerServiceImpl）

- 从 controller 的 Javadoc 提取 `directCategory`（"用户管理"）
- 从 handler 方法的 Javadoc 提取 `descriptionLines`（"查询用户列表"、"创建用户"）
- 多行描述：listUsers 的 Javadoc 包含第2行描述"根据关键字搜索用户"

### 3. RequestMapping 分析（RequestMappingServiceImpl）

- controller 级 `@RequestMapping("/api/users")` 与 handler 级 `@GetMapping` / `@PostMapping` 的 URL 合并
- HTTP 动词推导：GET、POST

### 4. Query Param 分析（UrlParamServiceImpl.analyzeQueryParams）

- `@RequestParam(required = false) String keyword` 参数识别
- `required = false` 解析（NormalAnnotationExpr 分支）
- String 类型推导为 ValueTypeEnum.STRING

### 5. Request Body 分析（RequestBodyServiceImpl）

- `@RequestBody CreateUserReq req` 参数识别
- CreateUserReq 的字段解析（username, age, email）
- JsonSchema 生成与转换

### 6. Response Body 分析（ResponseBodyServiceImpl）

- `@RestController` 下 handler 方法自动作为 ResponseBody
- `UserResp` 返回类型解析（id, username, age, email）
- `List<UserResp>` 泛型返回类型解析（listUsers 方法）

### 7. 字段分析（FieldServiceImpl.analyzeFieldVars）

- 从 Javadoc 提取字段注释
- 扫描 primarySourceRoot 下的所有 Java 文件

### 8. 校验注解分析（JsgBuilderServiceImpl.analyzeValid）

- `@NotBlank`（username）
- `@NotNull`（age）
- 无校验注解的字段（email）

### 9. Markdown 输出（MarkdownServiceImpl.flushToMarkdown）

- 按 `directCategory` 分组生成 markdown 文件
- URL 区域：HTTP 方法 + URL
- Query Param 表格
- Request Body 表格
- Response Body 表格

### 10. 配置

- `flushTo: [MARKDOWN]`
- `markdownDir: api-docs`
- `globalUrlPrefix: ""`（空字符串，不添加前缀）
- `enableNoModifyAnnounce: true`
- `enableJavaxMoveToJakarta: false`

## 未覆盖的分支

- `@PathVariable` 参数 → Path Param 分析
- `@PutMapping` / `@DeleteMapping` HTTP 动词
- `void` 返回类型（无 Response Body）
- `@Deprecated` / `@since` 标签 → 兼容性说明
- `globalUrlPrefix` 非空时的 URL 前缀拼接
- `singleEndpointPerMarkdown = true` 时每个 endpoint 独立 markdown
- `mvcHandlerQualifierWildcards` 过滤
- 嵌套 DTO（List<XxxReq>）字段遍历
- 枚举字段类型分析
- DSL 输出模式
- `hierarchicalCategories`（package-info.java 层级分类）
- `@RequestParam` 的 `name` / `value` 别名
- `@RequestParam` 的 `defaultValue`
- `@Valid` 级联校验
- `@Size` / `@Min` / `@Max` / `@Length` / `@Pattern` 等校验注解
- `@JsonFormat` 格式标注
- `@JsonProperty.Access` READ_ONLY / WRITE_ONLY 字段过滤
- `#API-DOC-IGNORE#` 字段忽略标记
- `dependencyDirsOrJavaFilePath` 外部依赖目录
