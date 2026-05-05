# doc-analyzer / hierarchical-categories

## 本用例在验证什么

当控制器所在包带有 **`package-info.java`**，且其 Javadoc 表示 **上一级业务分类**（本例「后台管理模块」）时，doc-analyzer 应按 **「分类目录 / 接口文档文件」** 的层级落盘 Markdown，而不是把 `.md` 全堆在 `api-docs` 根目录。

## 功能点与分支关注点（逐条）

- **目录镜像分类**：在 `markdownDir`（`api-docs`）下先建 **子目录**，目录名来自包级说明（**`后台管理模块`**）；其下再放具体接口文档文件。
- **文档文件名与控制器主题**：子目录内的单个文件名为 **`系统设置.md`**，与控制器类 Javadoc 主题一致；全路径为 **`api-docs/后台管理模块/系统设置.md`**。
- **根目录不放散文件**：`api-docs` **根下不得直接出现** `.md` 文件（断言层级模式下「只进子文件夹」的约定）。
- **接口内容仍完整**：文档中包含方法说明「查询系统设置」、**`GET /api/admin/settings`**，以及响应 DTO 字段 **`key` / `value`** 与注释（「设置键」「设置值」）。
- **包布局含义**：控制器放在 **`com.example.controller.admin`** 子包，与 `package-info.java` 同包，用于模拟「管理后台子域」一类包结构。

## 小结

本用例验证 **包级 Javadoc → 分类子目录** 与 **类级主题 → 文件名** 的组合策略，并防止层级开启时仍向 `api-docs` 根写入 Markdown。
