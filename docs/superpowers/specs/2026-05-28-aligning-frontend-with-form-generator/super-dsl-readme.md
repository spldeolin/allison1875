# Super DSL Specification - Form Generator All Dimensions

本文档整理了 form-generator 的 DSL 所有维度，用于"超级 DSL"(YAML) 的设计，以覆盖 form-generator 的完整功能集。

## 一、FormDef 顶层字段

FormDef 是表单定义的顶层类，包含以下字段：

| 字段名 | 类型 | 说明 | 必填 |
|--------|------|------|------|
| name | String | 表单名称（upperCamel） | ✓ |
| title | String | 表单标题（用户可见） | ✓ |
| desc | String | 表单描述（用户可见） | ✗ |
| items | List<ItemDef> | 表单字段列表 | ✓ |
| indices | List<IndexDef> | 数据库索引列表 | ✗ |

## 二、ItemDef 通用字段（所有字段类型都继承）

| 字段名 | 类型 | 说明 | 默认值 |
|--------|------|------|--------|
| name | String | 字段名称（lowCamel） | - |
| title | String | 字段标题 | - |
| isNonVoid | Boolean | 字段是否非空（广义） | - |
| canInputOnInit | Boolean | 初始化模式 | USER_INPUT |
| canInputOnEdit | Boolean | 编辑模式 | USER_INPUT |
| type | ItemType | 字段类型（子类实现） | - |

## 三、ItemType 及其子类特有字段

### 3.1 number (NumberItemDef)

| 字段名 | 类型 | 说明 | 默认值 |
|--------|------|------|--------|
| canBeDecimal | Boolean | 是否可以是小数 | false |

### 3.2 onOff (OnOffItemDef)

无特有字段（简单开关）

### 3.3 secret (SecretItemDef)

无特有字段（密码类字段，特点是不能返回、DB需加密保存、不能编辑、只能重置）

### 3.4 select (SelectItemDef)

| 字段名 | 类型 | 说明 | 必填 |
|--------|------|------|------|
| options | List<OptionDef> | 可选项列表 | ✓ |

OptionDef 结构：
- code (String, 必填): 可选项唯一标示，最多64字符
- title (String, 必填): 可选项标题

### 3.5 multiSelect (MultiSelectItemDef)

| 字段名 | 类型 | 说明 | 必填 |
|--------|------|------|------|
| options | List<OptionDef> | 可选项列表 | ✓ |

**YAML DSL 格式注意**：
- 在 ItemDef 的 @JsonSubTypes 注解中，multiSelect 映射为独立的 type="multiSelect"（不是 type="select" + is_multi=true）
- 这是因为 MultiSelectItemDef 是一个独立的子类，而非 SelectItemDef 的变体

### 3.6 text (TextItemDef)

| 字段名 | 类型 | 说明 | 默认值 |
|--------|------|------|--------|
| isMultilineOrRich | Boolean | 是否多行文本或富文本 | false |
| maxLength | Integer | 单行文本最大长度（1-65535） | 255 |
| regex | String | 正则表达式匹配约束 | null |

### 3.7 time (TimeItemDef)

| 字段名 | 类型 | 说明 | 默认值 |
|--------|------|------|--------|
| format | TimeFormat | 时间格式 | DATE_TIME |

## 四、IndexDef 字段

| 字段名 | 类型 | 说明 | 默认值 |
|--------|------|------|--------|
| itemNames | List<String> | 组成索引的字段名称列表 | - |
| isUnique | Boolean | 是否为唯一索引 | false |

## 五、枚举取值

### 5.1 Boolean 取值

初始化模式和编辑模式的可选值：

| 枚举值 | Code | 说明 |
|--------|------|------|
| DO_NOT | "doNot" | 不进行初始化或不可编辑 |
| USER_INPUT | "userInput" | 用户输入 |
| TODO | "todo" | 生成为 TODO，由开发者自行开发 |

### 5.2 FilterPattern 取值

文本类字段作为过滤条件时支持的过滤方式：

| 枚举值 | Code | 说明 |
|--------|------|------|
| IN | "in" | 在范围内或与…有交集，单元素时等价于"等于" |
| GE | "ge" | 大于等于 |
| GT | "gt" | 大于（注：注释说"小于等于"有误，应为"大于"） |
| LE | "le" | 小于等于 |
| LT | "lt" | 小于 |
| LIKE | "like" | 左右模糊匹配 |
| DATE_RANGE | "dateRange" | 开始日期和结束日期范围 |
| DATE_TIME_RANGE | "dateTimeRange" | 开始时间和结束时间范围 |

### 5.3 TimeFormat 取值

时间类字段的格式：

| 枚举值 | Code | Java 类型 | 时间格式 |
|--------|------|-----------|---------|
| DATE | "date" | java.time.LocalDate | yyyy-MM-dd |
| TIME | "time" | java.time.LocalTime | HH:mm:ss |
| DATE_TIME | "dateTime" | java.time.LocalDateTime | yyyy-MM-dd HH:mm:ss |

### 5.4 SpecialItemType 取值

