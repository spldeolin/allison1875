# doc-analyzer / glob-regex-branches

## 本用例在验证什么

在配置了 **`mvcHandlerQualifierWildcards`**（若干 **Glob 风格** 的「全限定名 + 方法名」模式）时，doc-analyzer 只对 **命中白名单的 handler** 出文档，并正确解析 **`*`、`?`、花括号 `{a,b}` 交替** 等通配语义，避免误包含或漏掉接口。

## 功能点与分支关注点（逐条）

- **多控制器、多 Markdown 文件**：工程内有 **订单 / 用户 / 商品** 三个 `@RestController`，配置未改为「单文件」时，应生成 **三个** 独立 `.md`（`订单管理.md`、`用户管理.md`、`商品管理.md`），文件名仍与各自类级主题一致。
- **`*` 任意段匹配**：模式 **`*Controller.list*`** 应同时命中三个类里的 **`list*`** 方法，故文档中需出现「列出订单 / 列出用户 / 列出商品」及对应 **`GET .../list`** 路径。
- **`{Order,User}` 类名交替 + 路径前缀**：第二条模式限定在 **`OrderController` 与 `UserController`** 上，且方法段使用 **`get?rder*`**（`?` 占 **单个** 字符、` *` 接后缀）：应 **命中** `getOrderDetail`（「获取订单详情」、**`GET /api/orders/detail`**），并 **不命中** `getUserDetail`（验证脚本要求全文 **不得** 出现「获取用户详情」——即 **单字符通配** 与 **`rder` 固定片段** 共同排除了另一条 detail 接口）。
- **多模式并集**：两条 wildcard **求并**；`ProductController` 无 `get?rder*` 匹配项，但仍有 **`listProducts`** 来自第一条，故 **商品** 侧仅有列表接口出现在文档中。
- **未匹配方法静默省略**：`UserController` 的 **`getUserDetail`** 不在并集内时，不应以任何形式写入聚合后的 Markdown 内容（负例断言）。

## 小结

本用例专门压 **`mvcHandlerQualifierWildcards` 的 Glob 语义**（星号、问号、花括号分支）与 **多模式 OR 过滤**，保证「只文档化想暴露的 handler」，而不是扫描到类就全文输出。
