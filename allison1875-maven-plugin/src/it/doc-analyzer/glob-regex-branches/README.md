# glob-regex-branches 集成测试

## 概述

验证 `mvcHandlerQualifierWildcards` 配置中 glob 模式到正则表达式的转换逻辑（`convertGlobToRegex`），
覆盖 `*`（任意字符匹配）、`?`（单字符匹配）、`{A,B}`（花括号交替匹配）三种通配符分支。

## 覆盖的功能

### 1. `*` 通配符匹配（convertGlobToRegex — star 分支）

- 模式 `*Controller.list*` 匹配所有 controller 的 `list*` 方法
- OrderController.listOrders、UserController.listUsers、ProductController.listProducts 均被匹配

### 2. `?` 单字符匹配（convertGlobToRegex — question-mark 分支）

- 模式 `com.example.controller.{Order,User}Controller.get?rder*` 中的 `?` 匹配任意单个字符
- `getOrderDetail`（`?` 匹配 `O`）被匹配
- `getUserDetail`（`get?rder*` 不匹配 `getUserDetail`）被正确排除

### 3. `{A,B}` 花括号交替匹配（convertGlobToRegex — curly-brace 分支）

- 模式中的 `{Order,User}` 转换为正则的 `(Order|User)` 交替组
- 限定 glob 仅匹配 OrderController 和 UserController，不匹配 ProductController 的 get 方法

### 4. 组合模式过滤

- 两条 wildcard 模式的并集：最终输出 3 个 controller 对应的 md 文件（商品管理、用户管理、订单管理）
- 但只包含被 glob 匹配到的 handler，未匹配的 handler（如 `getUserDetail`）不出现在文档中

### 5. 配置

- `mvcHandlerQualifierWildcards`:
    - `*Controller.list*`
    - `com.example.controller.{Order,User}Controller.get?rder*`
- `flushTo: [MARKDOWN]`
- `markdownDir: api-docs`
