# persistence-generator 集成测试用例说明

本目录包含 persistence-generator 工具的全部集成测试用例。每个测试通过基类 `PersistenceGeneratorItBaseTest` 将测试资源（含
DDL 文件和 `.allison1875.yml`）拷贝到临时目录、改写配置为绝对路径，然后调用 `Bootstrap.main()` 执行
persistence-generator，最后在生成的 Entity、Mapper 接口、Mapper XML、Design 文件上进行断言。

persistence-generator 的核心职责是：解析 DDL 表结构，自动生成 Entity 类、Mapper 接口（含 CRUD 及索引查询方法）、Mapper XML（含
SQL 语句）、Design 文件。

---

## 基本功能

### BasicDdlItTest

- 验证标准 DDL 转换的完整流程
- 生成 Entity 文件，包含所有字段及正确的 Java 类型映射（`Long`、`String`、`BigDecimal`、`LocalDateTime`）
- 生成 Mapper 接口，包含基础 CRUD 方法（`insert`、`batchInsert`、`updateById`、`deleteById`、`queryById`、`queryByIds`）
- 根据索引自动生成查询方法（唯一索引 → `queryByOrderNo`，普通索引 → `queryByUserId`）
- 生成 Mapper XML，包含 `resultMap`、表名引用、列名映射和全部 SQL 方法
- 生成 Design 文件

---

## 列类型映射

### AllColumnTypesItTest

- 验证所有 MySQL 列类型到 Java 类型的正确映射
- `tinyint(1)` → `Boolean`，`tinyint` → `Byte`，`int` → `Integer`，`bigint` → `Long`
- `double` → `Double`，`decimal` → `BigDecimal`
- `varchar`/`char`/`text`/`mediumtext` → `String`
- `date` → `LocalDate`，`time` → `LocalTime`，`datetime`/`timestamp` → `LocalDateTime`
- XML 中所有列均正确出现

---

## 主键与索引策略

### NoPrimaryKeyItTest

- 验证无主键的表不生成 `queryById`、`updateById`、`deleteById`、`queryByIds` 方法
- 仍生成 `insert`、`batchInsert`
- 无索引时生成 `listAll` 方法
- XML 的 `resultMap` 中不包含 `<id>` 标签

### CompositePrimaryKeyItTest

- 验证复合主键表的处理
- `queryById` 和 `deleteById` 使用 `@Param` 分别传入各主键列
- 不生成 `queryByIds` 和 `queryByIdsEachId`（复合主键不适用）

### CompositeIndexItTest

- 验证复合索引自动生成多种组合查询方法
- 三列联合索引生成最左前缀的所有组合（如 `queryByWarehouseId`、`queryByWarehouseIdProductId`、
  `queryByWarehouseIdProductIdSkuCode`）
- 非唯一索引查询返回 `List`
- 同时生成对应的 `deleteByXxx` 方法
- 有索引的表不生成 `listAll`

### NoIndexListAllItTest

- 验证无索引（仅有主键）的表生成 `listAll` 方法
- 不生成按列名查询的方法

### AllNotNullItTest

- 验证所有字段均 NOT NULL 时，不生成 `updateByIdEvenNull` 方法
- 仍正常生成 `updateById`、`insert`、`queryById`、`deleteById`

---

## 逻辑删除

### SoftDeleteItTest

- 验证配置逻辑删除列后，`deleteById` 在 XML 中使用 `<update>` 标签（`SET is_deleted = 1`）而非 `<delete>`
- 所有查询方法自动附加 `is_deleted = 0` 条件
- 索引相关的 `deleteByXxx` 同样使用 `<update>` 标签

---

## 多表处理

### MultiTableItTest

- 验证 DDL 包含多张表时，每张表独立生成 Entity、Mapper、XML、Design 文件
- 各表的字段、类型、索引查询方法互不干扰

---

## 已有文件的增量更新

### ExistingMapperItTest

- 验证 Mapper 接口文件已存在时的增量更新
- 已有的自定义方法（如 `queryByBalanceRange`、`sumBalance`）被保留
- 生成的基础 CRUD 方法正常加入
- 自定义方法排列在生成方法之后

