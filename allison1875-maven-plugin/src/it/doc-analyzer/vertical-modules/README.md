# doc-analyzer / vertical-modules

## 本用例在验证什么

在 **垂直拆分** 布局下：**`@RestController` 位于主模块 `src/main/java`**，而 **请求/响应 DTO 位于子目录模块 `dto-api`**（通过 **`dtoModule` / `wholeDTOModule` / `enumModule`** 指向该模块名）时，doc-analyzer 能否把 **多个 source root** 并入同一座 AST 森林，从而 **跨目录解析 DTO 字段与 Javadoc** 并写入 Markdown。

## 功能点与分支关注点（逐条）

- **多源码根可见**：构建日志中应出现 **`dto-api`** 路径，表示 DTO 子树已被纳入 **全部 source root** 列表（与根 `pom` 里 **`build-helper-maven-plugin` 追加 `dto-api/src/main/java`** 的布局一致）。
- **Controller → DTO 跨根引用**：主模块里的 **`ProductController`** 使用 **`dto-api`** 中的 **`CreateProductReq` / `ProductResp`** 时，文档中需出现 **`productName`、`price`、`description`** 及响应 **`id`** 等字段说明。
- **接口与产物**：**`POST /api/products`**（「创建商品」）；生成 **`商品管理.md`**。
- **与「水平多 domain」区别**：本例为 **单 domain（shop）** + **DTO 外置子模块**，不依赖 **`-Ddomain`**；重点在 **垂直 `dtoModule` 接线** 与 **跨模块 AST**。

## 小结

本用例验证 **「控制器与 DTO 分仓」** 时文档仍完整；与 **`mixed-module-domains`** 相比，本仓库布局是 **controller 在根、`dto-api` 外挂**，更贴近「Web 与 API 分包编译」的常见 Maven 技巧。
