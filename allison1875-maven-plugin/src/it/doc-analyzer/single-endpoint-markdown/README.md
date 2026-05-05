# single-endpoint-markdown 集成测试

## 概述

验证 doc-analyzer 在 `singleEndpointPerMarkdown = true` 模式下的工作流程，同时覆盖 globalUrlPrefix 不以 `/` 开头的分支、
`@RequestParam` 别名与默认值、`#API-DOC-IGNORE#` 字段忽略、`void` 返回类型等。

## 覆盖的功能

### 1. singleEndpointPerMarkdown = true（MarkdownServiceImpl.categorizeMarkdowns）

- 每个 endpoint 的 description 第一行作为独立 directCategory
- 2 个 handler 应生成 2 个独立的 .md 文件

### 2. globalUrlPrefix 不以 `/` 开头（RequestMappingServiceImpl）

- `globalUrlPrefix: api`（不以 `/` 开头）
- 自动补 `/` 前缀：`/api/items/search`

### 3. @RequestParam name / value 别名（UrlParamServiceImpl.mergeReqParam）

- `@RequestParam(name = "q")` → NormalAnnotationExpr 分支，参数名为 `q`
- `@RequestParam(value = "page", required = false, defaultValue = "1")` → NormalAnnotationExpr 分支

### 4. @RequestParam defaultValue

- `defaultValue = "1"` 出现在 Query Param 表格的"默认值"列

### 5. #API-DOC-IGNORE# 字段忽略（JsgBuilderServiceImpl.isIgnored）

- SearchReq.internalTraceId 字段注释包含 `#API-DOC-IGNORE#`
- 该字段不应出现在文档中

### 6. void 返回类型（ResponseBodyServiceImpl）

- submitItem 方法返回 `void`
- 不生成 Response Body 区域
