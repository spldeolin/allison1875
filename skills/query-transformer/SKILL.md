---
name: query-transformer
description: 编写类型安全的条件数据库查询。当用户要求"带条件查询数据库"、"动态 WHERE 查询"、"按条件搜索"、"可选条件过滤"时使用。自动生成 Mapper 方法、Param DTO 和 MyBatis XML。
---

# Query Transformer（查询转换器）

当用户需要编写带动态条件的数据库查询时使用。通过 QueryChain DSL 生成 Mapper 方法、Param DTO 和 XML。

## 前置条件

1. **必须有 Design 类**：目标表需要先运行 `persistence-generator`（设置 `enableGenerateDesign=true`）生成 Design 类
2. Maven 插件已配置

## 执行步骤

### 第一步：在 Service 代码中编写 QueryChain DSL

在需要查询结果的位置（通常在 Service 实现中）编写流式 DSL 语句。

**DSL 模式：**

```java
{Design}.query("{方法名}").{选择字段}.where().{条件}.{终结操作}();
```

**DSL 规则速查：**

| 元素 | 说明 |
|---|---|
| `XxxDesign.query("methodName")` | 入口点，methodName 成为 Mapper 方法名 |
| `.fieldName`（where 前） | 添加到 SELECT 子句，省略则选择所有列 |
| `.where()` | 标记 WHERE 条件开始 |
| `.fieldName.eq(value)` | `= #{param}`，带空值检查 `<if>` |
| `.fieldName.ne(value)` | `!= #{param}` |
| `.fieldName.gt(value)` | `> #{param}` |
| `.fieldName.ge(value)` | `>= #{param}` |
| `.fieldName.lt(value)` | `< #{param}` |
| `.fieldName.le(value)` | `<= #{param}` |
| `.fieldName.like(value)` | `LIKE CONCAT('%', #{param}, '%')` |
| `.fieldName.in(collection)` | `IN <foreach>` |
| `.fieldName.isnull()` | `IS NULL`（无参数，始终应用） |
| `.fieldName.notnull()` | `IS NOT NULL`（无参数，始终应用） |
| `.one()` | 终结：返回单个实体或 null |
| `.list()` | 终结：返回 `List<Entity>` |
| `.count()` | 终结：返回 `long` |
| `.groupBy{FieldName}()` | 终结：返回 `Map<FieldType, List<Entity>>` |

**完整示例：**

```java
// 带多个可选条件的单表查询
Map<String, List<SandboxHost>> sandboxHosts = SandboxHostDesign
    .query("findBySandboxId")       // Mapper 方法名
    .sandboxHostIp                  // SELECT sandbox_host_ip
    .createdAt                      // SELECT created_at
    .where()                        // WHERE 开始
    .hostId.isnull()                // AND host_id IS NULL（始终应用）
    .sandboxHostIp.eq(sandboxId)    // AND sandbox_host_ip = #{sandboxHostIp}（带空值检查）
    .createdAt.ge(new Date())       // AND created_at >= #{createdAt}
    .maxSandbox.le(20)              // AND max_sandbox <= #{maxSandbox}
    .sandboxProxyBaseUrl.like("a")  // AND sandbox_proxy_base_url LIKE '%a%'
    .groupBySandboxProxyBaseUrl();  // GROUP BY + 收集为 Map
```

### 第二步：执行 Maven 命令

```bash
mvn allison1875:query-transformer
```

### 生成/转换结果

DSL 语句会被**原地替换**为常规代码：

1. **Param DTO**（如 `FindBySandboxIdParam.java`）：生成在 `paramDTOPackage`
2. **Mapper 方法**：添加到对应 Mapper 接口
3. **Mapper XML**：带动态 `<where>` 和 `<if>` 的 `<select>`
4. **DSL 调用点替换为**：构造 Param → 调用 Mapper → 收集结果

**转换后代码示例：**

```java
final FindBySandboxIdParam param = new FindBySandboxIdParam();
param.setSandboxHostIp(sandboxId);
param.setCreatedAt(new Date());
param.setMaxSandbox(20);
param.setSandboxProxyBaseUrl("a");
Map<String, List<SandboxHost>> sandboxHosts = sandboxHostMapper
    .findBySandboxId(param)
    .stream()
    .collect(Collectors.groupingBy(SandboxHost::getSandboxProxyBaseUrl));
```

## 重要说明

- **建议仅使用单表查询**：虽然技术上支持 JOIN，但有局限性
- **多表场景**：使用多个单表查询后在 Java 中组装，或使用 star-transformer

## 故障排查

| 问题 | 解决方案 |
|---|---|
| 命令失败提示 "plugin not found" | Allison1875 未发布到 Maven Central，需要用户克隆仓库并运行 `mvn install -DskipTests` |
| 找不到 Design 类 | 先运行 `mvn allison1875:persistence-generator` 并设置 `enableGenerateDesign=true` |
| 需要 JOIN 查询 | 建议使用多个单表查询 + Java 组装，或使用 star-transformer |
