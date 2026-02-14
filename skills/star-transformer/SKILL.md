---
name: star-transformer
description: 从星型模式结构组装多表关联数据。当用户要求"加载实体及其关联数据"、"一对一/一对多关联查询"、"聚合多表数据到单个 DTO"、"星型模式查询"时使用。自动生成 Whole DTO 和 Mapper 查询方法。
---

# Star Transformer（星型转换器）

当用户需要从星型模式结构（中央事实表 + 周围维度表）组装数据时使用。替代手动编写多查询和组装的样板代码。

## 前置条件

1. **所有涉及的表必须有 Design 类**：先运行 `persistence-generator`（设置 `enableGenerateDesign=true`）
2. Maven 插件已配置
3. 添加 support 依赖（包含 `StarSchema` 类）：

```xml
<dependency>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-support</artifactId>
    <version>${allison1875.version}</version>
</dependency>
```

## 星型模式说明

星型存储结构：
- **中央事实表**：核心业务表
- **维度表**：通过逻辑外键关联到事实表

优点：查询快、结构简单、易理解维护、适合报表分析
缺点：处理复杂层级关系吃力

star-transformer 的价值：自动组装星型结构数据，弥补手动组装的繁琐

## 执行步骤

### 第一步：在 Service 代码中编写 StarSchema DSL

在需要组装结果的位置编写 DSL。

**DSL 模式：**

```java
StarSchema
    .cft(事实表Design.逻辑主键字段, 查询值)
    .oo(一对一维度表Design.外键字段)  // 可选，可多个
    .om(一对多维度表Design.外键字段)  // 可选，可多个
    .over();
```

**DSL 规则速查：**

| 元素 | 说明 |
|---|---|
| `StarSchema.cft(Design.fkField, value)` | 声明中央事实表及其逻辑主键查找 |
| `.oo(Design.fkField)` | 添加**一对一**维度表，fk 字段引用事实表的键 |
| `.om(Design.fkField)` | 添加**一对多**维度表，fk 字段引用事实表的键 |
| `.over()` | 终止 DSL |

**完整示例：**

```java
// StarSchema 是来自 allison1875-support 的静态入口类
StarSchema
    // 中央事实表：Sandbox 表，按 e2bSandboxId 查询
    .cft(SandboxDesign.e2bSandboxId, yourSandboxId)
    // 一对一维度表：SandboxHeartbeat，通过 e2bSandboxId 关联
    .oo(SandboxHeartbeatDesign.e2bSandboxId)
    // 一对多维度表：SandboxEnvVar，通过 sandboxId 关联
    .om(SandboxEnvVarDesign.sandboxId)
    // 结束 DSL
    .over();
```

### 第二步：执行 Maven 命令

```bash
mvn allison1875:star-transformer
```

### 生成/转换结果

1. **Whole DTO** 聚合事实实体和所有维度实体：

```java
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SandboxWholeDTO {
    Sandbox sandbox;                      // 中央事实实体
    SandboxHeartbeat sandboxHeartbeat;     // 一对一维度
    List<SandboxEnvVar> sandboxEnvVars;    // 一对多维度
}
```

2. **Mapper 查询方法**：添加到各相关 Mapper

3. **DSL 调用点替换为**：

```java
SandboxWholeDTO whole = new SandboxWholeDTO();
Sandbox sandbox = sandboxMapper.querySandbox(yourSandboxId);
List<SandboxEnvVar> sandboxEnvVars = sandboxEnvVarMapper.querySandboxEnvVar(yourSandboxId);
SandboxHeartbeat sandboxHeartbeat = sandboxHeartbeatMapper.querySandboxHeartbeat(yourSandboxId);
whole.setSandbox(sandbox);
whole.setSandboxEnvVars(sandboxEnvVars);
whole.setSandboxHeartbeat(sandboxHeartbeat);
```

## 故障排查

| 问题 | 解决方案 |
|---|---|
| 命令失败提示 "plugin not found" | Allison1875 未发布到 Maven Central，需要用户克隆仓库并运行 `mvn install -DskipTests` |
| 找不到 Design 类 | 先运行 `mvn allison1875:persistence-generator` 并设置 `enableGenerateDesign=true` |
| 找不到 StarSchema 类 | 添加 `allison1875-support` 依赖 |
