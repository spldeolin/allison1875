# query-transformer / select-basic

## 覆盖功能

本 IT case 验证 query-transformer 的**基本 SELECT 查询**功能，覆盖以下关键点：

### 1. 三种终止方法（Termination Methods）

| 终止方法      | DSL 示例                                                                   | 生成的 SQL 特征           |
|-----------|--------------------------------------------------------------------------|----------------------|
| `one()`   | `TOrderDesign.select("findById").where().id.eq(id).one()`                | SELECT ... LIMIT 1   |
| `list()`  | `TOrderDesign.select("listAll").list()`                                  | SELECT ... (返回 List) |
| `count()` | `TOrderDesign.select("countByUserId").where().userId.eq(userId).count()` | SELECT COUNT(*)      |

### 2. WHERE 条件

- 使用 `.where().propertyName.eq(value)` 格式声明等值条件
- 条件通过 `<if test="...">` 包裹生成到 Mapper XML 中
- 验证了 `id` 和 `userId` 两个字段的条件绑定

### 3. Design 链替换为 Mapper 调用

- `TOrderDesign.select(...).where()...one()` → `tOrderMapper.findById(id)`
- `TOrderDesign.select(...).list()` → `tOrderMapper.listAll()`
- `TOrderDesign.select(...).where()...count()` → `tOrderMapper.countByUserId(userId)`
- 自动注入 `@Resource private TOrderMapper tOrderMapper` 字段
- import 语句自动更新（移除 Design 相关 import，添加 Mapper 相关 import）

### 4. Mapper 方法和 XML SQL 的生成

- 在 Mapper 接口中生成对应方法签名（含 `@Param` 注解）
- 在 Mapper XML 中追加 `<select>` 语句
- 方法名来源于 `select("methodName")` 中声明的字符串

## 执行流程

```
allison1875:persistence-generator  →  生成 Entity/Design/Mapper/XML
         ↓
process-classes + compile          →  编译 Design 类 + 拷贝 java-qt 源码
         ↓
allison1875:query-transformer      →  解析 Design DSL 链 → 转换为 Mapper 调用
```

## 验证点

1. **Mapper 接口** — 包含 `findById`、`listAll`、`countByUserId` 方法
2. **Mapper XML** — 包含对应 SQL（LIMIT 1、COUNT(*)、user_id 条件）
3. **Service 文件** — Design 链被替换为 Mapper 调用，`TOrderDesign` 不再出现
