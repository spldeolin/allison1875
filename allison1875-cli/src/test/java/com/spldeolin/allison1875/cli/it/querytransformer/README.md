# query-transformer 集成测试用例说明

本目录包含 query-transformer 工具的全部集成测试用例。每个测试通过基类 `QueryTransformerItBaseTest` 执行一个两阶段流程：先运行
persistence-generator 生成 Entity/Design/Mapper/XML，再将 `java-qt/` 下含有 Design DSL 链的 Service 源码拷贝到
`src/main/java/`，最后运行 query-transformer 解析 DSL 链并转换为 Mapper 调用。断言覆盖 Mapper 接口、Mapper XML（SQL 语句）和
Service 文件。

query-transformer 的核心职责是：将 Service 中基于 Design 类的 **QueryChain DSL**（如
`TOrderDesign.select("name").orderNo.userId.list()`）转换为对应的 Mapper 接口方法、MyBatis XML SQL 语句，并将 Service 中的
DSL 调用替换为 Mapper 方法调用。

---

## 基本查询

### SelectBasicItTest

- 验证基本 SELECT 查询功能
- 生成 `findById`（`one()` → `LIMIT 1`）、`listAll`（`list()` 无 WHERE）、`countByUserId`（`count()` → `COUNT(*)`）
- Service 中 `TOrderDesign.` 链被替换为 `tOrderMapper.` 调用
- Mapper 自动注入到 Service

---

## 指定属性查询

### SelectPropertiesItTest

- 验证 select 指定属性功能
- 单属性查询（如只查 `orderNo`）→ 返回 `List<String>`
- 多属性查询（如查 `orderNo` + `userId` + `amount`）→ 生成 Record DTO，包含指定字段
- 多属性 + WHERE 条件组合

---

## 比较运算符

### AllOperatorsItTest

- 验证全部 11 种比较运算符的 SQL 生成
- `eq` → `=`、`ne` → `!=`、`gt` → `>`、`ge` → `>=`、`lt` → `<`、`le` → `<=`
- `like` → `LIKE CONCAT('%', ?, '%')`
- `in` → `IN (foreach)`、`nin` → `NOT IN (foreach)`
- `notnull` → `IS NOT NULL`、`isnull` → `IS NULL`
- Mapper 接口和 Service 中每个运算符对应的方法均正确生成和调用

---

## 分页查询

### SelectPageItTest

- 验证 `page()` 分页查询功能
- 生成分页方法（`pageAll`、`pageByUserId`）和对应的 count 方法
- XML 中包含 `LIMIT #{offset}, #{limit}` 子句
- Mapper 方法包含 `offset`/`limit` 参数，count 方法返回 `long`

---

## 排序

### OrderByItTest

- 验证 ORDER BY 排序功能
- 单字段 ASC / DESC 排序
- 多字段排序
- WHERE 条件 + ORDER BY 组合

---

## 聚合方法

### SelectMapGroupItTest

- 验证 `map()` / `group()` 聚合方法
- `map()` → Mapper 方法带 `@MapKey` 注解，返回 Map
- `group()` → Service 中生成 `Collectors.groupingBy` 调用
- 支持带 WHERE 条件的 map 查询

---

## UPDATE / DELETE

### UpdateDeleteItTest

- 验证 UPDATE 和 DELETE DSL 链
- UPDATE 单字段（`SET status = #{status}`）和多字段（`SET amount, remark`）
- DELETE 按 ID 和按条件
- Mapper 接口方法返回 `int`
- XML 中生成 `UPDATE t_order` / `DELETE FROM t_order` 语句

---

## 强制条件（whereEvenNull）

### WhereForcedItTest

- 验证 `.whereEvenNull()` 强制条件模式
- XML 中条件不包裹 `<if test>`，直接输出 `AND column = #{var}`
- 适用于 SELECT 和 DELETE
- 支持单条件和多条件

---

## 参数阈值（ParamDTO 自动生成）

### ParamThresholdItTest

