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

**隐含行为**：`CommonItemsExpansionService.addCommonItems()` 自动在 items 头部插入 `bizId`，尾部追加 `createdAt`、`updatedAt`，
均设为 `canInputOnInit=false, canInputOnEdit=false, isBuiltinField=true`。`items.get(0)` 即 bizId。

### isBuiltinField

`ItemDef.isBuiltinField`（默认 false）标记由 `CommonItemsExpansionService` 自动追加的字段。`FormDef.getNonAuditedItems()` 通过 `!isBuiltinField` 过滤返回用户定义字段，用于审计日志等场景。

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

| init  | edit  | CreateReq 字段 | UpdateReq 字段 |       校验位置        |
|:-----:|:-----:|:-----------:|:-----------:|:-----------------:|
| true  | true  |      有      |      有      | 各自 ReqDTO 注解均带校验 |
| true  | false |      有      |      无      |  CreateReq 注解带校验  |
| false | true  |      无      |      有      |  UpdateReq 注解带校验  |
| false | false |      无      |      无      |    Create 内写默认值    |

## API 生成架构

每个 FormDef 生成 4 类 API，由独立 Service 负责：

| API | Service | 生成的 handler 名 | ReqDTO 含 bizId | 有 RespDTO |
|-----|---------|----------------|:--------------:|:---------:|
| 创建 | `CreateApiService` | `create{FormName}` | 否 | 是（含 bizId） |
| 更新 | `UpdateApiService` | `update{FormName}` | 是 | 否（void） |
| 列表 | `ListApiService` | `list{FormName}s` | — | 是 |
| 详情 | `GetDetailApiService` | `get{FormName}Detail` | 是 | 是 |
| 删除 | `DeleteApiService` | `delete{FormName}` | 是 | 否（void） |

`MutationApiSupport` 封装 Create/Update 共用逻辑（setter 生成、唯一索引校验、multiSelect 关联重建、setUpdatedAt）。

### 关键约束：唯一索引校验中的 isUpdate 参数

`MutationApiSupport.generateCheckExistStatement(form, index, isUpdate)` 生成 Design chain 查询判重。
- **Update 场景**：`.bizId.ne(req.getBizId())` 排除自身
- **Create 场景**：跳过 `.ne(bizId)`，因为 CreateReq 中没有 bizId 字段

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

### file

| 字段          | 类型      | 默认值     | 说明                                              |
|-------------|---------|---------|---------------------------------------------------|
| category    | String  | general | 文件类别，对应后端 FileCategoryEnum（image/document/archive/audio/video/general） |
| maxFileSize | Integer | null    | 前端 UX 校验用的最大文件大小（MB），留空不限制                         |

**合并单列设计**：业务表的 file 字段是单个 VARCHAR(512) 列，值为 `fileKey/originFileName`（首个 `/` 分隔）。因此 file 对 form-generator 退化为「一个 VARCHAR 列 + 一个 String DTO 字段」，走标准单字段路径，Ddl/Create/Update/GetDetail/List 均无特殊分支。

- **不可过滤**（`getFilterPatterns` 返回 null）、**不可排序**（不在 SortEnum 的 number/onOff/text/time 允许清单内）
- **审计日志**：`AppGeneratorMutationExpansionServiceImpl` 对 file 字段取 `originFileName`（合并列首个 `/` 之后的部分），不取 fileKey
- **file_record 表**（骨架固定表，持久层已生成）保留独立 `file_key`/`origin_file_name` 两列，不合并——下载接口按 fileKey 等值精确查询

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

## MutationExpansionService 钩子调用顺序

**Create 方法体生成顺序：**
1. 校验字段（唯一索引检查）
2. entity setter（canInputOnInit 字段）
3. `mutationApiSupport.generateSetCreatedAt()`
4. `mutationExpansionService.expandCreateMethodBody(form, body)` — 扩展点
5. mapper.insert
6. multiSelect 关联
7. `mutationExpansionService.postProcessCreateMethodBody(form, body)` — 后处理（审计日志包装等）

**Update 方法体生成顺序：**
1. entity setter（canInputOnEdit 字段）
2. `mutationApiSupport.generateSetUpdatedAt()`
3. `mutationExpansionService.expandUpdateMethodBody(form, body)` — 扩展点
4. 唯一索引检查
5. mapper.updateById
6. multiSelect 关联
7. `mutationExpansionService.postProcessUpdateMethodBody(form, body)` — 后处理

**Delete 方法体：** `mutationExpansionService.expandDeleteMethodBody(form, body)` 在核心删除逻辑之后调用。

## 动态排序（Sort Enum）

每个 FormDef 生成 `{FormName}SortEnum`，枚举项来源：
- 所有 type 为 `number`、`onOff`、`text`、`time` 的 item
- 内置的 `createdAt`、`updatedAt`
- 有 User 表单时额外追加 `createdBy`、`updatedBy`

List ReqDTO 包含 `sortBy`（枚举类型，nullable）和 `isAsc`（Boolean，nullable），通过 `FormGeneratorMapperLayerExpansionServiceImpl` 在 Mapper XML 中生成动态 `<choose>` ORDER BY 片段。未指定时默认 `ORDER BY id DESC`。
