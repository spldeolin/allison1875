# Form Generator DSL 完整参考指南

## 概述

本文档详细说明 Form Generator 的 DSL（领域特定语言）定义，用于通过 YAML 配置生成完整的表单管理代码。

## YAML 文件结构

```yaml
forms:
  - name: FormName          # 表单定义
    title: 表单标题
    desc: 表单描述
    items: [...]           # 字段列表
    indices: [...]         # 索引列表
```

## 一、表单定义 (FormDef)

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 是 | 表单名称，UpperCamel 格式，无需 "Form" 后缀 |
| `title` | String | 是 | 表单标题，用户可见 |
| `desc` | String | 否 | 表单描述，用户可见 |
| `items` | List | 是 | 字段定义列表，至少一个 |
| `indices` | List | 否 | 索引定义列表 |

### 示例

```yaml
- name: Student
  title: 学生信息管理
  desc: 学生基本信息的录入、编辑和查询表单
  items: [...]
  indices: [...]
```

## 二、字段定义 (ItemDef)

### 通用属性

所有字段类型都包含以下通用属性：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `type` | Enum | 是 | - | 字段类型，见下文 |
| `name` | String | 是 | - | 字段名称，lowerCamel 格式 |
| `title` | String | 是 | - | 字段标题 |
| `is_non_valid` | Boolean | 是 | - | 是否必填（非空） |
| `init_pattern` | Enum | 否 | userInput | 初始化方式 |
| `edit_pattern` | Enum | 否 | userInput | 编辑方式 |

### InitOrEditPattern 枚举值

| 值 | 说明 |
|----|------|
| `userInput` | 用户输入 |
| `doNot` | 不进行初始化或不可编辑 |
| `todo` | 生成为 TODO，由开发者自行实现 |

## 三、字段类型 (ItemType)

### 3.1 TEXT - 文本类型

**类型标识**: `text`

**特有属性**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `is_multiline_or_rich` | Boolean | 否 | false | 是否多行文本或富文本 |
| `max_length` | Integer | 否 | 255 | 最大长度（≤65535） |
| `regex` | String | 否 | - | 正则表达式验证 |

**示例**:

```yaml
# 基础文本
- type: text
  name: userName
  title: 用户名
  is_non_valid: true
  max_length: 50
  regex: ^[a-zA-Z0-9_]{3,20}$

# 多行文本
- type: text
  name: description
  title: 描述
  is_non_valid: false
  is_multiline_or_rich: true
  max_length: 2000
```

**生成结果**:
- 数据库类型: `VARCHAR(max_length)` 或 `LONGTXT`（多行）
- Java 类型: `String`
- 支持过滤: IN, LIKE

---

### 3.2 NUMBER - 数字类型

**类型标识**: `number`

**特有属性**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `can_be_decimal` | Boolean | 否 | false | 是否可以是小数 |

**示例**:

```yaml
# 整数
- type: number
  name: age
  title: 年龄
  is_non_valid: true
  can_be_decimal: false

# 小数
- type: number
  name: price
  title: 价格
  is_non_valid: true
  can_be_decimal: true
```

**生成结果**:
- 数据库类型: `BIGINT`（整数）或 `DECIMAL(14, 4)`（小数）
- Java 类型: `Long`（整数）或 `java.math.BigDecimal`（小数）
- 支持过滤: IN, GE, GT, LE, LT
- 可排序: 是

---

### 3.3 SELECT - 单选类型

**类型标识**: `select`

**特有属性**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `options` | List | 是 | 可选项列表 |

**OptionDef 结构**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `code` | String | 是 | 选项唯一标识（≤64字符） |
| `title` | String | 是 | 选项标题 |

**示例**:

```yaml
- type: select
  name: status
  title: 状态
  is_non_valid: true
  options:
    - code: active
      title: 激活
    - code: inactive
      title: 未激活
    - code: suspended
      title: 暂停
```

**生成结果**:
- 数据库类型: `VARCHAR(64)`
- Java 类型: 枚举类（`{Name}Enum`）
- 支持过滤: IN
- 可排序: 否

---

### 3.4 MULTI_SELECT - 多选类型

**类型标识**: `multiSelect`

**特有属性**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `options` | List | 是 | 可选项列表（同 SELECT） |

**示例**:

```yaml
- type: multiSelect
  name: hobbies
  title: 兴趣爱好
  is_non_valid: false
  options:
    - code: reading
      title: 阅读
    - code: sports
      title: 运动
    - code: music
      title: 音乐
```

**生成结果**:
- 数据库类型: `VARCHAR(64)` + 关联表
- Java 类型: 枚举类（`{Name}Enum`）
- 支持过滤: IN
- 可排序: 否

---

### 3.5 TIME - 时间类型

**类型标识**: `time`

**特有属性**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `format` | Enum | 否 | dateTime | 时间格式 |

**TimeFormat 枚举值**:

| 值 | 格式 | Java 类型 |
|----|------|-----------|
| `date` | yyyy-MM-dd | java.time.LocalDate |
| `time` | HH:mm:ss | java.time.LocalTime |
| `dateTime` | yyyy-MM-dd HH:mm:ss | java.time.LocalDateTime |

**示例**:

```yaml
# 日期
- type: time
  name: birthDate
  title: 出生日期
  is_non_valid: true
  format: date

# 日期时间
- type: time
  name: createdAt
  title: 创建时间
  is_non_valid: true
  format: dateTime
```

**生成结果**:
- 数据库类型: `DATETIME`
- Java 类型: 根据 format 决定
- 支持过滤: IN, DATE_RANGE, DATE_TIME_RANGE
- 可排序: 是
- 自动添加 @JsonFormat 注解

---

### 3.6 ON_OFF - 布尔开关类型

