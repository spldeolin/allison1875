# dependency-dirs 集成测试

## 概述

验证 `dependencyDirsOrJavaFilePath` 配置项，指向外部 Java 文件目录用于字段 Javadoc 提取。

## 覆盖的功能

### 1. FieldServiceImpl.buildAnalysisScope - 外部目录扫描

- 配置 `dependencyDirsOrJavaFilePath: [external-dto]`
- `external-dto` 目录中的 Java 文件被纳入分析范围
- `BaseInfo.java` 中的字段注释（外部创建者名称、外部创建时间）被提取

### 2. Endpoint 基本信息

- 生成 1 个 md 文件：`审计管理.md`
- handler: `查询审计信息`
- URL: `GET /api/audit`

### 3. Response Body 字段

- auditId（审计ID）、actionType（操作类型）

### 4. GET 无 Request Body 验证

- handler 为 GET 方法且无 `@RequestBody` 参数
- Markdown 中**不应包含** `Request Body` 区域
