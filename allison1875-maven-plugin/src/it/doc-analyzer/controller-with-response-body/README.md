# doc-analyzer / controller-with-response-body

## 本用例在验证什么

在类上使用 **`@Controller`（而非 `@RestController`）** 时，只要 handler 上显式加了 **`@ResponseBody`**，doc-analyzer 仍应把这些方法当作 **会写出 JSON 的接口** 来解析，并生成与普通 REST 类似的 Markdown（含 Request/Response Body 区块）。

## 功能点与分支关注点（逐条）

- **控制器形态分支**：识别 **`@Controller` + 类级 `@RequestMapping`** 这一组合，避免「只有 `@RestController` 才算 API」的单一假设。
- **按方法声明响应体**：每个 handler 单独带 **`@ResponseBody`**，用于覆盖「非类级 `@RestController` 等价物、逐方法标记写 body」的路径；两条接口都应有 **Response Body** 小节（本例共 **2** 处）。
- **GET 仍可有 Response Body**：无 `@RequestBody` 的 GET 返回 `ReportResp`，文档中应出现响应字段（如 `reportName`、`id`）及 Javadoc（如「报表名称」）。
- **POST + Request Body**：带 `@RequestBody` 的创建接口应出现请求字段（如 `reportType`）及注释（如「报表类型」）。
- **路由与动词**：`GET /api/reports` 与 `POST /api/reports` 均写入文档；方法 Javadoc 标题（含括号内说明）用于区分两条 handler 的语义。
- **产物文件**：仅 **一个** Markdown 文件，名称与类级主题一致（「报表管理」→ `报表管理.md`）。

## 小结

本用例专门验证 **「老式 `@Controller` + 方法级 `@ResponseBody`」** 与 **`@RestController` 默认写 body** 在文档侧是否等价对待，避免漏扫或漏掉 Response Body 章节。
