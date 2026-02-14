---
name: persistence-generator
description: 从数据库表生成 MyBatis 持久层代码。当用户要求"生成实体"、"生成 Mapper"、"从数据库表创建持久层"、"刷新实体"或提到新建数据库表时使用。生成 Entity、Mapper 接口、Mapper XML 和 Design 类。
---

# Persistence Generator（持久层生成器）

当用户需要从数据库表生成 MyBatis 持久层代码时使用。这是 query-transformer 和 star-transformer 的前置条件。

## 前置条件

确保用户项目的 `pom.xml` 包含 Allison1875 Maven 插件配置：

```xml
<plugin>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-maven-plugin</artifactId>
    <version>${allison1875.version}</version>
    <configuration>
        <common>
            <basePackage>用户项目的根包</basePackage>
            <author>作者名</author>
        </common>
        <persistenceGenerator>
            <jdbcUrl>jdbc:mysql://localhost:3306</jdbcUrl>
            <userName>数据库用户名</userName>
            <password>数据库密码</password>
            <schema>数据库名</schema>
            <tables>表名1,表名2</tables>
            <isEntityEndWithEntity>false</isEntityEndWithEntity>
            <enableGenerateDesign>true</enableGenerateDesign>
            <deletedSql>is_deleted = 1</deletedSql>
            <notDeletedSql>is_deleted = 0</notDeletedSql>
        </persistenceGenerator>
    </configuration>
</plugin>
```

## 执行步骤

### 第一步：配置数据库连接和目标表

在 `pom.xml` 的 `<persistenceGenerator>` 中配置：

| 配置项 | 说明 |
|---|---|
| `jdbcUrl` | 数据库连接 URL |
| `userName` | 数据库用户名 |
| `password` | 数据库密码 |
| `schema` | 数据库名 |
| `tables` | 逗号分隔的表名，省略或 `*` 表示所有表 |
| `isEntityEndWithEntity` | Entity 类名是否以 "Entity" 后缀结尾 |
| `enableGenerateDesign` | 是否生成 Design 类（query-transformer 需要） |
| `deletedSql` | 软删除行的 SQL 条件片段 |
| `notDeletedSql` | 未删除行的 SQL 条件片段 |

### 第二步：执行 Maven 命令

```bash
mvn allison1875:persistence-generator
```

### 生成结果

为每个指定的表生成：

| 生成文件 | 位置 | 说明 |
|---|---|---|
| `Xxx.java` 或 `XxxEntity.java` | `entityPackage` | 实体类，字段映射数据库列 |
| `XxxMapper.java` | `mapperPackage` | Mapper 接口，包含基本 CRUD 方法 |
| `XxxMapper.xml` | `mapperXmlDirs` | MyBatis XML，包含 resultMap 和 SQL |
| `XxxDesign.java` | `designPackage` | Design 类，query-transformer 的流式 API 入口 |

## 重要说明

- **幂等操作**：此命令可以多次运行，会重新生成文件并尽可能合并现有自定义内容
- **Design 类**：如果需要使用 query-transformer 或 star-transformer，必须设置 `<enableGenerateDesign>true</enableGenerateDesign>`

## 故障排查

| 问题 | 解决方案 |
|---|---|
| 命令失败提示 "plugin not found" | Allison1875 未发布到 Maven Central，需要用户克隆仓库并运行 `mvn install -DskipTests` |
| 连接数据库失败 | 检查 jdbcUrl、userName、password 配置是否正确 |
| 表不存在 | 检查 schema 和 tables 配置是否正确 |
