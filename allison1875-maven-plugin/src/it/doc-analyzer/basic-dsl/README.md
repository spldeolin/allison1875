# basic-dsl 集成测试

## 概述

验证 doc-analyzer 在 **DSL（JSON）** 输出模式下的工作流程。

## 覆盖的功能

### 1. MVC Handler 检测（MvcHandlerDetectorServiceImpl）

- 扫描 `@RestController` 类（ProductController）
- 识别 `@GetMapping("/{id}")` 和 `@PostMapping` 标注的 handler 方法

### 2. MVC Handler 分析（MvcHandlerAnalyzerServiceImpl）

- 从 controller 的 Javadoc 提取 `directCategory`（"商品管理"）
- 从 handler 方法的 Javadoc 提取 `descriptionLines`（"根据ID查询商品"、"创建商品"）

### 3. RequestMapping 分析（RequestMappingServiceImpl）

- controller 级 `@RequestMapping("/api/products")` 与 handler 级 `@GetMapping("/{id}")` 的 URL 合并
- URL 路径模板（`/api/products/{id}`）

### 4. Path Param 分析（UrlParamServiceImpl.analyzePathParams）

- `@PathVariable Long id` 参数识别（MarkerAnnotationExpr 分支 — 无属性的标记注解）
- Long 类型推导为 ValueTypeEnum.INTEGER

### 5. Request Body 分析（RequestBodyServiceImpl）

- `@RequestBody CreateProductReq req` 参数识别
- CreateProductReq 的字段解析（productName, price）

### 6. Response Body 分析（ResponseBodyServiceImpl）

- `ProductResp` 返回类型解析（id, productName, price）

### 7. 校验注解分析（JsgBuilderServiceImpl.analyzeValid）

- `@NotBlank`（productName）
- `@NotNull`（price）

### 8. DSL 输出（EndpointDslServiceImpl.flushToEndpointDsl）

- 将 EndpointDTO 列表序列化为 JSON
- 按 `directCategory` 分类生成 .json 文件
- 文件名清理（sanitizeFileName）

### 9. 配置

- `flushTo: [DSL]`
- `dslDir: api-dsls`
- `globalUrlPrefix: ""`

### 10. 验证互斥性

- DSL 模式下**不生成** markdown 目录（api-docs 不存在）

## 未覆盖的分支

- `@RequestParam` Query Param 分析
- `@PutMapping` / `@DeleteMapping` HTTP 动词
- `void` 返回类型
- `@Deprecated` / `@since` 标签
- `globalUrlPrefix` 非空
- `singleEndpointPerMarkdown`
- `mvcHandlerQualifierWildcards` 过滤
- 嵌套 DTO 字段遍历
- 枚举字段类型分析
- MARKDOWN / YAPI / SHOWDOC 输出模式
- `hierarchicalCategories`
- `@PathVariable` 的 `name` / `value` 别名（SingleMemberAnnotationExpr / NormalAnnotationExpr 分支）
- `@Valid` 级联校验
- `@Size` / `@Min` / `@Max` / `@Length` / `@Pattern` 等校验注解
- `@JsonFormat` 格式标注
- `@JsonProperty.Access` 字段过滤
- `#API-DOC-IGNORE#` 字段忽略标记
- `dependencyDirsOrJavaFilePath` 外部依赖目录
