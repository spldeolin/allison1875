# controller-with-response-body 集成测试

## 概述

验证 `@Controller` + `@ResponseBody`（非 `@RestController`）场景下的 Response Body 分析。

## 覆盖的功能

### 1. @Controller 检测（MvcHandlerDetectorServiceImpl.isNotController）

- 使用 `@Controller` 而非 `@RestController`，验证 Spring `@Controller` 注解也能被检测

### 2. @ResponseBody 方法级注解（ResponseBodyServiceImpl）

- handler 方法标注 `@ResponseBody` → `isNotResponseBody` 返回 false
- 验证 `isNotRestController` 为 true 但 `isNotResponseBody` 为 false 的组合分支

### 3. Endpoint 基本信息

- 生成 1 个 md 文件：`报表管理.md`
- handler 1: `查询报表（有@ResponseBody）` → `GET /api/reports`
- handler 2: `创建报表（有@ResponseBody）` → `POST /api/reports`

### 4. Request Body 字段

- reportType（报表类型）

### 5. Response Body 字段

- reportName（报表名称）、id
- 两个 handler 都有 `@ResponseBody`，因此 Markdown 中应包含 2 个 Response Body 区域
