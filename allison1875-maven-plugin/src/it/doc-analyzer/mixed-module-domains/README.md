# mixed-module-domains 集成测试

## 概述

验证混合划分场景（多 domain + 垂直拆分）下，`-Ddomain` 选择特定域、跨模块解析 controller 和 DTO 的能力。

## 覆盖的功能

### 1. 多 domain 配置与 `-Ddomain` 选择

- `domains` 列表包含 `user` 和 `order` 两个 domain
- 通过 `invoker.properties` 传递 `-Ddomain=order`，仅运行 order 领域
- build.log 中确认 `domain=order` 被正确选择

### 2. 垂直拆分 — dtoModule 跨模块解析

- order domain 的 controller 在主模块 `src/main/java`（`com.example.order.controller`）
- order domain 的 DTO 在子模块 `order-api`（`com.example.order.dto.req` / `com.example.order.dto.resp`）
- `dtoModule: order-api` 配置使 `allSourceRoots` 包含 order-api 的 sourceRoot
- build.log 中确认 `order-api` 路径被纳入

### 3. 跨模块 DTO 字段解析

- `CreateOrderReq`（order-api 模块）的字段：productId、shippingAddress、quantity
- `OrderResp`（order-api 模块）的字段：orderNo
- controller 引用这些 DTO 类型时，AST 能跨 sourceRoot 正确解析字段和 Javadoc

### 4. 单 sourceRoot 下多 controller 检测

- order 和 user 的 controller 都在主模块的 `src/main/java` 下
- 生成 2 个 md 文件（两个 controller 都在同一个 sourceRoot 中被检测到）

### 5. 配置

- `domains`: user（dtoModule: user-api）, order（dtoModule: order-api）
- `-Ddomain=order`（通过 invoker.properties 传递）
- `flushTo: [MARKDOWN]`
- `markdownDir: api-docs`
