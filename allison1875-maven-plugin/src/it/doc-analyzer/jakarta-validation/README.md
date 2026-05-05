# jakarta-validation 集成测试

## 概述

验证 `jakarta.validation.constraints.*` 注解（Spring Boot 3+ 场景）的校验解析。

## 覆盖的功能

### 1. jakarta.validation.constraints.NotBlank（JsgBuilderServiceImpl.analyzeValid）

### 2. jakarta.validation.constraints.Size（JsgBuilderServiceImpl.analyzeValid）

### 3. jakarta.validation.constraints.Min / Max（JsgBuilderServiceImpl.analyzeValid）

### 4. jakarta.validation.constraints.Past（JsgBuilderServiceImpl.analyzeValid）

### 5. enableJavaxMoveToJakarta = true 配置

- 验证 Jakarta 命名空间的校验注解能够被正确识别

### 6. Endpoint 基本信息

- 生成 1 个 md 文件：`会员管理.md`
- handler: `创建会员`
- URL: `POST /api/members`

### 7. Request Body 字段

- memberName（会员名称）：`@NotBlank` + `@Size(min=2, max=50)`
- birthday（出生日期）：`@Past`
- 级别字段：`@NotNull` + `@Min(1)` + `@Max(5)`

### 8. Response Body 字段

- id、level（会员等级）
