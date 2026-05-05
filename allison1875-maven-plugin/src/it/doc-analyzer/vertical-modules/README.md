# vertical-modules 集成测试

## 概述

验证垂直模块拆分场景（controller 在主模块，DTO 在子模块 dto-api）下，
AstForest 能跨多个 sourceRoot 解析 controller 和 DTO 的 AST。

## 覆盖的功能

### 1. dtoModule 配置与多 sourceRoot 解析

- controller 在主模块 `src/main/java`（`com.example.shop.controller`）
- DTO 在子模块 `dto-api/src/main/java`（`com.example.shop.dto.req` / `com.example.shop.dto.resp`）
- `dtoModule: dto-api` 配置使 `allSourceRoots` 包含 dto-api 的 sourceRoot
- build.log 中确认 `dto-api` 路径被纳入

### 2. 跨模块 controller → DTO 关联

- `ProductController`（主模块）引用 `CreateProductReq` 和 `ProductResp`（dto-api 模块）
- AST 跨 sourceRoot 解析时能正确找到 DTO 类的字段定义和 Javadoc

### 3. DTO 字段文档提取

- Request Body 字段：productName、price、description（来自 dto-api 模块的 `CreateProductReq`）
- Response Body 字段：id（来自 dto-api 模块的 `ProductResp`）

### 4. Markdown 输出

- 生成 `商品管理.md`
- endpoint: `POST /api/products`（创建商品）

### 5. 配置

- `domains`: shop（dtoModule: dto-api, wholeDTOModule: dto-api, enumModule: dto-api）
- `flushTo: [MARKDOWN]`
- `markdownDir: api-docs`
