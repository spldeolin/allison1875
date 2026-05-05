# deprecated-and-since 集成测试

## 概述

验证 doc-analyzer 对 `@deprecated` 和 `@since` Javadoc 标签的处理，包括 handler 级和字段级的兼容性说明。

## 覆盖的功能

### 1. Handler 级 @since 标签（MvcHandlerAnalyzerServiceImpl.analyzeSinceVersion）

- handler 方法声明 `@since v1.0.0`
- handler 方法未声明 `@since` 时回退到 controller 级 `@since v2.0.0`

### 2. Handler 级 @deprecated 标签（MvcHandlerAnalyzerServiceImpl.analyzeDeprecatedDescription）

- handler 方法声明 `@deprecated 请使用 getLatest 替代，本接口将在 v4.0 移除`
- Markdown 中输出"兼容性说明"区域

### 3. 字段级 @since 标签（FieldServiceImpl.analyzeSinceVersion）

- CreateNoticeReq.priority 字段的 `@since v3.0.0`

### 4. 字段级 @deprecated 标签（FieldServiceImpl.analyzeDeprecatedDescription）

- CreateNoticeReq.category 字段的 `@deprecated 请使用 tags 字段替代`

### 5. Markdown 兼容性说明区域（MarkdownServiceImpl.generateEndpointDoc）

- `sinceVersion != null` 时生成"本接口加入版本：xxx"
- `deprecatedDescription != null` 时生成"本接口已过时，不建议调用"
