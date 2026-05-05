# doc-analyzer / advanced-validation

## 本用例在验证什么

在 **仅导出 Markdown** 的前提下，检查 doc-analyzer 能否把「复杂 Bean Validation + 带 Javadoc 的请求/响应 DTO」正确写进接口文档。

## 功能点与分支关注点（逐条）

- **Markdown 产物形态**：生成 `api-docs` 目录，且整次运行只产出 **一个** Markdown 文件，文件名由控制器类上的中文说明推导为 **`支付管理.md`**（分类/模块命名路径）。
- **路由与方法**：从 `@RequestMapping("/api/payments")` + `@PostMapping` 解析出 **`POST /api/payments`**，并与方法 Javadoc「创建支付」一起出现在文档中。
- **字符串字段上的组合校验**：`@NotBlank` 与 Hibernate `@Length(min/max)` 同时存在时，文档中需出现对应的 **非空/长度上下限** 说明（与注解语义一致的中文表述）。
- **金额类字段上的数值校验**：`BigDecimal` 上 `@NotNull`、`@DecimalMin`、`@DecimalMax`、`@Digits(integer, fraction)` 组合时，文档需分别体现 **非 null、最小值、最大值、整数位/小数位上限**。
- **时间与整数上的标量校验**：`Date` 上的 `@Future`、`Integer` 上的 `@Positive` 需在文档中有 **未来时间、正数** 等对应说明。
- **集合元素上的 TYPE_USE 校验（重要分支）**：`List<@NotBlank @Length(max=20) String>` 这类「泛型实参上的注解」需被识别为 **列表元素** 的约束，而不是仅平铺为普通字段；文档中应出现 **tags**、元素级 **非空与最大长度**，且列表类型在文档中呈现为 **字符串数组** 一类表述。
- **请求体字段说明**：请求 DTO 各字段的 Javadoc（支付描述、支付金额、预计支付时间、支付笔数、标签列表等）需进入文档，与校验说明并存。
- **响应体结构**：响应 DTO 的字段名（如 `id`、`amount`、`status`）及 Javadoc（如「状态」）需在文档的响应部分体现。

## 小结

本用例专门覆盖 **javax Bean Validation + Hibernate Validator** 在「普通字段」与「`List<@...> T>` 元素约束」两条路径上的文档化能力，并顺带校验 **单控制器单接口** 时的 Markdown 文件命名与 HTTP 元数据是否正确。
