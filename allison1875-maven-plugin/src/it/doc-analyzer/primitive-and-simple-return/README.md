# doc-analyzer / primitive-and-simple-return

## 本用例在验证什么

当接口直接返回 **`String`、`int`、`boolean` 等标量**（而非 DTO / `void`）时，doc-analyzer 能否在 **Response Body** 中按 **值类型** 展示，并把方法 Javadoc 里的 **`@return`** 说明写进文档；且 **无 `@RequestBody` 的 GET** 不应出现 Request Body 章节。

## 功能点与分支关注点（逐条）

- **三种标量返回**：**`String`**（健康状态文案）、**`int`**（在线用户数）、**`boolean`**（是否正常）分别对应三条 **GET**；URL 为 **`/api/health/status|count|alive`**。
- **值类型行而非字段展开**：文档表格中应出现 **String / Integer / Boolean** 一类 **单行值类型** 表述（不强行虚构子字段），与「返回 DTO 并展开属性」路径区分。
- **`@return` 文案**：每条接口的 **`@return`** 描述（健康状态、在线用户数、是否正常）需出现在响应说明相关列中。
- **无请求体**：全文 **不得** 出现 **Request Body** 字样（三条均为纯 GET）。
- **产物**：单文件 **`健康检查.md`**。

## 小结

本用例验证 **「最简 JSON 响应」**（纯字符串/数字/布尔）在 Markdown 中的落点，防止工具只支持对象图而漏掉常见探活接口。
