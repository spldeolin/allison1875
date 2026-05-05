# doc-analyzer / basic-markdown

## 本用例在验证什么

在 **仅导出 Markdown** 时，doc-analyzer 能否把「带 GET（查询参数 + 列表返回）与 POST（JSON 请求体）」的常见用户接口，整理成 **可读、结构固定的 `.md` 文档**，并正确带出 **字段注释、基础校验、可选 Query 参数** 等信息。

## 功能点与分支关注点（逐条）

- **输出形态**：在 `api-docs` 下生成 Markdown；本例控制器对应 **单个** 文件，文件名取自类级 Javadoc 首行主题（「用户管理」→ `用户管理.md`）。
- **双端点 GET + POST**：文档中同时出现 **GET**、**POST** 与 **`/api/users`** 路径；分别对应「查询用户列表」与「创建用户」两条 handler 的语义。
- **多行 Javadoc 描述**：主标题行与后续说明行（如「根据关键字搜索用户」）都应进入文档，用于验证 **方法说明不止一行** 时的合并/展示。
- **Query Param 与必填语义**：`@RequestParam(required = false)` 的查询参数名（`keyword`）出现在 Query Param 区块；**非必填** 在文档中需有明确体现（本例用中文「否」等表述与 `required=false` 对应）。
- **Request Body**：POST 的 JSON 请求体区块存在；请求 DTO 字段名（`username`、`age`、`email`）及 Javadoc（如「用户名」）写入文档；**`@NotBlank` / `@NotNull`** 等常见约束有对应中文说明。
- **Response Body 与集合返回**：GET 返回 **`List<UserResp>`** 时，文档需能表达 **列表/数组语义**（本例断言中出现「Object Array」类表述，覆盖「元素类型为对象的列表」这一展示分支）；列表元素类型上的字段（如 `id`）及 Javadoc（如「用户ID」）仍应出现。
- **Markdown 版面骨架**：包含约定的 **小节标题**（如 URL、Query Param、Request Body、Response Body 的 `###` 标题）以及 **分隔线**（`---`），保证多接口在同文件内排版一致。

## 小结

本用例是 doc-analyzer **Markdown 模式的最小闭环**：路由与动词、可选 Query、POST 请求体与校验、**List 包裹的响应 DTO** 的文档化，以及固定章节结构。
