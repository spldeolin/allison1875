# request-mapping-params 集成测试

## 概述

验证 `@RequestMapping(params = {...})` 条件参数追加到 URL 的逻辑。

## 覆盖的功能

### 1. Controller 级 @RequestMapping.params（RequestMappingServiceImpl）

- `@RequestMapping(value = "/api/config", params = {"module=system"})` → URL 追加 `?module=system`

### 2. Method 级 @GetMapping.params（RequestMappingServiceImpl）

- `@GetMapping(params = {"action=read"})` → URL 追加 `&action=read`

### 3. questionMark 逻辑（RequestMappingServiceImpl）

- 第一个 param 使用 `?`，后续使用 `&`