**类型标识**: `onOff`

**特有属性**: 无

**示例**:

```yaml
- type: onOff
  name: isActive
  title: 是否激活
  is_non_valid: true
```

**生成结果**:
- 数据库类型: `TINYINT(1)`
- Java 类型: `Boolean`
- 支持过滤: IN
- 可排序: 否

---

### 3.7 SECRET - 密码/密钥类型

**类型标识**: `secret`

**特有属性**: 无

**示例**:

```yaml
- type: secret
  name: password
  title: 密码
  is_non_valid: true
```

**生成结果**:
- 数据库类型: `VARCHAR(255)`
- Java 类型: `String`
- 支持过滤: 无
- 可排序: 否
- 特点: DB 中需加密保存，不可编辑，只能重置

---

## 四、索引定义 (IndexDef)

### 字段说明

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `item_names` | List | 是 | - | 组成索引的字段名称列表 |
| `is_unique` | Boolean | 否 | false | 是否唯一索引 |

### 示例

```yaml
indices:
  # 唯一索引 - 单字段
  - item_names: [studentNo]
    is_unique: true
  
  # 唯一索引 - 复合字段
  - item_names: [studentNo, subject, semester]
    is_unique: true
  
  # 普通索引
  - item_names: [grade]
    is_unique: false
  
  # 复合普通索引
  - item_names: [department, isActive]
    is_unique: false
```

---

## 五、完整示例

### 简单示例

```yaml
forms:
  - name: User
    title: 用户管理
    desc: 系统用户信息管理
    items:
      - type: text
        name: username
        title: 用户名
        is_non_valid: true
        max_length: 50
        regex: ^[a-zA-Z0-9_]{3,20}$
      
      - type: secret
        name: password
        title: 密码
        is_non_valid: true
      
      - type: select
        name: role
        title: 角色
        is_non_valid: true
        options:
          - code: admin
            title: 管理员
          - code: user
            title: 普通用户
      
      - type: onOff
        name: isActive
        title: 是否激活
        is_non_valid: true
      
      - type: time
        name: createdAt
        title: 创建时间
        is_non_valid: true
        init_pattern: todo
        edit_pattern: doNot
        format: dateTime
    
    indices:
      - item_names: [username]
        is_unique: true
```

### 复杂示例

参见 `forms-example.yml` 文件，包含：
- 4 个完整表单（学生、教师、成绩、课程安排）
- 覆盖所有 7 种字段类型
- 各种 init_pattern 和 edit_pattern 组合
- 单字段和复合字段索引
- 唯一索引和普通索引

---

## 六、字段类型快速参考表

| ItemType | 数据库类型 | Java 类型 | 可过滤 | 可排序 | 特殊说明 |
|----------|-----------|-----------|--------|--------|----------|
| text | VARCHAR/LONGTXT | String | ✓ | ✓ | 支持正则验证 |
| number | BIGINT/DECIMAL | Long/BigDecimal | ✓ | ✓ | 可选小数 |
| select | VARCHAR(64) | 枚举 | ✓ | ✗ | 需定义 options |
| multiSelect | VARCHAR(64) | 枚举 | ✓ | ✗ | 需定义 options，创建关联表 |
| time | DATETIME | LocalDate/Time/DateTime | ✓ | ✓ | 可选格式 |
| onOff | TINYINT(1) | Boolean | ✓ | ✗ | 布尔值 |
| secret | VARCHAR(255) | String | ✗ | ✗ | 需加密，不可编辑 |

---

## 七、命名规范

### 表单名称 (name)
- 格式: **UpperCamel**（大驼峰）
- 规则: 英文单词首字母大写
- 示例: `Student`, `TeacherSchedule`, `StudentGrade`
- 注意: 无需添加 "Form" 后缀

### 字段名称 (name)
- 格式: **lowerCamel**（小驼峰）
- 规则: 首字母小写，后续单词首字母大写
- 示例: `studentName`, `employeeNo`, `isActive`

### 选项代码 (code)
- 格式: **lowerCamel** 或 **snake_case**
- 规则: 建议使用有意义的英文标识
- 示例: `active`, `grade1`, `computer_science`

---

## 八、常见问题

### Q1: is_non_valid 和 required 有什么区别？
A: `is_non_valid` 是广义的非空概念，包括：
- 非 null
- 非空字符串（不含纯空格）
- 非空列表/数组
- 非未指定的值

### Q2: init_pattern 和 edit_pattern 的 doNot 和 todo 有什么区别？
A:
- `doNot`: 不生成初始化/编辑代码
- `todo`: 生成代码框架，但标记为 TODO，需开发者填充逻辑

### Q3: multiSelect 和 select 的区别？
A:
- `select`: 单选，值存储在主表字段
- `multiSelect`: 多选，需创建关联表存储多个值

### Q4: 如何设置字段默认值？
A: DSL 不直接支持默认值配置，需在生成后的 Service 层手动添加业务逻辑。

### Q5: 索引中的 item_names 必须存在吗？
A: 是的，`item_names` 中的字段名必须在 `items` 列表中存在，否则会验证失败。

---

## 九、最佳实践

1. **字段命名**: 使用清晰、有意义的英文名称
2. **is_non_valid**: 根据业务需求合理设置必填项
3. **索引设计**: 
   - 高频查询字段添加普通索引
   - 唯一性字段添加唯一索引
   - 避免过多索引影响写入性能
4. **init_pattern/edit_pattern**:
   - 不可变字段（如ID）设置 `edit_pattern: doNot`
   - 系统自动计算字段设置为 `todo`
5. **regex**: 为关键字段（邮箱、手机号）添加正则验证
6. **options**: code 使用英文，title 使用中文

---

*更新时间: 2026-02-15*
