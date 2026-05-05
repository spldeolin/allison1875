# query-transformer / select-page

## 覆盖功能

本 IT case 验证 query-transformer 的 **PAGE 分页** 终止方法。

### 覆盖的代码路径

| 代码路径                                                                           | 说明                                         |
|--------------------------------------------------------------------------------|--------------------------------------------|
| `QueryChainAnalyzerServiceImpl` `designChain.getNameAsString().equals("page")` | PAGE 终止方法识别                                |
| `QueryChainAnalyzerServiceImpl` `PageParamStyleEnum.PAGE_NO_PAGE_SIZE` 分支      | offset = (pageNo-1)*pageSize 计算            |
| `MapperLayerServiceImpl` `ReturnStyleEnum.PAGE` count 方法生成                     | `<select id='countXxx' resultType='long'>` |
| `MapperLayerServiceImpl` `LIMIT #{offset}, #{limit}`                           | 分页 SQL                                     |
| `MethodGeneratorServiceImpl.generateParam()` PAGE offset/limit 参数注入            | 额外 offset, limit 参数                        |
| `MethodGeneratorServiceImpl.generateReturnType()` PAGE 返回 List                 | `List<Entity>` 返回类型                        |
| `MapperLayerService.generateMethodToMapper()` PAGE count 方法添加                  | Mapper 接口添加 count + page 两个方法              |
| `TransformMethodCallServiceImpl` PAGE offset/limit 表达式                         | `(pageNo - 1) * pageSize, pageSize`        |

### 测试用例

| 方法名            | DSL                                                                        | 覆盖点   |
|----------------|----------------------------------------------------------------------------|-------|
| `pageAll`      | `.select("pageAll").page(pageNo, pageSize)`                                | 无条件分页 |
| `pageByUserId` | `.select("pageByUserId").where().userId.eq(userId).page(pageNo, pageSize)` | 带条件分页 |

### 验证点

1. **Mapper XML** — 生成 count + 分页两个 `<select>` 语句，分页含 `LIMIT #{offset}, #{limit}`
2. **Mapper 接口** — count 返回 `long`，page 方法含 offset/limit 参数
3. **Service 文件** — Design 链替换为 Mapper 调用
