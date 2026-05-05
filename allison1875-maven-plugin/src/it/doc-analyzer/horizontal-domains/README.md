# doc-analyzer / horizontal-domains

## 本用例在验证什么

在 **`.allison1875.yml` 里声明多个「水平 domain」**（本例 **user** 与 **order**，各自一套包名前缀）时，通过 Maven 参数 **`-Ddomain=user`** 选中当前运行所用的 domain；并观察 **单模块、同一源码根** 下，文档生成与 **包级 domain 配置** 如何协同。

## 功能点与分支关注点（逐条）

- **`-Ddomain` 生效**：构建日志中需出现 **`domain=user`**，表明 invoker 传入的领域选择参数被插件侧识别；配置序列化结果中仍应能同时看到 **`user` 与 `order`** 两个 domain 块（多 domain 配置未被裁掉）。
- **双领域包结构**：`com.example.user.*` 与 `com.example.order.*` 并列存在，用于模拟「一个 Maven 模块里两套业务分包」的常见布局。
- **Markdown 数量与命名**：生成 **两个** 文档文件 **`用户管理.md`**、**`订单管理.md`**，分别对应两个控制器的类级主题。
- **选中 domain 的接口与 DTO**：**user** 侧「创建用户」、**`POST /api/users`**，以及请求体字段 **`username`** 与中文说明「用户名」等应写入文档。
- **单模块水平划分的已知行为（重要）**：注释与断言一致——在 **单源码根** 场景下，工具会 **遍历该根下全部控制器**，**不会**仅按当前 domain 的 `controllerPackage` 做硬过滤；因此即使用 **`-Ddomain=user`** 运行，**order** 的「创建订单」、**`POST /api/orders`**、**`productId` / 「商品ID」** 等仍会出现在输出里；本用例把这一点当作 **预期行为** 写入断言，避免误报为回归。
- **order 领域 DTO 仍被解析**：订单请求/响应相关字段与 Javadoc 出现在合并后的 Markdown 内容中。

## 小结

本用例验证 **多 domain 配置 + `-Ddomain` 选择** 的接线是否正确，并明确文档化 **「单模块 + 水平 domain」时控制器检测范围偏宽** 的产品行为，防止与「只扫当前 domain 包」的直觉假设混淆。
