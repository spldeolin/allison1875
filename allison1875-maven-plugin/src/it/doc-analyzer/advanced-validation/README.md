# advanced-validation 集成测试

## 概述

验证 doc-analyzer 对更多校验注解的处理，包括 `@DecimalMin`/`@DecimalMax`、`@Digits`、`@Future`、`@Positive`、`@Length`
、以及集合泛型参数上的校验注解。

## 覆盖的功能

### 1. @DecimalMin / @DecimalMax（JsgBuilderServiceImpl.analyzeValid）

- `@DecimalMin("0.01")` → ValidatorTypeEnum.MIN_NUMBER
- `@DecimalMax("999999.99")` → ValidatorTypeEnum.MAX_NUMBER

### 2. @Digits（JsgBuilderServiceImpl.analyzeValid）

- `@Digits(integer = 6, fraction = 2)` → MAX_INTEGRAL_DIGITS / MAX_FRACTIONAL_DIGITS

### 3. @Future（JsgBuilderServiceImpl.analyzeValid）

- `@Future` → ValidatorTypeEnum.FUTURE

### 4. @Positive（JsgBuilderServiceImpl.analyzeValid）

- `@Positive` → ValidatorTypeEnum.POSITIVE

### 5. @Length - Hibernate Validator（JsgBuilderServiceImpl.analyzeValid）

- `@Length(min = 1, max = 100)` → ValidatorTypeEnum.MIN_SIZE / MAX_SIZE

### 6. 集合泛型参数上的校验注解（JsgBuilderServiceImpl）

- `List<@NotBlank @Length(max = 20) String>` → AnnotatedParameterizedType 分支
- 校验描述前缀 "列表内元素"
- tags 字段类型为 `String Array`

### 7. Endpoint 基本信息

- 生成 1 个 md 文件：`支付管理.md`
- handler: `创建支付`
- URL: `POST /api/payments`

### 8. Request Body 字段注释验证

- 支付描述、支付金额、预计支付时间、支付笔数、标签列表

### 9. Response Body 字段

- id、amount、status（状态）
