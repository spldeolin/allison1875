# java-record 集成测试

## 概述

验证 Java Record 类型作为 Request Body / Response Body 时，record component 及其 Javadoc `@param` 注释
能被正确解析为 API 文档字段。**需要 JDK 17+**。

## 覆盖的功能

### 1. Record 类型作为 Request Body（FieldServiceImpl / JsgBuilderServiceImpl）

- `CreateAddressReq` 是一个 Java Record，包含 `title`、`city`、`zipCode` 三个 component
- Record 的 Javadoc `@param` 注释（地址标题、城市、邮编）被正确提取为字段描述

### 2. Record 类型作为 Response Body（ResponseBodyServiceImpl）

- `AddressResp` 是一个 Java Record，包含 `id` 等 component
- Response Body 的字段文档从 record component 的 `@param` 注释中提取

### 3. Markdown 输出结构

- 生成 1 个 md 文件：`地址管理.md`
- 包含 `### Request Body (application/json)` 和 `### Response Body (application/json)` 区域
- endpoint: `POST /api/addresses`（创建地址）

### 4. 配置

- `javaVersion: "17"`
- `enableJavaxMoveToJakarta: true`（Spring 6+ / Jakarta 场景）
- `maven.compiler.source/target: 17`（pom.xml）
- `flushTo: [MARKDOWN]`
- `markdownDir: api-docs`

## 前置条件

运行此 IT 用例前需切换到 JDK 17 或更高版本：

```bash
jenv shell 21  # 或 export JENV_VERSION=21
```
