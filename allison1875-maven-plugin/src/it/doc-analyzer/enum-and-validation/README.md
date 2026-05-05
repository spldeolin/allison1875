# enum-and-validation 集成测试

## 概述

验证 doc-analyzer 对**枚举字段类型的分析**和**更多校验注解**的处理。

## 覆盖的功能

### 1. 枚举分析（EnumServiceImpl.analyzeEnumConstants）

- TaskStatusEnum 枚举类型字段（CreateTaskReq.status, TaskResp.status）
- 通过反射调用 `getCode()` / `getTitle()` 获取枚举项
- 枚举项输出到 Markdown 表格"其他"列：`1 : 待处理`、`2 : 处理中`、`3 : 已完成`、`4 : 已取消`

### 2. 配置项 getEnumCodeMethodName / getEnumTitleMethodName

- `getEnumCodeMethodName: getCode`
- `getEnumTitleMethodName: getTitle`

### 3. 校验注解分析（JsgBuilderServiceImpl.analyzeValid）

- `@NotBlank`（title）
- `@Size(min = 1, max = 200)`（title）→ ValidatorTypeEnum.MIN_SIZE / MAX_SIZE
- `@Size(max = 2000)`（description）
- `@NotNull`（status, priority）
- `@Min(1)` / `@Max(10)`（priority）→ ValidatorTypeEnum.MIN_NUMBER / MAX_NUMBER
- `@Pattern(regexp = "^TASK-\\d{6}$")`（taskCode）→ ValidatorTypeEnum.REGEX

### 4. BigDecimal 字段类型

- `budget` 字段为 `BigDecimal` 类型，验证 JsonSchema 能正确处理数值类型

### 5. Endpoint 基本信息

- 生成 1 个 md 文件：`任务管理.md`
- handler: `创建任务`
- URL: `POST /api/tasks`

### 6. Request Body 字段

- title（任务标题）、description（任务描述）、status（任务状态）、priority、budget（预算金额）、taskCode（任务编号）

### 7. Response Body 枚举验证

- Response Body 的 status 字段同样包含枚举项（`1 : 待处理` 等）
