# query-transformer / order-by

## 覆盖功能

本 IT case 验证 query-transformer 对 **ORDER BY** 子句的生成。

### 覆盖的代码路径

| 代码路径                                                        | 说明               |
|-------------------------------------------------------------|------------------|
| `QueryChainAnalyzerServiceImpl` `OrderByChainSequence` 类型解析 | ORDER BY 字段和方向识别 |
| `MapperLayerServiceImpl` `sortProperties` 非空分支              | ORDER BY SQL 拼接  |
| `OrderSequenceEnum.ASC`                                     | 升序               |
| `OrderSequenceEnum.DESC`                                    | 降序 + ` DESC` 后缀  |

### 测试用例

| 方法名                            | DSL                                                          | 覆盖点                 |
|--------------------------------|--------------------------------------------------------------|---------------------|
| `listOrderByCreatedAtAsc`      | `.order().createdAt.asc().list()`                            | 单字段 ASC             |
| `listOrderByAmountDesc`        | `.order().amount.desc().list()`                              | 单字段 DESC            |
| `listOrderByMultiFields`       | `.order().status.asc().createdAt.desc().list()`              | 多字段排序               |
| `listByUserIdOrderByCreatedAt` | `.where().userId.eq(userId).order().createdAt.desc().list()` | WHERE + ORDER BY 组合 |

### 验证点

1. **Mapper XML** — 包含 `ORDER BY` 关键字，正确列名和排序方向（ASC 默认省略/DESC 显式输出）
2. **Mapper 接口** — 4 个查询方法全部生成
3. **Service 文件** — Design 链替换为 Mapper 调用