### ExistingXmlMarkersItTest

- 验证 Mapper XML 文件已存在且含有 `[START]`/`[END]` 标记时的增量更新
- 标记范围内的旧生成内容被清除，替换为新内容
- 标记范围外的自定义 SQL（如 `customQueryByStatus`）被保留

---

## 生成选项

### DisableDesignItTest

- 验证 `enableGenerateDesign=false` 时，不生成 Design 文件
- Entity、Mapper、XML 仍正常生成

### OffsetLimitPageStyleItTest

- 验证 `pageParamStyle=OFFSET_LIMIT` 配置的效果
- Design 文件中分页方法使用 `offset`/`limit` 参数
- 不使用 `pageNo`/`pageSize` 参数

### EntityNoSuffixItTest

- 验证 `isEntityEndWithEntity=false` 时，Entity 类名不带 `Entity` 后缀
- 如 `TCategory`（而非 `TCategoryEntity`）
- Mapper 接口和 XML 中的类型引用也使用不带后缀的名称

### NoModifyAnnounceOffItTest

- 验证 `enableNoModifyAnnounce=false` 时，生成的 Entity、Mapper、XML 中不包含「Any modifications may be overwritten...」声明

### NoLombokItTest

- 验证 `isEntityWithoutLombok=true` 时，Entity 包含手写 getter/setter 方法
- 不包含 `@Data`、`@Accessors`、`@FieldDefaults` 等 Lombok 注解

---

## MySQL jdbcUrl 直连

以下两个测试用例通过 `Config.jdbcUrl` 直连 MySQL 数据库，查询 `information_schema` 获取表结构（替代 DDL 内存 H2 方式），
验证 persistence-generator 的 jdbcUrl 数据源路径。

这些测试依赖系统属性 `-Dmysql.host` / `-Dmysql.port` / `-Dmysql.user` / `-Dmysql.password` / `-Dmysql.database`，
参数不全时自动跳过（`Assumptions`）。测试由 `PersistenceGeneratorMySqlItBaseTest` 驱动，在测试前自动创建表、测试后自动清理。

### BasicJdbcUrlMysqlItTest

- 验证通过 jdbcUrl 直连 MySQL 的单表完整流程
- 测试表：`t_order`（含主键、唯一索引 `uk_order_no`、普通索引 `idx_user_id`）
- 生成的 Entity/Mapper/XML/Design 与 BasicDdlItTest 的断言一致
- 类型映射：`bigint` → `Long`，`varchar` → `String`，`decimal` → `BigDecimal`，`datetime` → `LocalDateTime`，`tinyint` → `Byte`

### JdbcUrlMysqlItTest

- 验证通过 jdbcUrl 直连 MySQL 的多表独立生成能力
- 测试表：`t_user`（唯一索引）、`t_product`（普通索引）、`t_order_item`（多索引）
- 各表独立生成 Entity、Mapper、XML、Design 文件，互不干扰
- 断言每表的字段、类型、索引查询方法和 Design 文件

### 实现架构

- 基类 `PersistenceGeneratorMySqlItBaseTest extends PersistenceGeneratorItBaseTest`：
  - 读取系统属性构建 `jdbcUrl`，参数不全时通过 `Assumptions.assumeTrue` 跳过
  - 子类通过 `ddls()` 返回 DDL 列表，基类负责创建表 / 注入配置 / 清理表
  - 将 `jdbcUrl`、`userName`、`password`、`schema` 注入 `.allison1875.yml`，同时移除 `ddl` 字段
- `MysqlConnectionItTest`（独立连接验证）：验证 GitHub workflow 中 MySQL 服务可用

---

## 当前覆盖率概况（JaCoCo）

