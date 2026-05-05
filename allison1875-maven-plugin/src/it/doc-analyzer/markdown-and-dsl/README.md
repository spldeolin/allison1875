# doc-analyzer / markdown-and-dsl

## 本用例在验证什么

在 **`flushTo` 同时包含 MARKDOWN 与 DSL** 时，doc-analyzer 能否 **一次运行产出两套产物**：人类可读的 **`api-docs/*.md`** 与机器可读的 **`api-dsls/*.json`**，且在 **`globalUrlPrefix`** 非空时，两侧 URL 与端点集合 **保持一致、数量对齐**。

## 功能点与分支关注点（逐条）

- **双通道输出**：`api-docs` 与 `api-dsls` 目录均存在；Markdown 侧 **单文件** **`订单管理.md`**；DSL 侧所有 JSON 根数组中的 endpoint **条数之和为 5**（与下方 CRUD 条数一致）。
- **完整 CRUD 动词**：同一控制器上 **GET（列表）、GET（详情带路径变量）、POST、PUT、DELETE** 五类 handler 均被收录；DSL 与 Markdown 都应覆盖 **5** 个端点。
- **全局 URL 前缀**：配置 **`/v1`** 后，文档中出现的完整路径需带 **`/v1`** 前缀（如 **`GET /v1/api/orders`**、**`POST /v1/api/orders`**、**`PUT|DELETE /v1/api/orders/{orderId}`** 等），验证前缀与类级、方法级 `@RequestMapping` 的拼接。
- **PathVariable**：**`orderId`** 出现在文档的 Path Param 语境中（详情 / 更新 / 删除共用同一路径参数名）。
- **Query Param**：列表接口带 **可选 `status`** 时，列表类说明与 **第二行 Javadoc**（「支持按状态筛选」）仍进入 Markdown（多行方法说明）。
- **嵌套 DTO（请求）**：创建/更新请求中含 **订单行项** 时，内层字段 **`productId`、`quantity`** 等应展开到 Request Body 叙述中；扁平字段如 **`shippingAddress`、`buyerNote`** 亦需出现。
- **嵌套 DTO（响应）**：订单详情响应中含嵌套列表时，内层 **`productName`、`unitPrice`** 等应在 Response Body 侧可见。
- **DELETE 与无响应体**：删除接口为 **`void`** 时，属于「无返回体」分支；本用例的 **显式断言** 集中在五端点齐全、路径与嵌套字段上，不要求删除操作出现 Response Body 表格（与 `void` 语义一致）。

## 小结

本用例是 **「双模式 + 全局前缀 + 典型订单 CRUD + 嵌套行项 + Path/Query」** 的组合回归：既看 **Markdown 可读性**，也看 **DSL 端点计数与 JSON 合法性**，避免只开单模式时隐藏的双写缺陷。
