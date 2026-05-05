# query-transformer / where-forced

## 覆盖功能

本 IT case 验证 query-transformer 的 **whereEvenNull()** 强制条件模式。

### 核心差异：`where()` vs `whereEvenNull()`

| 方法                 | SQL 生成                                         | 适用场景             |
|--------------------|------------------------------------------------|------------------|
| `.where()`         | `<if test="var != null">AND col = #{var}</if>` | 动态条件，null 时跳过    |
| `.whereEvenNull()` | `AND col = #{var}`                             | 强制条件，即使 null 也输出 |

### 覆盖的代码路径

| 代码路径                                                             | 说明                                       |
|------------------------------------------------------------------|------------------------------------------|
| `QueryChainAnalyzerServiceImpl` `isByForced` 标识设置                | `chainCode.contains(".whereEvenNull()")` |
| `MapperLayerServiceImpl` `concatWhereSection()` EQUALS forced 分支 | 直接输出 `AND col = #{var}`                  |
| `MapperLayerServiceImpl` `concatWhereSection()` DELETE forced 分支 | DELETE 中的强制条件                            |

### 测试用例

| 方法名                           | DSL                                                           | 覆盖点           |
|-------------------------------|---------------------------------------------------------------|---------------|
| `findByIdForced`              | `.whereEvenNull().id.eq(id).one()`                            | SELECT 单条件强制  |
| `listByStatusAndUserIdForced` | `.whereEvenNull().status.eq(status).userId.eq(userId).list()` | SELECT 多条件强制  |
| `deleteByIdForced`            | `.delete().whereEvenNull().id.eq(id).over()`                  | DELETE + 强制条件 |

### 验证点

1. **Mapper XML** — 强制模式下**无** `<if test>` 包裹，直接输出 `AND column = #{var}`
2. **对比** — 普通 `where()` 模式会生成 `<if test="var != null">`，`whereEvenNull()` 不会
3. **Service 文件** — Design 链替换为 Mapper 调用