- 验证 WHERE 条件超过 3 个时自动生成 ParamDTO
- ParamDTO 生成在 `dto/param/` 包下，包含所有 WHERE 条件字段
- Mapper 接口方法参数为单个 ParamDTO 类型（不使用 `@Param`）
- Service 中包含 ParamDTO 构建代码（setter 链调用）

---

## JOIN 查询

### JoinBasicItTest

- 验证基本 LEFT JOIN 查询功能
- XML 中包含 `LEFT JOIN t_user` 和 `t1`/`t2` 表别名
- 生成 Record DTO，包含被 join 表的字段（如 `userName`）
- Service 中 Design 链被替换为 Mapper 调用

---

## 当前覆盖率概况（JaCoCo）

| 类                              | 指令覆盖率 | 分支覆盖率   |
|--------------------------------|-------|---------|
| QueryTransformer               | 87%   | 86%     |
| QueryChainDetectorServiceImpl  | 91%   | **69%** |
| QueryChainAnalyzerServiceImpl  | 91%   | 82%     |
| MethodGeneratorServiceImpl     | 81%   | **71%** |
| MapperLayerServiceImpl         | 79%   | **62%** |
| TransformMethodCallServiceImpl | 88%   | 83%     |
| DesignServiceImpl              | 77%   | **63%** |

---

## 待补充 case

> 每个 case 标注了 **归属决策**（🆕 新建 / 🔄 扩展已有），以及关键实现要点。

### where-forced-all-operators 🔄 扩展 WhereForcedItTest

- **目标覆盖**：`MapperLayerServiceImpl.concatWhereSection()` 中 `isByForced=true` 时 ne/gt/ge/lt/le/like/in/nin 运算符分支（当前
  `where-forced` 仅测试了 eq）
- DSL 中使用 `whereEvenNull()` + 各种比较运算符（ne、gt、ge、lt、le、like、in、nin）
- 验证 XML 中条件不包裹 `<if test>`，直接输出对应运算符
- **归属决策：扩展已有 `WhereForcedItTest`**
    - 理由：`where-forced` 与本 case 测试同一功能（`whereEvenNull` 强制条件模式），仅缺少 eq 之外的运算符；在
      `OrderService.java`（java-qt）中追加 ne/gt/ge/lt/le/like/in/nin 的 DSL 链，并在 `WhereForcedItTest` 中补充对应运算符的
      XML 断言即可，不需要新的表结构或 yml 配置
    - 实现要点：
        - 在 `where-forced/src/main/java-qt/…/OrderService.java` 中追加使用 ne/gt/ge/lt/le/like/in/nin +
          `whereEvenNull()` 的 DSL 链
        - 在 `WhereForcedItTest` 中补充断言：XML 中对应方法不含 `<if test>`，且包含 `!=`、`>`、`>=`、`<`、`<=`、`LIKE`、`IN`、
          `NOT IN` 运算符

### join-other-types 🆕 新建

- **目标覆盖**：`JoinTypeEnum.of()` 中 right/inner/outer 分支、`MapperLayerServiceImpl.concatJoinSection()` 中不同 JOIN 类型
  SQL 生成
- DSL 中分别使用 `rightJoin()`、`innerJoin()`、`outerJoin()`
- 验证 XML 中出现 `RIGHT JOIN`、`INNER JOIN`、`OUTER JOIN`
- **归属决策：新建独立 IT 类**
    - 资源目录：`join-other-types`
    - 理由：`join-basic` 仅覆盖 `LEFT JOIN` 且断言中硬编码了 `LEFT JOIN` 检查；追加 RIGHT/INNER/OUTER JOIN 需不同的 DSL
      链和独立的 SQL 断言，混入会导致断言难以定位
    - 实现要点：
        - `OrderService.java`（java-qt）中 3 条 DSL 链分别使用 `rightJoin()`、`innerJoin()`、`outerJoin()`
        - 断言重点：XML 中分别出现 `RIGHT JOIN`、`INNER JOIN`、`OUTER JOIN`

### join-multi-on-conditions 🆕 新建

