# pathvar-and-reqparam-aliases 集成测试

## 概述

验证 `@PathVariable` 和 `@RequestParam` 的多种注解形式（SingleMemberAnnotationExpr / NormalAnnotationExpr）以及多种参数类型推导。

## 覆盖的功能

### 1. @PathVariable SingleMemberAnnotationExpr（UrlParamServiceImpl.mergePathVariable）

- `@PathVariable("bookId")` → 参数名使用别名 `bookId`

### 2. @PathVariable NormalAnnotationExpr（UrlParamServiceImpl.mergePathVariable）

- `@PathVariable(name = "isbn")` → 参数名使用别名 `isbn`

### 3. @RequestParam SingleMemberAnnotationExpr（UrlParamServiceImpl.mergeReqParam）

- `@RequestParam("keyword")` → 参数名使用别名 `keyword`

### 4. induceToValueType 多分支（UrlParamServiceImpl）

- `int` → PrimitiveType.INT → ValueTypeEnum.INTEGER
- `boolean` → PrimitiveType.BOOLEAN → ValueTypeEnum.BOOLEAN
