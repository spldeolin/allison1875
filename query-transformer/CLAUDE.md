# Query-Transformer — AI Coding 规约

## DSL 赋值规则

Query-Transformer 对 Design chain DSL 存在**编译时类型 vs 转换时类型**的区分，这是最常见的 AI 误用点。

**编译时类型：** Design chain 方法（如 `.list()`）始终返回 `List<TXxxEntity>`，因为 Design 类是编译时桩。

**转换时类型：** query-transformer 根据选中的属性在 AST 转换阶段推导实际返回类型。

| 场景       | DSL 代码                                                              | 编译时类型                    | 转换结果                      |
|----------|---------------------------------------------------------------------|--------------------------|---------------------------|
| 无赋值（单属性） | `TOrderDesign.select("xxx").orderNo.list();`                        | `List<TOrderEntity>`（丢弃） | `List<String>`            |
| 无赋值（多属性） | `TOrderDesign.select("xxx").orderNo.userId.list();`                 | `List<TOrderEntity>`（丢弃） | `List<XxxRecord>`（生成 DTO） |
| 有赋值      | `List<TOrderEntity> r = TOrderDesign.select("xxx").orderNo.list();` | `List<TOrderEntity>` ✓   | `List<TOrderEntity>`（保留）  |
| **非法**   | `List<String> r = TOrderDesign.select("xxx").orderNo.list();`       | 编译错误                     | —                         |

**关键规则：** 若希望 transformer 推导非 Entity 返回类型（单属性 → `List<String>`，多属性 → `List<Record>`），DSL 语句**必须不赋值
**。赋值会强制保留编译时类型 `List<TXxxEntity>`。