| 类                          | 指令覆盖率   | 分支覆盖率   |
|----------------------------|---------|---------|
| PersistenceGenerator       | 97%     | 92%     |
| TableAnalyzerServiceImpl   | **57%** | **63%** |
| EntityGeneratorServiceImpl | 93%     | **82%** |
| MapperCoidServiceImpl      | 98%     | 93%     |
| MapperXmlServiceImpl       | 98%     | 91%     |
| DesignGeneratorServiceImpl | 99%     | 100%    |

---

## 待补充 case

### default-values

- **目标覆盖**：`EntityGeneratorServiceImpl.buildFieldInitExpr()` 的各类型默认值分支（分支覆盖 82%→提高）
- DDL 中为多种类型字段设置默认值：`DEFAULT 0`（Integer）、`DEFAULT ''`（String）、`DEFAULT 0`（Long）、`DEFAULT 0`
  （Boolean/tinyint(1)）、`DEFAULT 0.00`（BigDecimal）
- 验证生成的 Entity 字段具有对应的初始化表达式

### longtext-column

- **目标覆盖**：`TableAnalyzerServiceImpl.jdbcType2javaType()` 中 `longtext` 分支
- DDL 包含 `longtext` 类型列，验证映射为 `String`
- `all-column-types` 目前未覆盖 `longtext`

### biz-id-unique-index

- **目标覆盖**：`PersistenceGenerator.process()` 中 `index.isBizId()` 为 true 的分支、
  `MapperCoidServiceImpl.generateQueryByBizIdsMethodToMapper()` 和 `generateQueryByBizIdsEachIdMethodToMapper()`
- DDL 包含单字段唯一索引（非主键，如 `uk_order_no(order_no)`），该索引满足 `isBizId()=true`（单字段 + 唯一）
- 验证额外生成 `queryByOrderNos` 和 `queryByOrderNosEachOrderNo` 方法及对应 XML

### entity-existence-rename

- **目标覆盖**：`entityExistenceResolution: RENAME` 分支
- 项目中已存在同名 Entity 文件，配置 `entityExistenceResolution: RENAME`
- 验证旧文件被重命名（而非覆盖），新文件正常生成

### jakarta-validation

- **目标覆盖**：`enableJavaxMoveToJakarta: true` 分支
- 验证生成的代码中 `javax.*` 命名空间被替换为 `jakarta.*`

### unsupported-column-type

- **目标覆盖**：`TableAnalyzerServiceImpl.jdbcType2javaType()` 返回 null 的分支（log.warn "unsupport jbdcType"）
- DDL 包含一个不在映射表中的列类型（如 `blob`、`binary`、`json` 等）
- 验证工具打印警告日志但不中断，其他字段正常生成

### overlapping-flatten-indices

- **目标覆盖**：`TableAnalyzerServiceImpl.analyzeTable()` 中索引平铺时的去重与唯一性合并逻辑（
  `flattenIndices.contains(flattenIndex)` 为 true 且 `isUnique` 为 true 的分支）
- DDL 包含两个索引，其中一个索引的前缀列与另一个索引完全重叠，且重叠部分在其中一个索引中是唯一的。例如 `UNIQUE KEY uk_a(a)`
  和 `KEY idx_ab(a, b)`
- 验证平铺后 `(a)` 索引被正确标记为唯一，不会重复生成查询方法

### soft-delete-with-composite-pk

- **目标覆盖**：`MapperXmlServiceImpl.generateDeleteByIdMethod()` 中 soft-delete + 复合主键组合路径
- DDL 为复合主键表，同时配置 `deletedSql` / `notDeletedSql`
- 验证 deleteById 生成为多参数 UPDATE、查询加 notDeletedSql、不生成 queryByIds / queryByIdsEachId

### not-deleted-sql-no-equation

- **目标覆盖**：`TableAnalyzerServiceImpl.getDeleteFlagName()` 中 `notDeletedSql` 不含等号的分支（log.warn 并忽略）
- 配置 `notDeletedSql: "is_deleted IS NULL"` 和 `deletedSql: "is_deleted = NOW()"`（notDeletedSql 不含 `=`）
- DDL 含 `is_deleted` 字段，验证 `isDeleteFlagExist` 为 false（因为无法识别字段名），按无软删除逻辑生成