特殊字段类型（null 表示非特殊字段）：

| 枚举值 | 说明 |
|--------|------|
| BIZ_ID | 业务主键 |
| CREATED_AT | 创建时间 |
| UPDATED_AT | 更新时间 |

### 5.5 ApiType 取值

API 类型（用于 form-generator 生成的后端接口）：

| 枚举值 | Code | 说明 |
|--------|------|------|
| SAVE | "save" | 保存接口 |
| LIST | "list" | 列表接口 |
| GET_DETAIL | "getDetail" | 详情接口 |
| DELETE | "delete" | 删除接口 |

## 六、其他重要维度

### 6.1 ItemType 完整列表

在 YAML DSL 中使用 type 字段指定，共 7 种：

1. **number** - 数字字段
2. **onOff** - 开关字段（只有 True/False）
3. **secret** - 密码/密钥字段
4. **select** - 选择字段（单选）
5. **multiSelect** - 多选字段（创建关联表）
6. **text** - 文本字段
7. **time** - 时间字段

### 6.2 自动注入/特殊处理

- FormDef 的前两个 item 被认为是"审计字段"，不包含在 getNonAuditedItems() 中
- 业务主键（BIZ_ID）通常是 items 列表的第一个元素
- 后两个 item 通常是 CREATED_AT 和 UPDATED_AT

## 七、YAML DSL 示例框架

```yaml
name: YourFormName
title: Your Form Title
desc: Optional form description

items:
  # Item 1: Business ID (typically)
  - name: itemId
    title: Item ID
    type: number
    isNonVoid: true
    canInputOnInit: false      # or: true, false
    canInputOnEdit: false
    canBeDecimal: false

  # Item 2: Example text field
  - name: itemName
    title: Item Name
    type: text
    isNonVoid: true
    canInputOnInit: true
    canInputOnEdit: true
    isMultilineOrRich: false
    maxLength: 255
    regex: null

  # Item 3: Example select field
  - name: itemStatus
    title: Item Status
    type: select
    isNonVoid: true
    canInputOnInit: true
    canInputOnEdit: true
    options:
      - code: active
        title: Active
      - code: inactive
        title: Inactive

  # Item 4: Example multiSelect field
  - name: itemTags
    title: Item Tags
    type: multiSelect
    isNonVoid: false
    canInputOnInit: true
    canInputOnEdit: true
    options:
      - code: tag1
        title: Tag 1
      - code: tag2
        title: Tag 2

  # Item N-1: Created At (audit field)
  - name: createdAt
    title: Created At
    type: time
    isNonVoid: true
    canInputOnInit: false
    canInputOnEdit: false
    format: dateTime

  # Item N: Updated At (audit field)
  - name: updatedAt
    title: Updated At
    type: time
    isNonVoid: true
    canInputOnInit: false
    canInputOnEdit: false
    format: dateTime

indices:
  - itemNames: [itemId]
    isUnique: true
  - itemNames: [itemStatus, itemName]
    isUnique: false
```

## 八、维度对齐清单

以下维度都已被 form-generator 支持，frontend-skeleton 需要与其对齐：

- ✓ 所有 7 种 ItemType 及其特有字段
- ✓ 所有 Boolean 取值（doNot/userInput/todo）
- ✓ FilterPattern（用于搜索/过滤条件）
- ✓ TimeFormat（用于时间字段）
- ✓ SpecialItemType（业务主键、审计字段）
- ✓ IndexDef（数据库索引）
- ✓ OptionDef（select/multiSelect 的选项）
- ✓ ApiType（后端 API 类型）
- ✓ isNonVoid（字段约束）

---

**文档生成日期**: 2026-05-28  
**来源**: form-generator DSL 类分析  
**Phase**: A1 - Research

## Form 覆盖矩阵

以下表格展示了 super-dsl.yml 中 3 个 form 对各维度的覆盖情况：

| 维度 | StudentProfile | StudentDormitory | StudentExam |
|---|---|---|---|
| **ItemType 覆盖** | | | |
| text (短文本) | studentName (max=50), idCard (regex) | — | — |
| text (多行文本) | bio (multiline, max=2000) | — | — |
| number (整数) | age | studentId, monthlyRent (decimal) | studentId, score (decimal) |
| number (小数) | — | monthlyRent | score |
| select (单选) | gender (2 options) | — | subject (2 options) |
| multiSelect | — | facilities (3 options) | — |
| onOff | — | hasAllergy | — |
| secret | — | doorPassword, emergencyContact | — |
| time (date) | — | — | examDate |
| time (time) | — | — | examTime |
| time (dateTime) | — | — | submittedAt |
| **InitPattern/EditPattern** | | | |
| userInput / userInput | studentName, idCard, bio, age | studentId, hasAllergy, facilities, doorPassword, monthlyRent | studentId, subject, score, examDate, examTime, submittedAt |
| userInput / doNot | gender | emergencyContact | — |
| todo / * | — | emergencyContact (todo/doNot) | — |
| **isNonVoid** | | | |
| true | studentName, idCard, age, gender | studentId, hasAllergy, doorPassword, emergencyContact, monthlyRent | studentId, subject, score, examDate, examTime, submittedAt |
| false | bio | facilities | — |
| **索引类型** | | | |
| 单字段唯一索引 | idCard | studentId | — |
| 复合唯一索引 | — | — | (studentId, subject, examDate) |
| **Text 特殊约束** | | | |
| maxLength | studentName (50), bio (2000), idCard (18) | — | — |
| regex | idCard (^\d{18}$) | — | — |
| isMultilineOrRich | bio (true) | — | — |
| **Number 特殊约束** | | | |
| canBeDecimal=false | age | studentId | studentId |
| canBeDecimal=true | — | monthlyRent | score |