- **目标覆盖**：`MapperLayerServiceImpl.concatJoinSection()` 中 `joinConditions.size() > 1` 的分支（多个 ON 条件占多行）
- DSL 中 JOIN 使用多个 ON 条件（如 `.on().userId.eq(TOrderDesign.userId).status.eq(TOrderDesign.status)`）
- 验证 XML 中 ON 子句包含括号和多行 AND 条件
- **归属决策：新建独立 IT 类**
    - 资源目录：`join-multi-on-conditions`
    - 理由：`join-basic` 仅有 1 个 ON 条件（走 `joinConditions.size() == 1` 分支），多 ON
      条件触发的是不同代码路径（括号包裹 + 多行 AND），需独立 DSL 和断言
    - 实现要点：
        - DSL 链中 JOIN 含 2+ 个 ON 条件
        - 断言重点：XML ON 子句包含 `(`、`)` 括号和多个 AND 条件行

### join-on-operators 🆕 新建

- **目标覆盖**：`MapperLayerServiceImpl.concatOnBinary()` 中 ne/gt/ge/lt/le/in/nin/like/isnull/notnull 等分支（当前仅 eq
  被覆盖）
- DSL 中 JOIN ON 使用 eq 以外的运算符
- 验证 ON 子句中出现对应 SQL 运算符
- **归属决策：新建独立 IT 类**
    - 资源目录：`join-on-operators`
    - 理由：与 `join-basic`（仅 eq ON）和 `all-operators`（WHERE 运算符）均不同，此 case 覆盖的是 JOIN ON 子句中的运算符分支，需独立
      DSL
    - 实现要点：
        - DSL 链中 JOIN ON 分别使用 ne、gt、in 等运算符
        - 断言重点：XML ON 子句中出现 `!=`、`>`、`IN` 等对应 SQL

### join-no-select-properties 🆕 新建

- **目标覆盖**：`QueryChainAnalyzerServiceImpl` 中 `returnProperties.isEmpty() && !joinConditions.isEmpty()` 分支、
  `MapperLayerServiceImpl` 中 join + 无 selectProperties 时需要 `t1.` 前缀的完整 select 列表
- DSL 中使用 JOIN 但不指定 select 属性，即 `TOrderDesign.select("xxx").leftJoin().TUserEntity.userName.on()...list()`
  （主表属性全选）
- 验证 XML 中 SELECT 子句包含主表所有列（带 `t1.` 前缀 + `AS`）和 join 表的列
- **归属决策：新建独立 IT 类**
    - 资源目录：`join-no-select-properties`
    - 理由：`join-basic` 指定了 select 属性，此 case 需 join + 不指定属性触发主表全列 `t1.` 前缀的完整 select 列表生成逻辑
    - 实现要点：
        - DSL 链中 JOIN 但仅在 join 侧指定字段，主表不指定属性（全选）
        - 断言重点：XML SELECT 子句包含主表所有列（`t1.column_name AS fieldName` 格式）

### assigned-chain 🆕 新建

- **目标覆盖**：`MethodGeneratorServiceImpl.isAssigned()` 返回 true 时的各 returnStyle 分支（assigned + LIST/MAP/ONE 等）、
  `DesignServiceImpl.replaceDesign()` 中 parent 为 VariableDeclarator 的分支
- DSL 链赋值给变量（如 `List<TOrderEntity> result = TOrderDesign.select("xxx").where()...list();`）
- 验证返回值使用 Entity 类型（而非生成 Record DTO），Service 中替换后保留变量声明
- **归属决策：新建独立 IT 类**
    - 资源目录：`assigned-chain`
    - 理由：所有已有 case 的 DSL 链均为无赋值语句（expression statement），此 case 需 DSL 链赋值给变量以触发
      `isAssigned()=true` 分支，返回类型和 Service 替换逻辑均不同
    - 实现要点：
        - `OrderService.java`（java-qt）中 `List<TOrderEntity> result = TOrderDesign.select("xxx")...list();`
        - 断言重点：不生成 Record DTO（返回 Entity 类型）、Service 中保留
          `List<TOrderEntity> result = tOrderMapper.xxx(...)` 赋值语句

