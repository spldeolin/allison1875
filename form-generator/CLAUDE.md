# form-generator DSL 参考

## 概述

`form-generator` 从 `forms.yml` 生成完整的 CRUD 栈：DDL、Entity、Mapper、Mapper XML、Controller、Service、DTO、Enum。
它是唯一的 composite tool，内部链式调用 persistence-generator → handler-transformer → doc-analyzer → query-transformer。

## forms.yml 结构

```yaml
# 顶层为 FormDef 列表
- name: Order              # UpperCamel，表单/实体名（无需 "Form" 后缀）
  title: 订单              # 用户可见标题
  desc: 订单管理           # 可选描述
  items: # ItemDef 列表（不需要定义 bizId/createdAt/updatedAt，自动追加）
    - type: text
      name: orderNo        # lowerCamel 英文
      title: 订单号
      isNonVoid: true
      maxLength: 32
  indices: # 可选，IndexDef 列表
    - itemNames: [ orderNo ]
      isUnique: true
```

## FormDef 字段

| 字段      | 类型               | 必填 | 说明                  |
|---------|------------------|----|---------------------|
| name    | String           | 是  | UpperCamel，源码中的类名前缀 |
| title   | String           | 是  | 用户可见标题              |
| desc    | String           | 否  | 描述                  |
| items   | List\<ItemDef\>  | 是  | 字段定义列表（不含审计字段）      |
| indices | List\<IndexDef\> | 否  | 索引定义                |

**隐含行为**：`FormGenerator.addCommonItems()` 自动在 items 头部插入 `bizId`，尾部追加 `createdAt`、`updatedAt`，
均设为 `canInputOnInit=false, canInputOnEdit=false`。`items.get(0)` 即 bizId。

## ItemDef 公共字段

| 字段             | 类型      | 必填 | 默认值  | 说明                         |
|----------------|---------|----|------|----------------------------|
| type           | String  | 是  | —    | 字段类型，见下方类型表                |
| name           | String  | 是  | —    | lowerCamel 字段名             |
| title          | String  | 是  | —    | 用户可见标题                     |
| isNonVoid      | Boolean | 是  | —    | 是否非空（驱动校验注解与 NOT NULL DDL） |
| canInputOnInit | Boolean | 是  | true | 创建时是否允许用户输入                |
| canInputOnEdit | Boolean | 是  | true | 编辑时是否允许用户输入                |

### canInputOnInit / canInputOnEdit 组合语义

| init  | edit  | ReqDTO 字段 |          校验位置           |      setter 位置       |
|:-----:|:-----:|:---------:|:-----------------------:|:--------------------:|
| true  | true  |     有     |        ReqDTO 注解        | common 节（if/else 之外） |
| true  | false |     有     | if(toCreate) 内 if-throw |    if(toCreate) 内    |
| false | true  |     有     |     else 内 if-throw     |        else 内        |
| false | false |     无     |            —            |  if(toCreate) 内写默认值  |

## 字段类型 (type)

### text

| 字段                | 类型      | 默认值   | 说明                 |
|-------------------|---------|-------|--------------------|
| isMultilineOrRich | Boolean | false | 多行/富文本时 DB 用 TEXT  |
| maxLength         | Integer | 255   | 非多行时的最大长度 (≤65535) |
| regex             | String  | null  | 正则约束               |

### number

| 字段           | 类型      | 默认值   | 说明                                                 |
|--------------|---------|-------|----------------------------------------------------|
| canBeDecimal | Boolean | false | true → BigDecimal / DECIMAL; false → Long / BIGINT |

### select

| 字段      | 类型                | 说明       |
|---------|-------------------|----------|
| options | List\<OptionDef\> | 必填，可选项列表 |

### multiSelect

| 字段      | 类型                | 说明               |
|---------|-------------------|------------------|
| options | List\<OptionDef\> | 必填，可选项列表。多选创建关联表 |

**multiSelect 特殊性**：生成独立关联表，有独立的辅助 FormDef。走关联表单分支，不参与主表单的 canInputOn* 条件分支逻辑。

### time

| 字段     | 类型     | 默认值        | 说明                                                                 |
|--------|--------|------------|--------------------------------------------------------------------|
| format | String | "dateTime" | `date` → LocalDate, `time` → LocalTime, `dateTime` → LocalDateTime |

### onOff

无额外字段。DB 为 TINYINT(1)，Java 为 Boolean。

### secret

无额外字段。列表/搜索时脱敏，详情时明文，编辑时只能重置不能修改。

## OptionDef

| 字段    | 类型     | 必填 | 说明                          |
|-------|--------|----|-----------------------------|
| code  | String | 是  | 唯一标识 (≤64 字符)，转为 Java 枚举常量名 |
| title | String | 是  | 用户可见的选项标题                   |

## IndexDef

| 字段        | 类型             | 必填 | 默认值   | 说明              |
|-----------|----------------|----|-------|-----------------|
| itemNames | List\<String\> | 是  | —     | 组成索引的字段 name 列表 |
| isUnique  | Boolean        | 是  | false | 是否唯一索引          |

## FilterPattern 枚举

用于 list API 的过滤条件模式。在 `forms.yml` 中不直接出现，而是根据字段类型自动推断或在扩展配置中指定。

| 值             | 含义       | 生成的参数名形式              |
|---------------|----------|-----------------------|
| in            | 范围/交集/等于 | `xxxes` (复数)          |
| ge            | ≥        | `xxxGe`               |
| gt            | >        | `xxxGt`               |
| le            | ≤        | `xxxLe`               |
| lt            | <        | `xxxLt`               |
| like          | 模糊匹配     | `xxxKw`               |
| dateRange     | 日期范围     | `xxxBegin` + `xxxEnd` |
| dateTimeRange | 日期时间范围   | `xxxBegin` + `xxxEnd` |

## 完整示例

```yaml
- name: StudentProfile
  title: 学生信息
  desc: 学生基本信息管理
  items:
    - type: text
      name: studentName
      title: 学生姓名
      isNonVoid: true
      maxLength: 50
    - type: select
      name: gender
      title: 性别
      isNonVoid: true
      canInputOnInit: true
      canInputOnEdit: false
      options:
        - code: male
          title: 男
        - code: female
          title: 女
    - type: number
      name: age
      title: 年龄
      isNonVoid: false
    - type: text
      name: bio
      title: 简介
      isNonVoid: false
      isMultilineOrRich: true
    - type: time
      name: enrollDate
      title: 入学日期
      isNonVoid: true
      format: date
    - type: onOff
      name: isActive
      title: 是否在读
      isNonVoid: true
    - type: secret
      name: idCard
      title: 身份证号
      isNonVoid: true
    - type: multiSelect
      name: hobbies
      title: 兴趣爱好
      isNonVoid: false
      options:
        - code: sports
          title: 体育
        - code: music
          title: 音乐
        - code: reading
          title: 阅读
  indices:
    - itemNames: [ studentName ]
      isUnique: false
```
