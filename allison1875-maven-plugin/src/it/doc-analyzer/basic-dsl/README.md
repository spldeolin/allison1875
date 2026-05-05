# doc-analyzer / basic-dsl

## 本用例在验证什么

在 **仅开启 DSL（JSON）输出** 时，doc-analyzer 能否为典型 REST 控制器产出 **结构正确、可被下游消费的接口 DSL 文件**，且 **不生成 Markdown 目录**（两种输出模式互斥的基本约定）。

## 功能点与分支关注点（逐条）

- **输出目录与格式**：在配置的 `api-dsls` 下生成 **合法 JSON**；根节点为 **数组**，每个元素表示一个 HTTP 接口端点。
- **「只出 DSL」时的副作用**：工作目录下 **不应出现** `api-docs`，避免在纯 DSL 场景误写 Markdown。
- **按业务分类落盘**：同一控制器 Javadoc 所代表的模块名（本例为「商品管理」）对应 **唯一一个** JSON 文件名（`商品管理.json`），用于多控制器/多模块时的文件分桶策略。
- **类级 + 方法级路径拼接**：`@RequestMapping("/api/products")` 与 `@GetMapping("/{id}")` / `@PostMapping` 合并后，DSL 中的 URL 列表需能体现 **`/api/products` 前缀**；GET 场景带路径变量模板。
- **HTTP 动词覆盖**：同一控制器内 **GET 与 POST** 各一条，DSL 中 `httpMethod` 分别为 `get`、`post`，且数组长度为 **2**。
- **GET 端点**：方法 Javadoc「根据ID查询商品」进入描述；**单个路径参数** `id`（来自 `@PathVariable`）被结构化到 `pathParams`。
- **POST 端点**：方法 Javadoc「创建商品」进入描述；**请求体**绑定到请求 DTO 的 **全限定类名**；**响应体**绑定到响应 DTO 的 **全限定类名**；POST 还需带有 **请求体的 JSON Schema** 字段（供工具链或文档生成器消费）。

## 小结

本用例是 doc-analyzer **DSL 输出的最小闭环**：路由合并、两种动词、PathVariable、RequestBody/返回类型在 JSON 中的字段落位，以及 **DSL-only 不产 Markdown** 的配置行为。
