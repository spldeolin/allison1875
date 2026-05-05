# query-transformer / update-delete

## 覆盖功能

本 IT case 验证 query-transformer 对 **UPDATE** 和 **DELETE** 两种链初始方法的处理。

### 覆盖的代码路径

| 代码路径                                                                | 说明                |
|---------------------------------------------------------------------|-------------------|
| `QueryChainAnalyzerServiceImpl` `betweenCode.startsWith("update(")` | UPDATE 链识别        |
| `QueryChainAnalyzerServiceImpl` `betweenCode.startsWith("delete(")` | DELETE 链识别        |
| `QueryChainAnalyzerServiceImpl` `NextableUpdateChain` 类型解析          | SET 赋值解析          |
| `MapperLayerServiceImpl` `ChainInitialMethod.UPDATE` 分支             | `<update>` XML 生成 |
| `MapperLayerServiceImpl` `ChainInitialMethod.DELETE` 分支             | `<delete>` XML 生成 |
| `MapperLayerServiceImpl` `concatUpdateStartTag()`                   | UPDATE 起始标签生成     |
| `MapperLayerServiceImpl` `concatDeleteStartTag()`                   | DELETE 起始标签生成     |
| `MethodGeneratorServiceImpl.generateReturnType()` UPDATE/DELETE 分支  | 返回 `int` 类型       |

### 测试用例

| 方法名                         | DSL                                                                | 覆盖点             |
|-----------------------------|--------------------------------------------------------------------|-----------------|
| `updateStatusById`          | `.update().status(status).where().id.eq(id).over()`                | 单字段 SET + WHERE |
| `updateAmountAndRemarkById` | `.update().amount(amount).remark(remark).where().id.eq(id).over()` | 多字段 SET         |
| `deleteOrderById`           | `.delete().where().id.eq(id).over()`                               | DELETE + WHERE  |
| `deleteByUserId`            | `.delete().where().userId.eq(userId).over()`                       | DELETE + 非主键条件  |

### 验证点

1. **Mapper XML** — `<update>` 标签含 UPDATE SET + WHERE；`<delete>` 标签含 DELETE FROM + WHERE
2. **Mapper 接口** — 方法返回类型为 `int`
3. **Service 文件** — Design 链替换为 Mapper 调用
