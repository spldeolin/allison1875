---
name: allison1875
description: Allison1875 是 Java AST 代码生成工具集。当用户需要"创建 API 接口"、"生成实体/Mapper"、"条件查询数据库"、"多表关联查询"时，根据场景选择对应的子技能：handler-transformer、persistence-generator、query-transformer、star-transformer。
---

# Allison1875 — Java AST 代码生成工具集

Allison1875 以 Maven 插件形式提供，通过将 Java 内部 DSL 转换为生产级代码，消除重复性编码工作。

## 核心工作流程

1. **识别场景** → 选择对应功能
2. **编写 DSL** → 在用户源代码中编写
3. **配置插件** → 确保 `pom.xml` 配置正确
4. **执行命令** → 运行 Maven 目标触发转换

## 功能速查

| 场景 | 技能 | Maven 命令 |
|---|---|---|
| 创建 API 接口 | `/handler-transformer` | `mvn allison1875:handler-transformer` |
| 从数据库生成实体/Mapper | `/persistence-generator` | `mvn allison1875:persistence-generator` |
| 带条件查询数据库 | `/query-transformer` | `mvn allison1875:query-transformer` |
| 多表关联数据组装 | `/star-transformer` | `mvn allison1875:star-transformer` |

## 决策流程

```
用户请求
    │
    ├─ "创建 API / 添加接口 / 生成 Controller"
    │   └─► /handler-transformer
    │
    ├─ "生成实体 / 生成 Mapper / 从数据库表创建"
    │   └─► /persistence-generator
    │
    ├─ "条件查询 / 动态 WHERE / 可选过滤"
    │   └─► /query-transformer（需要先有 Design 类）
    │
    └─ "关联查询 / 一对多 / 聚合多表数据"
        └─► /star-transformer（需要先有 Design 类）
```

## 前置条件

### Maven 插件配置

```xml
<plugin>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-maven-plugin</artifactId>
    <version>${allison1875.version}</version>
    <configuration>
        <common>
            <basePackage>com.example.yourapp</basePackage>
            <author>作者名</author>
        </common>
        <!-- 各功能的配置见对应子技能 -->
    </configuration>
</plugin>
```

### Support 依赖

handler-transformer 和 star-transformer 需要：

```xml
<dependency>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-support</artifactId>
    <version>${allison1875.version}</version>
</dependency>
```

## 通用配置参考

在 `<common>` 中设置，大多数有基于 `basePackage` 推导的默认值：

| 属性 | 默认约定 | 说明 |
|---|---|---|
| `basePackage` | *必填* | 项目根包 |
| `author` | `"allison1875"` | 生成代码的 @author |
| `reqDTOPackage` | `{basePackage}.dto.req` | 请求 DTO 包 |
| `respDTOPackage` | `{basePackage}.dto.resp` | 响应 DTO 包 |
| `servicePackage` | `{basePackage}.service` | Service 接口包 |
| `serviceImplPackage` | `{basePackage}.service.impl` | Service 实现包 |
| `mapperPackage` | `{basePackage}.mapper` | Mapper 接口包 |
| `entityPackage` | `{basePackage}.model` | Entity 类包 |
| `designPackage` | `{basePackage}.design` | Design 类包 |
| `mapperXmlDirs` | `src/main/resources/mapper` | MyBatis XML 目录 |
| `enableJavaxMoveToJakarta` | `false` | 使用 jakarta.* 替代 javax.* |

## 故障排查

| 问题 | 解决方案 |
|---|---|
| `mvn allison1875:*` 失败，提示 "plugin not found" | Allison1875 未发布到 Maven Central，告知用户克隆仓库并运行 `mvn install -DskipTests` |
| query/star-transformer 找不到 Design 类 | 先运行 `persistence-generator` 并设置 `enableGenerateDesign=true` |
| 生成代码使用 javax.validation | 设置 `<enableJavaxMoveToJakarta>true</enableJavaxMoveToJakarta>` |

## 子技能详情

- **[handler-transformer](./.claude/skills/handler-transformer/SKILL.md)**: 创建 HTTP API 接口
- **[persistence-generator](./.claude/skills/persistence-generator/SKILL.md)**: 从数据库生成持久层
- **[query-transformer](./.claude/skills/query-transformer/SKILL.md)**: 条件数据库查询
- **[star-transformer](./.claude/skills/star-transformer/SKILL.md)**: 星型模式数据组装