### select-one-property-map 🆕 新建

- **目标覆盖**：`MethodGeneratorServiceImpl.generateReturnType()` 中 `returnProps.size()==1` + `ReturnStyleEnum.MAP`
  的组合分支
- DSL 中查询单属性并以 mapBy 终结（如 `.select("xxx").orderNo.mapByUserId()`）
- 验证返回值类型为 `Map<KeyType, SinglePropType>` 而非 `Map<KeyType, Entity>`
- **归属决策：新建独立 IT 类**
    - 资源目录：`select-one-property-map`
    - 理由：`select-map-group` 的 map 查询是多属性 + MAP，此 case 需单属性 + MAP 触发不同的返回类型生成分支
    - 实现要点：
        - DSL 链中 select 单属性 + `mapBy` 终结
        - 断言重点：Mapper 方法返回类型包含 `Map<...>` 且 value 类型为单属性类型（如 `String`）而非 Entity/Record

### default-method-name 🆕 新建

- **目标覆盖**：`QueryChainAnalyzerServiceImpl.analyzeSpecifiedMethodName()` 中 `arguments.isEmpty()` 分支（不指定方法名，使用默认名称）
- DSL 链使用无参的 `select()` / `update()` / `delete()`（不传方法名）
- 验证生成的 Mapper 方法名为默认格式（如 `queryTOrder`、`updateTOrder`、`deleteTOrder`）
- **归属决策：新建独立 IT 类**
    - 资源目录：`default-method-name`
    - 理由：所有已有 case 的 `select("xxx")`/`update("xxx")`/`delete("xxx")` 均传了方法名字符串参数，无法触发
      `arguments.isEmpty()` 分支
    - 实现要点：
        - `OrderService.java`（java-qt）中使用 `TOrderDesign.select().orderNo.list()` 等无参 DSL
        - 断言重点：Mapper 方法名为默认格式 `queryTOrder` / `updateTOrder` / `deleteTOrder`

### page-with-param-dto 🆕 新建

- **目标覆盖**：`MethodGeneratorServiceImpl.generateParam()` 中 `binaries.size() > 1 && returnStyle==PAGE` 时强制生成
  ParamDTO 的分支（当前 `param-threshold` 仅测试了 >3 条件的场景）、`TransformMethodCallServiceImpl.argumentBuildStmts()` 中
  PAGE 分支生成 offset/limit setter
- DSL 中 page + 2 个 WHERE 条件（2 > 1 但 ≤ 3，因为 PAGE 时阈值降低）
- 验证生成 ParamDTO（含 offset/limit 字段）和 Service 中的 setter 调用
- **归属决策：新建独立 IT 类**
    - 资源目录：`page-with-param-dto`
    - 理由：`param-threshold` 测试的是 >3 条件触发 ParamDTO，`select-page` 仅有 1 个 WHERE 条件（不触发 ParamDTO）；此 case 需
      page + 2 个条件触发 PAGE 特有的降低阈值逻辑
    - 实现要点：
        - DSL 中 `page()` + 2 个 WHERE 条件
        - 断言重点：生成 ParamDTO（含 `offset`/`limit` 字段）、Service 中包含 ParamDTO 的 setter 调用

### soft-delete-in-query 🆕 新建

- **目标覆盖**：`MapperLayerServiceImpl.concatWhereSection()` 中 `designMeta.getNotDeletedSql() != null` 分支
- persistence-generator 配置 `notDeletedSql`/`deletedSql`，表含逻辑删除字段
- 验证 query-transformer 生成的 XML 查询方法中 WHERE 自动加上 `notDeletedSql` 条件
- **归属决策：新建独立 IT 类**
    - 资源目录：`soft-delete-in-query`
    - 理由：所有已有 case 的 `.allison1875.yml` 均未配置 `notDeletedSql`/`deletedSql`，需独立配置 + 含逻辑删除字段的 DDL
    - 实现要点：
        - `.allison1875.yml` 配置 `notDeletedSql: "is_deleted = 0"` 和 `deletedSql: "is_deleted = 1"`
        - DDL 中 t_order 表含 `is_deleted` 字段
        - 断言重点：XML 查询方法 WHERE 子句中自动包含 `AND is_deleted = 0`

