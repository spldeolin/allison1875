# doc-analyzer / single-endpoint-markdown

## 本用例在验证什么

在 **`singleEndpointPerMarkdown: true`** 时，每个 HTTP 端点应生成 **独立 Markdown 文件**；本用例同时叠 **`globalUrlPrefix` 无前导 `/`**、`@RequestParam` **别名与默认值**、请求 DTO 上 **`#API-DOC-IGNORE#`** 忽略规则，以及 **`void`** 返回 **无 Response Body** 的分支。

## 功能点与分支关注点（逐条）

- **一接口一文件**：两个 handler → **两个** `.md`；分别对应「搜索物品」「提交物品」两条链路（文件名由工具按端点分类规则生成，断言以 **文件数量与内容** 为主）。
- **`globalUrlPrefix: api`**：配置不以 **`/`** 开头时，对外 URL 仍应规范为 **`/api/...`**（如 **`GET /api/items/search`**、**`POST /api/items`**）。
- **Query 别名与默认值**：**`@RequestParam(name = "q")`** 在表格中以 **`q`** 出现；**`@RequestParam(value = "page", defaultValue = "1")`** 需体现参数名 **`page`** 与默认值 **`1`**。
- **字段忽略**：**`SearchReq`** 中带 **`#API-DOC-IGNORE#`** 的 **`internalTraceId`** 不得出现在任何生成文档中；**`keyword`** 仍应出现。
- **`void` 与 Response Body**：含「提交物品」的那份文档 **不得** 含 **Response Body** 小节；带 **`ItemResp`** 的搜索接口仍可有响应体描述。
- **控制器路径**：类级 **`/items`** 与全局前缀拼接后落在 **`/api/items`** 之下。

## 小结

本用例是 **「单端点 Markdown 模式」** 的集成样例：同时验证 **文件拆分、URL 规范化、Query 高级属性、文档忽略标记、void 无 body** 五条独立规则能在一组接口里共存。
