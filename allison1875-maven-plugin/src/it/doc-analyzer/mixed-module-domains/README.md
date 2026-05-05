# doc-analyzer / mixed-module-domains

## 本用例在验证什么

在 **多 domain** 且每个 domain 采用 **「Web 与 API 分包/分模块」式垂直划分**（`dtoModule` / `wholeDTOModule` 等指向子目录模块名）时，用 **`-Ddomain=order`** 选中 **order** 领域后，插件能否把 **order 的 controller（根模块源码）** 与 **order-api 子树里的 DTO** 拼成同一套 AST 分析范围，并产出正确 Markdown。

## 功能点与分支关注点（逐条）

- **`-Ddomain` 与日志**：构建日志需出现 **`domain=order`**，表示 invoker 传入的领域选择与插件一致。
- **`dtoModule` 并入源码根**：日志或运行输出中应能体现 **`order-api`** 被纳入 **全部编译/分析用 source root**（与「DTO 写在独立子目录、通过 `build-helper-maven-plugin` 额外挂上 `user-api`/`order-api` 源码」的假工程布局一致）。
- **跨目录类型解析**：`OrderController` 引用 **`order-api`** 中的 **`PlaceOrderReq` / `OrderResp`** 时，文档中需出现 **`productId`、`quantity`、`shippingAddress`** 等请求字段，以及响应 **`orderNo`**，证明 **controller 所在根与 DTO 子模块** 能被联合解析。
- **假工程结构**：根 `pom` 下 **`src/main/java`** 同时放 **user** 与 **order** 两套 `controller` 包；**`user-api/`、`order-api/`** 下各放对应 domain 的 DTO，模拟多模块单体仓库。
- **双 Markdown 与扫描范围（与水平域 IT 类似）**：即使指定 **`-Ddomain=order`**，仍生成 **两个** `.md`（**用户管理**、**订单管理**各一份）；验证脚本将其解释为：**当前聚合源码根下两个 `@RestController` 仍都会被扫到**——与「仅按当前 domain 的 `controllerPackage` 硬过滤」的直觉不同，本用例把该行为写进预期。
- **order 侧接口语义**：**「下单」**、**`POST /api/orders`** 必须出现在合并后的 Markdown 内容中。

## 小结

本用例验证 **混合配置（多 domain + `dtoModule` 垂直拆分 + `-Ddomain`）** 下 **order-api 被吃进分析图** 且 **order 的 DTO 字段能进文档**；同时再次 **钉死「多控制器同根仍全量出文档」** 的产品行为，避免与 domain 选择语义混淆。