### delete-with-in-operator 🆕 新建

- **目标覆盖**：`MapperLayerServiceImpl.concatDeleteStartTag()` 中 `comparison==IN` 时不附加 parameterType 的分支
- DSL 中 DELETE 使用 IN 运算符（如 `.delete("xxx").where().id.in(ids).over()`）
- 验证 `<delete>` 标签不含 `parameterType` 属性
- **归属决策：新建独立 IT 类**
    - 资源目录：`delete-with-in-operator`
    - 理由：`update-delete` 的 DELETE 使用 eq 运算符（有 `parameterType`），此 case 需 IN 运算符触发不同的 `<delete>` 标签生成逻辑
    - 实现要点：
        - DSL 中 `TOrderDesign.delete("xxx").where().id.in(ids).over()`
        - 断言重点：`<delete>` 标签不含 `parameterType` 属性

### select-with-order-and-join 🆕 新建

- **目标覆盖**：`MapperLayerServiceImpl.generateMethodToMapperXml()` 中 ORDER BY + join 组合路径（`join ? "t1." : ""` 在
  ORDER BY 部分），以及 `select-properties` + join 组合
- DSL 中使用 JOIN + 指定属性 + ORDER BY
- 验证 XML 中 ORDER BY 的列名带 `t1.` 前缀，SELECT 子句混合主表和 join 表的列
- **归属决策：新建独立 IT 类**
    - 资源目录：`select-with-order-and-join`
    - 理由：`order-by` 不含 JOIN（无 `t1.` 前缀），`join-basic` 不含 ORDER BY；此 case 需三者组合触发 ORDER BY + join 前缀路径
    - 实现要点：
        - DSL 中 JOIN + 指定属性 + ORDER BY 组合
        - 断言重点：XML 中 ORDER BY 列名带 `t1.` 前缀、SELECT 混合主表和 join 表列

### 补充计划总览

| #  | Case 名称                    | 决策    | 资源目录 / 扩展目标                     | 涉及 Service 类                                               |
|----|----------------------------|-------|---------------------------------|------------------------------------------------------------|
| 1  | where-forced-all-operators | 🔄 扩展 | `WhereForcedItTest` 补充 DSL + 断言 | MapperLayerServiceImpl                                     |
| 2  | join-other-types           | 🆕 新建 | `join-other-types`              | JoinTypeEnum, MapperLayerServiceImpl                       |
| 3  | join-multi-on-conditions   | 🆕 新建 | `join-multi-on-conditions`      | MapperLayerServiceImpl                                     |
| 4  | join-on-operators          | 🆕 新建 | `join-on-operators`             | MapperLayerServiceImpl                                     |
| 5  | join-no-select-properties  | 🆕 新建 | `join-no-select-properties`     | QueryChainAnalyzerServiceImpl, MapperLayerServiceImpl      |
| 6  | assigned-chain             | 🆕 新建 | `assigned-chain`                | MethodGeneratorServiceImpl, DesignServiceImpl              |
| 7  | select-one-property-map    | 🆕 新建 | `select-one-property-map`       | MethodGeneratorServiceImpl                                 |
| 8  | default-method-name        | 🆕 新建 | `default-method-name`           | QueryChainAnalyzerServiceImpl                              |
| 9  | page-with-param-dto        | 🆕 新建 | `page-with-param-dto`           | MethodGeneratorServiceImpl, TransformMethodCallServiceImpl |
| 10 | soft-delete-in-query       | 🆕 新建 | `soft-delete-in-query`          | MapperLayerServiceImpl                                     |
| 11 | delete-with-in-operator    | 🆕 新建 | `delete-with-in-operator`       | MapperLayerServiceImpl                                     |
| 12 | select-with-order-and-join | 🆕 新建 | `select-with-order-and-join`    | MapperLayerServiceImpl                                     |
