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

### SelectOnePropertyMapItTest

- 验证单属性 + `mapBy` 终结时生成 Record DTO 类型的 Map
- 返回 `Map<KeyType, XxxRecord>`，Record DTO 包含指定的单属性字段

---

## UPDATE / DELETE

### UpdateDeleteItTest

- 验证 UPDATE 和 DELETE DSL 链
- UPDATE 单字段（`SET status = #{status}`）和多字段（`SET amount, remark`）
- DELETE 按 ID 和按条件
- Mapper 接口方法返回 `int`
- XML 中生成 `UPDATE t_order` / `DELETE FROM t_order` 语句

### DeleteWithInOperatorItTest

- 验证 DELETE 使用 IN 运算符时 `<delete>` 标签不含 `parameterType` 属性
- DSL：`TOrderDesign.delete("xxx").where().id.in(ids).over()`

---

## 强制条件（whereEvenNull）

### WhereForcedItTest

- 验证 `.whereEvenNull()` 强制条件模式
- XML 中条件不包裹 `<if test>`，直接输出 `AND column = #{var}`
- 适用于 SELECT 和 DELETE
- 覆盖全部比较运算符（eq/ne/gt/ge/lt/le/like/in/nin）与 `whereEvenNull()` 组合
- 每种运算符均验证：无条件 test 包裹、直接输出对应 SQL 运算符

---

## 参数阈值（ParamDTO 自动生成）

### ParamThresholdItTest

- 验证 WHERE 条件超过 3 个时自动生成 ParamDTO
- ParamDTO 生成在 `dto/param/` 包下，包含所有 WHERE 条件字段
- Mapper 接口方法参数为单个 ParamDTO 类型（不使用 `@Param`）
- Service 中包含 ParamDTO 构建代码（setter 链调用）

### PageWithParamDtoItTest

- 验证 `page()` + 2 个 WHERE 条件时 PAGE 降低阈值生成 ParamDTO（`binaries.size() > 1 && returnStyle==PAGE`）
- 生成的 ParamDTO 包含 `offset`/`limit` 分页字段
- Service 中包含 ParamDTO 的 setter 调用和 offset/limit 赋值

---

## JOIN 查询

### JoinBasicItTest

- 验证基本 LEFT JOIN 查询功能
- XML 中包含 `LEFT JOIN t_user` 和 `t1`/`t2` 表别名
- 生成 Record DTO，包含被 join 表的字段（如 `userName`）
- Service 中 Design 链被替换为 Mapper 调用

### JoinOtherTypesItTest

- 验证 LEFT JOIN 以外的其他 JOIN 类型
- `rightJoin()` → XML 出现 `RIGHT JOIN`
- `innerJoin()` → XML 出现 `INNER JOIN`
- `outerJoin()` → XML 出现 `OUTER JOIN`

### JoinMultiOnConditionsItTest

- 验证 JOIN 使用多个 ON 条件的场景（使用 `.on().open()...close()` 模式）
- XML ON 子句用括号 `(` `)` 包裹多个条件，每个条件占一行

### JoinOnOperatorsItTest

- 验证 JOIN ON 子句支持 eq 以外的比较运算符
- `ne()` → ON 子句出现 `!=`，`gt()` → `>`，`in()` → `IN (foreach)`

### JoinNoSelectPropertiesItTest

- 验证 JOIN + 不指定主表 select 属性时，生成主表全列
- XML SELECT 子句包含所有主表列（`t1.column_name AS fieldName` 格式）
- 同时包含 join 表（`t2.`）的列

### SelectWithOrderAndJoinItTest

- 验证 JOIN + select 属性 + ORDER BY 组合
- XML ORDER BY 列名带 `t1.` 前缀
- SELECT 子句混合主表和 join 表的列


## 赋值链

### AssignedChainItTest

- 验证 DSL 链赋值给变量（`isAssigned=true`）时的行为
- 返回 Entity 类型（不生成 Record DTO）
- Service 中替换后保留变量声明（如 `List<TOrderEntity> result = tOrderMapper.xxx(...)`）

---

## 默认方法名

### DefaultMethodNameItTest

- 验证 `select()`/`update()`/`delete()` 不传方法名参数时生成默认方法名
- `select()` → 默认方法名 `queryTOrder`
- `update()` → 默认方法名 `updateTOrder`
- `delete()` → 默认方法名 `deleteTOrder`

---

## 软删除查询

### SoftDeleteInQueryItTest

- 验证 persistence-generator 配置 `notDeletedSql` 后，query-transformer 自动追加软删除条件
- `.allison1875.yml` 中配置 `notDeletedSql: "is_deleted = 0"`
- XML 查询方法 WHERE 子句自动包含 `AND is_deleted = 0`
