# doc-analyzer / pathvar-and-reqparam-aliases

## 本用例在验证什么

检查 doc-analyzer 能否从 **`@PathVariable` / `@RequestParam` 的各类注解写法** 中读出 **对外参数名（别名）**，并正确拼 **Path / Query 两段表格**；同时覆盖 **基本类型 `boolean`** 在 Query 侧的类型展示与 **必填** 语义（与「仅按 Java 形参名」文档化区分）。

## 功能点与分支关注点（逐条）

- **`@PathVariable` 仅写字符串成员**：形参名与路径占位符不一致时，文档中的 Path 名应取 **`"bookId"`**（**`GET /api/books/{bookId}`**）。
- **`@PathVariable(name = "...")`**：使用显式 **`name`** 时，路径变量名取 **`isbn`**（**`GET /api/books/by-isbn/{isbn}`**），而非 Java 参数名。
- **`@RequestParam("keyword")` 等单成员形式**：Query 表中出现 **`keyword`**；**`required=true`** 时在文档中体现为 **「是」**（必填）。
- **`@RequestParam(value = "page_no", …)`**：对外名 **`page_no`** 与 Javadoc「页码」进入 Query 表（**`value` 与形参名不同** 的分支）。
- **基本类型 `boolean`**：Query 参数 **`active`** 推导为 **Boolean** 语义列，并带说明「是否启用」。
- **产物**：单文件 **`图书管理.md`**；三个 GET handler 的标题（SingleMember / name 别名 / 搜索）均出现。

## 小结

本用例把 **路径变量与查询参数的「注解别名」** 与 **`boolean` Query** 绑在同一控制器上，避免只覆盖默认形参名或只覆盖一种注解语法。
