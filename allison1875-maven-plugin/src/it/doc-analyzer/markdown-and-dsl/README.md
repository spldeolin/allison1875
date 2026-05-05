# markdown-and-dsl 集成测试

## 概述

验证 doc-analyzer **同时以 MARKDOWN 和 DSL 双模式输出**的工作流程，覆盖完整 CRUD 场景、嵌套
DTO、PathVariable、globalUrlPrefix 等多个进阶特性。

## 覆盖的功能

### 1. MVC Handler 检测（MvcHandlerDetectorServiceImpl）

- 扫描 `@RestController` 类（OrderController）
- 识别全部 5 种 HTTP 动词映射：`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`
- 识别同一 controller 下的多个 handler 方法（5 个）

### 2. MVC Handler 分析（MvcHandlerAnalyzerServiceImpl）

- 从 controller Javadoc 提取 `directCategory`（"订单管理"）
- 从 handler Javadoc 提取 `descriptionLines`（"查询订单列表"、"根据ID查询订单详情"、"创建订单"、"更新订单"、"删除订单"）
- 多行描述：listOrders 的 Javadoc 包含第2行描述"支持按状态筛选"

### 3. RequestMapping 分析（RequestMappingServiceImpl）

- controller 级 `@RequestMapping("/api/orders")` 与 handler 级各 `@XxxMapping` 的 URL 合并
- `@GetMapping("/{orderId}")` / `@PutMapping("/{orderId}")` / `@DeleteMapping("/{orderId}")` 路径模板
- **globalUrlPrefix = "/v1"**：验证全局 URL 前缀拼接逻辑（以 `/` 开头的前缀分支）
- HTTP 动词推导：GET、POST、PUT、DELETE

### 4. Path Param 分析（UrlParamServiceImpl.analyzePathParams）

- `@PathVariable Long orderId` 参数识别
- 同一 controller 中多个 handler 共用 PathVariable 名称

### 5. Query Param 分析（UrlParamServiceImpl.analyzeQueryParams）

- `@RequestParam(required = false) String status` 参数识别

### 6. Request Body 分析（RequestBodyServiceImpl）

- `@RequestBody CreateOrderReq req`、`@RequestBody UpdateOrderReq req` 两种不同 RequestBody
- 嵌套 DTO：CreateOrderReq 中包含 `List<OrderItemReq> items`
- 嵌套 DTO 的字段解析（productId, quantity）

### 7. Response Body 分析（ResponseBodyServiceImpl）

- `OrderDetailResp` 返回类型（含嵌套 `List<OrderItemResp> items`）
- `List<OrderDetailResp>` 泛型列表返回类型
- **`void` 返回类型**（deleteOrder 方法 → 不生成 Response Body 区域）

### 8. 字段分析（FieldServiceImpl.analyzeFieldVars）

- 嵌套 DTO 的字段注释提取（OrderItemReq、OrderItemResp）

### 9. 校验注解分析（JsgBuilderServiceImpl.analyzeValid）

- `@NotBlank`（shippingAddress）
- `@NotEmpty`（items 列表非空）
- `@Valid`（级联校验标记）
- `@NotNull` + `@Min(1)`（OrderItemReq.quantity → 最小值校验分支）

### 10. Markdown 输出（MarkdownServiceImpl.flushToMarkdown）

- 5 个 endpoint 全部输出到 markdown
- Path Param 表格
- Query Param 表格
- Request Body 表格（含嵌套字段）
- Response Body 表格（含嵌套字段）

### 11. DSL 输出（EndpointDslServiceImpl.flushToEndpointDsl）

- 同时验证 JSON 输出的正确性
- JSON 中 endpoint 总数 = 5

### 12. 配置

- `flushTo: [MARKDOWN, DSL]`（双模式同时输出）
- `markdownDir: api-docs`
- `dslDir: api-dsls`
- `globalUrlPrefix: /v1`（非空前缀）

## 未覆盖的分支

- `@Deprecated` / `@since` 标签 → 兼容性说明区域
- `singleEndpointPerMarkdown = true` 分支
- `mvcHandlerQualifierWildcards` 过滤
- 枚举字段类型分析（EnumServiceImpl.analyzeEnumConstants）
- `hierarchicalCategories`（package-info.java 层级分类）
- `@RequestParam` 的 `name` / `value` 别名
- `@RequestParam` 的 `defaultValue`
- `@PathVariable` 的 `name` / `value` 别名
- `@Size` / `@Max` / `@Length` / `@Pattern` / `@Digits` 等更多校验注解
- `@JsonFormat` 格式标注
- `@JsonProperty.Access` READ_ONLY / WRITE_ONLY 字段过滤
- `#API-DOC-IGNORE#` 字段忽略标记
- `dependencyDirsOrJavaFilePath` 外部依赖目录
- YAPI / SHOWDOC 输出模式
- globalUrlPrefix 不以 `/` 开头的分支
