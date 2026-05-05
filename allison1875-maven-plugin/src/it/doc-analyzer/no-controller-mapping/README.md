# no-controller-mapping 集成测试

## 概述

验证 Controller 无类级 `@RequestMapping` 时的 URL 合并逻辑。

## 覆盖的功能

### 1. RequestMappingServiceImpl - controllerRequestMapping 为 null

- `findRequestMappingAnnoOrElseNull` 返回 null → `findValueFromAnno` 返回空数组
- `combineUrl` 走 `ArrayUtils.isEmpty(cPaths)` 分支

### 2. combineVerb - controller 无 verb

- `findVerbFromAnno` 返回空数组 → `combineVerb` 只使用 method 级 verb
