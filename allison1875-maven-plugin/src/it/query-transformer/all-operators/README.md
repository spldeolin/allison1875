# query-transformer / all-operators

## 覆盖功能

本 IT case 验证 query-transformer 对**所有 11 种比较运算符**的 SQL 生成正确性。

### 运算符与生成 SQL 对照表

| DSL 方法             | 枚举值               | 生成的 SQL 片段                             | 测试方法             |
|--------------------|-------------------|----------------------------------------|------------------|
| `.eq(value)`       | EQUALS            | `column = #{var}`                      | `queryByEq`      |
| `.ne(value)`       | NOT_EQUALS        | `column != #{var}`                     | `queryByNe`      |
| `.gt(value)`       | GREATER_THEN      | `column > #{var}`                      | `queryByGt`      |
| `.ge(value)`       | GREATER_OR_EQUALS | `column >= #{var}`                     | `queryByGe`      |
| `.lt(value)`       | LESS_THEN         | `column &lt; #{var}`                   | `queryByLt`      |
| `.le(value)`       | LESS_OR_EQUALS    | `column &lt;= #{var}`                  | `queryByLe`      |
| `.like(value)`     | LIKE              | `column LIKE CONCAT('%', #{var}, '%')` | `queryByLike`    |
| `.in(collection)`  | IN                | `column IN (<foreach ...>)`            | `queryByIn`      |
| `.nin(collection)` | NOT_IN            | `column NOT IN (<foreach ...>)`        | `queryByNin`     |
| `.notnull()`       | NOT_NULL          | `column IS NOT NULL`                   | `queryByNotnull` |
| `.isnull()`        | IS_NULL           | `column IS NULL`                       | `queryByIsnull`  |

### 验证的字段类型多样性

- `Long` 类型字段 (`id`) — eq
- `Byte` 类型字段 (`status`) — ne / in / nin
- `BigDecimal` 类型字段 (`amount`) — gt / ge / lt / le
- `String` 类型字段 (`orderNo`) — like
- 可空 `String` 类型字段 (`remark`) — notnull / isnull

### 额外覆盖点

1. **`<if test>` 动态条件包裹** — 非 forced 模式下，有值参数的运算符（eq/ne/gt/ge/lt/le/like）使用
   `<if test="var != null">` 包裹
2. **IN/NOT IN 的空集保护** — `in` 运算符生成 `<if test="var.size() > 0">` +
   `<if test="var.size() == 0">AND 1 != 1</if>` 防止空集 SQL 错误
3. **notnull/isnull 无条件包裹** — 这两个运算符不需要参数，直接生成 SQL，无 `<if>` 包裹
4. **XML 特殊字符转义** — `<` 和 `<=` 在 XML 中转义为 `&lt;` 和 `&lt;=`

## 执行流程

```
allison1875:persistence-generator  →  生成 Entity/Design/Mapper/XML
         ↓
process-classes + compile          →  编译 Design 类 + 拷贝 java-qt 源码
         ↓
allison1875:query-transformer      →  解析 Design DSL 链 → 转换为 Mapper 调用
```

## 验证点

1. **Mapper XML** — 每个运算符对应的 SQL 片段存在且格式正确
2. **Mapper 接口** — 11 个查询方法全部生成
3. **Service 文件** — Design 链全部被替换为 Mapper 调用，无 `TOrderDesign` 残留
