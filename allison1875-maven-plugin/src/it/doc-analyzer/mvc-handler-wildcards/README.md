# mvc-handler-wildcards 集成测试

## 概述

验证 doc-analyzer 的 `mvcHandlerQualifierWildcards` 过滤功能，以及 `convertGlobToRegex` 方法中 `*` 通配符和 `{...}`
花括号分支。

## 覆盖的功能

### 1. mvcHandlerQualifierWildcards 过滤（MvcHandlerDetectorServiceImpl）

- 配置 `mvcHandlerQualifierWildcards: ["*.list*", "*.get{ById,Detail}"]`
- Controller 有 4 个 handler：listAnimals、getById、createAnimal、deleteAnimal
- 只有 listAnimals 和 getById 匹配通配符，出现在文档中
- createAnimal 和 deleteAnimal 不匹配，被过滤

### 2. convertGlobToRegex 分支（MvcHandlerDetectorServiceImpl）

- `*` 通配符 → `.*` 正则
- `{ById,Detail}` 花括号 → `(ById|Detail)` 正则（inCurlies 和 `,` 分支）