## 未覆盖维度说明

以下维度在 3 个 form 中未被覆盖，原因如下：

### 1. FilterPattern 相关维度（in/ge/gt/le/lt/like/dateRange/dateTimeRange）

**未覆盖原因**: 经过对 form-generator Java 源码的分析，发现 ItemDef 及其子类（NumberItemDef、TextItemDef、SelectItemDef、MultiSelectItemDef、TimeItemDef、SecretItemDef、OnOffItemDef）**均不包含 filterPatterns 字段**。

- study-dsl.yml 中的 `filter_patterns` 字段在实际的 Java 模型中不存在
- FilterPattern 枚举虽然存在于代码库中，但它不是 DSL schema 的一部分
- 该字段可能是前端或其他模块的设计，不属于 form-generator 的 DSL 规范

因此，super-dsl.yml **不包含任何 filter_patterns 字段**。

### 2. checkDuplicate 字段

**未覆盖原因**: 经源码分析，ItemDef 及其子类中**不存在 checkDuplicate 字段**。study-dsl.yml 中出现的此字段在实际 Java 模型中未定义，因此未在 super-dsl.yml 中使用。

### 3. canBeNegative 字段（number 类型）

**未覆盖原因**: NumberItemDef 只包含 `canBeDecimal` 字段，**不包含 canBeNegative 字段**。study-dsl.yml 中的 `can_be_negative` 在实际模型中不存在。

负数支持可能通过其他机制（如正则表达式或业务逻辑验证）实现，但不是 DSL schema 的一部分。

### 4. secret 的 displayType 和 desensitization 字段

**未覆盖原因**: SecretItemDef 类**没有任何特有字段**，只继承 ItemDef 的通用字段（name、title、isNonVoid、canInputOnInit、canInputOnEdit）。

- study-dsl.yml 中的 `display_type` 和 `desensitization` 字段在 Java 模型中不存在
- secret 类型的特殊行为（不能返回、DB 加密、只能重置）是由 form-generator 的代码逻辑控制，而非 DSL 配置

### 5. SpecialItemType（BIZ_ID/CREATED_AT/UPDATED_AT）

**未覆盖原因**: 根据 README 说明，这些特殊字段由 **form-generator 自动注入**，不需要在 DSL 中显式声明。

- BIZ_ID 通常是 items 列表的第一个元素
- CREATED_AT 和 UPDATED_AT 是审计字段，自动追加到表单末尾
- 在 DSL 中手动添加这些字段反而可能与自动注入逻辑冲突

### 6. select + like 组合（文本搜索的选择字段）

**未覆盖原因**: 
1. SelectItemDef 和 MultiSelectItemDef 不包含 filterPatterns 字段（见第 1 点）
2. select/multiSelect 字段的 filter 语义是 "in"（在范围内），与 "like"（模糊匹配）语义冲突
3. 此组合不在 form-generator 的设计范围内

### 7. time 字段的 FilterPattern（dateRange/dateTimeRange）

**未覆盖原因**: TimeItemDef 不包含 filterPatterns 字段（见第 1 点）。时间范围过滤可能由前端或查询构建器单独处理，不属于 DSL 规范。

## 重要发现：study-dsl.yml 与实际 Java 模型的差异

在分析过程中发现，`study-dsl.yml` 包含了多个**在 form-generator Java 模型中不存在的字段**：

1. `filter_patterns` - 所有 ItemDef 子类均无此字段
2. `check_duplicate` - ItemDef 中无此字段
3. `can_be_negative` - NumberItemDef 无此字段
4. `display_type` / `desensitization` - SecretItemDef 无此字段
5. `is_multi` - study-dsl.yml 第 77 行使用了 `is_multi: true`，但正确做法应该是 `type: multiSelect`（独立类型，不是 select 的变体）

**结论**: super-dsl.yml 严格基于 form-generator 的实际 Java 模型创建，**只包含已验证存在的字段**：

- 通用字段: name, title, isNonVoid, canInputOnInit, canInputOnEdit
- NumberItemDef: canBeDecimal
- TextItemDef: isMultilineOrRich, maxLength, regex
- TimeItemDef: format (注意是 format 不是 pattern)
- SelectItemDef / MultiSelectItemDef: options
- SecretItemDef / OnOffItemDef: 无特有字段

---

**更新日期**: 2026-05-28  
**Phase**: A2 - Implementation
