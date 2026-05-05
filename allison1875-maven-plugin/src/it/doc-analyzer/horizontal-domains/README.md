# horizontal-domains 集成测试

## 概述

验证水平域划分（单模块内多个 domain 按 package 区分）场景下的 controller 检测和 DTO 解析。

## 覆盖的功能

### 1. 多 domain 水平划分配置

- `domains` 列表包含 `user` 和 `order` 两个 domain
- 每个 domain 的 `controllerPackage`、`reqDTOPackage`、`respDTOPackage` 指向不同的 package
- user domain: `com.example.user.controller` / `com.example.user.dto.req` / `com.example.user.dto.resp`
- order domain: `com.example.order.controller` / `com.example.order.dto.req` / `com.example.order.dto.resp`

### 2. `-Ddomain` 选择与 build.log 验证

- 通过 `invoker.properties` 传递 `-Ddomain=user`，选择 user 领域运行
- build.log 中确认 `domain=user` 被正确选择
- 配置中同时包含两个 domain name（`"name" : "user"` 和 `"name" : "order"`）

### 3. 单模块场景下的 controller 检测

- user 和 order 的 controller 在同一个 sourceRoot 下
- 在单模块水平拆分场景中，**所有 controller 都会被检测到**（不限于当前 domain 的 controllerPackage）
- UserController 和 OrderController 都出现在输出的 markdown 中

### 4. Markdown 输出

- 生成 2 个 md 文件：`用户管理.md`、`订单管理.md`

### 5. DTO 字段跨 domain 解析

- user domain: `CreateUserReq`（username, email）→ `UserResp`（id, username, email）
- order domain: `CreateOrderReq`（productId, quantity）→ `OrderResp`（orderId, productId, quantity）
- 各 domain 的 DTO 字段 Javadoc 正确提取（用户名、商品ID 等中文注释）

### 6. 配置

- `domains`: user, order（水平划分，无 `dtoModule` / `controllerModule`）
- `flushTo: [MARKDOWN]`
- `markdownDir: api-docs`
