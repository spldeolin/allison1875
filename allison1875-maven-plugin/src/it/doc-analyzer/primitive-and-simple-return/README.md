# primitive-and-simple-return 集成测试

## 概述

验证 handler 返回原始类型（`int`/`boolean`/`String`）时的 Response Body 分析，特别是 `rootJsonSchema.isValueTypeSchema()`
分支。

## 覆盖的功能

### 1. ResponseBodyServiceImpl.getConcernedResponseBodyType

- 返回 `String` → resolve 为 ResolvedReferenceType
- 返回 `int` → resolve 为 ResolvedPrimitiveType → boxTypeQName

### 2. MarkdownServiceImpl.generateReqOrRespDoc - isValueTypeSchema 分支

- 根节点为 ValueType 时，直接输出类型，不遍历子字段
- `@return` Javadoc 标签的描述被提取到注释列
