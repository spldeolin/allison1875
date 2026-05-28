# Aligning frontend-skeleton With form-generator Output — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让 `app-generator` 的 `frontend-skeleton` 在调用 list / save / delete / getDetail 四个接口时（URL、请求体、响应体），与 `form-generator` 实际生成的后端 Spring Boot 代码严格对齐，并且 SearchForm 的查询参数严格遵循后端 list 入参 DTO（含 `FilterPattern` 衍生形式）。

**Architecture:** 在 `frontend-skeleton/src/core/protocol/` 新增 4 个文件（endpoints / request-builder / response-parser / field-policy），形成"分发器 + 规则表"形态的 protocol 层，集中所有"前端 schema ↔ 后端 DTO"的翻译；现有 vue 组件（CrudPage / SearchForm / EditModal / DataTable）改造为通过 protocol 层调用接口。`schema/types.ts` 新增 `FilterPattern` 类型；`field-policy.ts` 维护 `FILTER_PATTERNS_BY_TYPE` 常量表（mirror 后端 ItemService#getFilterPatterns 的固定映射，不通过 schema 传递）。

**Tech Stack:** TypeScript / Vue 3 / Naive UI / axios (frontend-skeleton 现有栈)；YAML（DSL 输入）；Java（form-generator，本计划只**读**它的产物，不改其源码）。

**Spec:** `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator-design.md`

**Working assumptions（从 design 继承）:**
- 不为 frontend-skeleton 写任何单测；验证 = TS 编译 + 联调 + contract.md。
- 不读 form-generator 源码；contract 来自读真实生成的 java（controller / req&resp DTO / enum 三类）。
- 不动 form-generator 源码；本计划仅作为"取证消费方"。

**Phase 间硬依赖:** Phase A 输出超级 DSL；用户人工执行 form-generator 后产出真实 java 文件并把绝对路径告诉本会话。Phase B 读那批 java，产出 contract.md / gaps.md，并等待用户对 gaps 拍板。Phase C 才动 frontend-skeleton 代码。**计划是顺序执行的，不可并行。**

---

## File Structure

### Phase A 产物（计划新增）

```
docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/
  super-dsl.yml              # 超级 DSL（YAML）
  super-dsl-readme.md        # DSL 设计说明（每个 form 覆盖哪些维度）
```

### Phase B 产物（计划新增）

```
docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/
  contract.md                # 4 接口的 URL/入参/出参/枚举
  gaps.md                    # 缺口清单 + 处置建议
```

### Phase C 改动（frontend-skeleton）

```
app-generator/src/main/resources/frontend-skeleton/
  src/
    schema/
      types.ts               # 修改：新增 FilterPattern 类型（ItemDefBase 不加 filterPatterns 字段）
    core/
      protocol/              # 新增目录
        endpoints.ts         # 新增
        request-builder.ts   # 新增
        response-parser.ts   # 新增
        field-policy.ts      # 新增
      CrudPage.vue           # 修改
      SearchForm.vue         # 修改
      EditModal.vue          # 修改
      DataTable.vue          # 修改
    utils/
      naming.ts              # 修改：deriveApiBasePath 校准；可能仅保留字符串变换工具
    app.json                 # 修改：示例 schema 跟随 types 扩展
```

### Phase C 改动（app-generator java 端）

无需改动。

---

# Phase A — 取证据

## Task A1: 调研 form-generator 的所有 DSL 维度（已隐式完成，仅整理输出）

**Files:**
- Read-only: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/**/*.java`

调研目的：写超级 DSL 之前，把要覆盖的维度列全。

- [ ] **Step 1: 读 FormDef、ItemDef、IndexDef，列出顶层字段**

Read 这三个文件：
- `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/FormDef.java`
- `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/ItemDef.java`
- `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/IndexDef.java`

整理 FormDef 顶层字段（name / title / desc / items / indices）和 ItemDef 通用字段（name / title / isNonVoid / initPattern / editPattern / type / specialItemType）。

- [ ] **Step 2: 读 7 个 ItemDef 子类，列出每个子类的特有字段**

`ls form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/item/` 已知有 7 个：MultiSelectItemDef / NumberItemDef / OnOffItemDef / SecretItemDef / SelectItemDef / TextItemDef / TimeItemDef。逐个 Read，把每个子类的特有字段（含 filterPatterns、checkDuplicate、maxLength、isMultilineOrRich、regex、canBeDecimal、canBeNegative、options、isMulti、format/pattern、displayType、desensitization）列在 readme 草稿里。

- [ ] **Step 3: 读 enums，确认 FilterPattern / InitOrEditPattern / TimeFormat / SpecialItemType / ApiType 的所有取值**

Read：
- `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/FilterPattern.java`（已知 8 个：in / ge / gt / le / lt / like / dateRange / dateTimeRange）
- `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/InitOrEditPattern.java`
- `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/TimeFormat.java`
- `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/SpecialItemType.java`
- `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/ApiType.java`

- [ ] **Step 4: 把维度清单写到 super-dsl-readme.md 草稿（不提交）**

创建 `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl-readme.md`，先写"维度索引"一节，列出所有要覆盖的取值/枚举，以备 Task A2 设计 DSL 时对照。**本步只起草，不要 commit**——A2 写完 DSL 后再一起 commit。

```markdown
# Super DSL Readme

本文档说明 super-dsl.yml 中每个 form 覆盖哪些 form-generator DSL 维度。

## 维度索引

### ItemType (7)
- text（子变体：maxLength / isMultilineOrRich / regex）
- number（子变体：canBeDecimal / canBeNegative）
- select（options，单选）
- multiSelect（options，多选 — 以 isMulti 还是独立类型实现，待 Task A2 写 DSL 时确认 yml 格式）
- time（pattern: date | time | dateTime）
- onOff
- secret（displayType: desensitization | hidden；desensitization: [start, end]）

### FilterPattern (8)
in / ge / gt / le / lt / like / dateRange / dateTimeRange

### InitOrEditPattern
（待 A1 Step 3 读完后填写，预期 doNot / userInput / todo 三个）

### 其他维度
- isNonVoid: true / false
- checkDuplicate: true 字段
- 复合索引（indices.itemNames 多元素）
- 唯一索引 (isUnique: true) vs 非唯一索引
- 自动注入：${formName}Code / createdAt / updatedAt（由 FormGenerator.addCommonItems 注入，DSL 不写）
```

## Task A2: 写超级 DSL（YAML）

**Files:**
- Create: `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl.yml`
- Modify (local draft): `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl-readme.md`

设计 3-4 个 form 覆盖 A1 里列的所有维度。**参考样本**是 `form-generator/src/test/resources/study-dsl.yml`——它的 yaml 顶层有 `forms:` 包装。注意：`app-generator` 在 `AppGenerator.invokeFormGenerator` 里会把 `List<FormDef>` 写成 yaml 给 form-generator 读，结构是**顶层数组**，不是 `forms:` 包装；但本任务的产物是给**form-generator 单独**跑（验证后端契约），所以使用 `forms:` 顶层包装格式（与 study-dsl.yml 一致）。

- [ ] **Step 1: 设计 Form 1 — `StudentProfile`，覆盖 text 全子变体 + like + in + 部分 InitOrEditPattern**

创建 `super-dsl.yml`，先写第一个 form。要求：
- 含 1 个 text(短) + filterPatterns: [like] + checkDuplicate
- 含 1 个 text(maxLength=2000, isMultilineOrRich=true) + 不参与过滤
- 含 1 个 text(regex=`^\\d{18}$`) + filterPatterns: [in]
- 含 1 个 number(canBeDecimal=false, canBeNegative=false) + filterPatterns: [ge, le]
- 含 1 个 select 单选 + filterPatterns: [in]
- initPattern/editPattern 至少出现 doNot 和 userInput

```yaml
forms:
  - name: StudentProfile
    title: 学生档案
    desc: 覆盖 text 全子变体 + like + in + GE/LE
    items:
      - type: text
        name: studentName
        title: 学生姓名
        is_non_void: true
        max_length: 50
        filter_patterns: [like]
        check_duplicate: true
      - type: text
        name: bio
        title: 学生简介
        is_non_void: false
        is_multiline_or_rich: true
        max_length: 2000
        # 无 filter_patterns —— 不参与查询
      - type: text
        name: idCard
        title: 身份证号
        is_non_void: true
        regex: '^\d{18}$'
        filter_patterns: [in]
      - type: number
        name: age
        title: 年龄
        is_non_void: true
        can_be_decimal: false
        can_be_negative: false
        filter_patterns: [ge, le]
      - type: select
        name: grade
        title: 年级
        is_non_void: true
        options:
          - code: '1'
            title: 一年级
          - code: '2'
            title: 二年级
        filter_patterns: [in]
    indices:
      - item_names: [idCard]
        is_unique: true
```

- [ ] **Step 2: 设计 Form 2 — `StudentDormitory`，覆盖 multiSelect + onOff + secret 全子变体 + gt/lt**

```yaml
  - name: StudentDormitory
    title: 学生宿舍
    desc: 覆盖 multiSelect + onOff + secret + gt/lt + todo pattern
    items:
      - type: number
        name: studentId
        title: 学生ID
        is_non_void: true
        can_be_decimal: false
        can_be_negative: false
        filter_patterns: [in]
      - type: onOff
        name: needDormitory
        title: 是否住宿
        is_non_void: true
        # filter_patterns 是否支持 onOff —— 留空，由 form-generator 决定是否生成入参；这是观察项
      - type: select
        name: specialNeeds
        title: 特殊需求
        is_non_void: false
        is_multi: true
        options:
          - code: lower_floor
            title: 低楼层
          - code: quiet
            title: 安静
          - code: near_bath
            title: 近卫生间
        filter_patterns: [in]
      - type: secret
        name: emergencyPhone
        title: 紧急联系电话
        is_non_void: true
        display_type: desensitization
        desensitization: [3, 7]
        # secret 通常不参与查询；不写 filter_patterns
        init_pattern: userInput
        edit_pattern: userInput
      - type: secret
        name: doorPassword
        title: 门锁密码
        is_non_void: true
        display_type: hidden
        init_pattern: todo
        edit_pattern: doNot
      - type: number
        name: roomFee
        title: 住宿费
        is_non_void: true
        can_be_decimal: true
        can_be_negative: false
        filter_patterns: [gt, lt]
    indices:
      - item_names: [studentId]
        is_unique: true
```

- [ ] **Step 3: 设计 Form 3 — `StudentExam`，覆盖 time 全子变体 + dateRange/dateTimeRange + 复合索引**

```yaml
  - name: StudentExam
    title: 学生考试
    desc: 覆盖 time(date|time|dateTime) + dateRange + dateTimeRange + 复合唯一索引 + can_be_negative
    items:
      - type: number
        name: studentId
        title: 学生ID
        is_non_void: true
        can_be_decimal: false
        can_be_negative: false
      - type: select
        name: subject
        title: 科目
        is_non_void: true
        options:
          - code: math
            title: 数学
          - code: chinese
            title: 语文
        filter_patterns: [in]
      - type: number
        name: score
        title: 分数
        is_non_void: true
        can_be_decimal: true
        can_be_negative: true
        filter_patterns: [ge, le]
      - type: time
        name: examDate
        title: 考试日期
        is_non_void: true
        pattern: date
        filter_patterns: [dateRange]
      - type: time
        name: examStartTime
        title: 考试开始时间
        is_non_void: true
        pattern: time
      - type: time
        name: gradedAt
        title: 评分时间
        is_non_void: true
        pattern: dateTime
        filter_patterns: [dateTimeRange]
    indices:
      - item_names: [studentId, subject, examDate]
        is_unique: true
```

- [ ] **Step 4: 完成 super-dsl-readme.md**

把每个 form 在维度上的覆盖矩阵写进 readme，类似：

```markdown
## Form 覆盖矩阵

| 维度 | StudentProfile | StudentDormitory | StudentExam |
|------|----|----|----|
| text/short + filterPattern | like, in (idCard) | — | — |
| text/multiline | bio | — | — |
| text/regex | idCard | — | — |
| number/integer + filterPattern | age (ge,le) | studentId (in) | studentId (none), score (ge,le) |
| number/decimal | — | roomFee (gt,lt) | score |
| number/negative-allowed | — | — | score |
| select 单选 | grade (in) | — | subject (in) |
| multiSelect | — | specialNeeds (in) | — |
| time/date | — | — | examDate (dateRange) |
| time/time | — | — | examStartTime |
| time/dateTime | — | — | gradedAt (dateTimeRange) |
| onOff | — | needDormitory | — |
| secret/desensitization | — | emergencyPhone | — |
| secret/hidden | — | doorPassword | — |
| InitOrEditPattern.todo | — | doorPassword.init | — |
| InitOrEditPattern.doNot | — | doorPassword.edit | — |
| checkDuplicate | studentName | — | — |
| 复合唯一索引 | — | — | (studentId,subject,examDate) |
| 单字段唯一索引 | idCard | studentId | — |

## 未覆盖且确认无需覆盖
（在此列出剩余维度，并说明为何不覆盖）
```

- [ ] **Step 5: Commit super-dsl + readme**

```bash
git add docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/
git commit -m "docs(aligning): add super DSL and dimensions matrix for Phase A"
```

## Task A3: 移交给用户执行 form-generator

**Files:** none

- [ ] **Step 1: 给用户操作指引**

输出给用户一段话，包含：
1. super-dsl.yml 的绝对路径；
2. 让用户用这个 DSL 跑 form-generator（用户自己熟悉如何配置 dslPath 和输出目录）；
3. 要求用户跑完后，把生成的 controller / req&resp DTO / enum 三类 java 文件所在**绝对路径**告诉会话。本会话会用 Read/Glob 自取，不再让用户贴源码。

- [ ] **Step 2: 等待用户响应**

收到用户给的绝对路径之前，**不要进入 Phase B**。如果用户回复"已准备好，路径是 X"，记下 X，进入 Phase B。

---

# Phase B — 提炼契约 + 缺口

> 进入此阶段的前置条件：用户已给出 java 输出目录的绝对路径。

## Task B1: 读 controller、确认 4 接口的 URL 模板

**Files:**
- Read-only: 用户给的路径下的 `**/*Controller.java`
- Modify (draft): `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/contract.md`（新建）

- [ ] **Step 1: Glob 找到所有 Controller**

Run: `Glob` pattern `**/*Controller.java` 在用户给的路径下。预期找到 3 个：`StudentProfileController.java` / `StudentDormitoryController.java` / `StudentExamController.java`。

- [ ] **Step 2: Read 每个 Controller**

每个 Read 完后，关注：
- 类级 `@RequestMapping`（决定 base path）；
- 4 个方法（save / list / getDetail / delete）的 `@PostMapping` / `@RequestMapping` 路径；
- 每个方法的入参类型（req DTO 类名）和返回类型（resp DTO 类名）；
- 注意：design 文档显式提到 form-generator 里有 `handler-transformer` 把 `initDec` 转成最终 controller 方法 — 读到的就是转后的产物。

- [ ] **Step 3: 写 contract.md 的"接口 URL 节"**

创建 `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/contract.md`，先写表格：

```markdown
# Backend Contract — Derived From Generated Code

源：form-generator 输出于 <用户提供的绝对路径>
DSL：super-dsl.yml

## 1. URL 表

| Form | base path | save | list | getDetail | delete |
|---|---|---|---|---|---|
| StudentProfile | <实际值> | <实际值> | ... | ... | ... |
| StudentDormitory | ... | ... | ... | ... | ... |
| StudentExam | ... | ... | ... | ... | ... |

## 2. URL 推导规则

（基于 1 中三行的共性总结，例如：
- base path = `/api/v1/${lower-camel-form-name}` 还是 `/${kebab-form-name}` 还是其他
- 每个 action 的具体 path segment）
```

填入实际从 java 中读到的值。如果三行不能形成清晰规则，把这个发现记到 gaps.md 待 Phase B5 处理。

- [ ] **Step 4: Commit contract.md 草稿**

```bash
git add docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/contract.md
git commit -m "docs(aligning): contract Phase B1 — URL table"
```

## Task B2: 读 list 入参 DTO，确认 FilterPattern 衍生规则

**Files:**
- Read-only: 用户路径下的 list 入参 DTO（类名待 B1 确认，可能形如 `Page${FormName}ReqDto.java`）
- Modify: `contract.md`

**背景**：form-generator 按 ItemType 固定分配 FilterPatterns（不经 DSL 配置）：
- text → IN, LIKE；number → IN, GE, GT, LE, LT；time → IN, DATE_RANGE, DATE_TIME_RANGE；
- select/multiSelect/onOff → IN；secret → 无（不参与查询）

观察的核心问题是：**这些 FilterPattern 在 DTO 里是以何种字段名规则体现的**。

- [ ] **Step 1: 对每个 form 的 list 入参 DTO Read**

观察：
- text 字段（形如 LIKE）在 DTO 里字段名叫什么？类型？
- select 字段（IN）在 DTO 里是 `List<String>` 还是单值？字段名后缀？
- number 字段（GE/LE 等）在 DTO 里产出几个字段、命名规则？
- time 字段（DATE_RANGE / DATE_TIME_RANGE）产出 begin/end 还是 start/end？类型是 String 还是 LocalDate？
- secret 字段是否出现在 DTO？（预期不出现）
- 分页参数字段名（pageNo + pageSize？pageNum？current + size？）— 与 `app-generator` 里 `setPageParamStyle(PAGE_NO_PAGE_SIZE)` 对照。
- 没有 FilterPattern（secret）字段——它们是否出现在 DTO？以什么形式？（预期不出现）
- 分页参数（pageNo、pageSize 还是 pageNum、pageSize？current、size？）— 与 `app-generator` 里 `setPageParamStyle(PAGE_NO_PAGE_SIZE)` 对照。

- [ ] **Step 2: 写 contract.md 的"list 入参规则节"**

```markdown
## 3. List 入参 DTO 规则

### 分页字段
（实际观察值，例：pageNo / pageSize）

### 按 ItemType × FilterPattern 衍生表

| ItemType | FilterPattern | DTO 字段名规则 | DTO 字段类型 | 备注 |
|---|---|---|---|---|
| text | like | `${name}Like` | String | 例：studentName → studentNameLike |
| text | in | `${name}List` 或 `${name}s` | List<String> | 例：idCard → ... |
| number | ge | `${name}Ge` | Long/Integer/BigDecimal | |
| number | le | ... | | |
| number | in | ... | | |
| number | gt / lt | ... | | |
| select | in | ... | | |
| multiSelect | in | ... | | |
| time | dateRange | begin/end 字段名 | String/LocalDate | 例：examDate → examDateBegin, examDateEnd |
| time | dateTimeRange | ... | | |

### secret 字段
（预期不出现在 list 入参 DTO；若出现则记录）
```

填入实际值。**所有"字段名规则"只能基于 3 个 form 的数据点**——若 ItemType×FilterPattern 的某个组合在 super-dsl 里没出现（例如 select+like），表格里写"未覆盖，本计划范围外"，不脑补。

- [ ] **Step 3: Commit**

```bash
git add docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/contract.md
git commit -m "docs(aligning): contract Phase B2 — list request DTO rules"
```

## Task B3: 读 list 出参 + getDetail 出参 + save 入参，确认 DTO 全字段

**Files:**
- Read-only: list/getDetail/save 相关的 DTO 类
- Modify: `contract.md`

- [ ] **Step 1: 找出每个 form 的剩余 DTO**

每个 form 应该有：
- list 出参（`List${FormName}RespDto` 或类似 — 内含 PageResult<T> 形式）
- getDetail 出参
- save 入参
- delete 入参（通常很简单，可能就是 `{ ${formName}Code }`）

- [ ] **Step 2: 对每个 form，对照 super-dsl 里的 items，把"哪些字段在哪种 DTO 里出现 + 类型 + 是否 nullable"列成表**

```markdown
## 4. 字段出现矩阵 — 以 StudentDormitory 为例

| Item (DSL) | list out | getDetail out | save in | delete in | 备注 |
|---|---|---|---|---|---|
| ${formName}Code (auto-injected) | ✓ String | ✓ String | ✗ (create) ✓ (update) | ✓ String | 业务主键 |
| createdAt | ✓ String | ✓ String | ✗ | ✗ | 后端自动填，前端不传 |
| updatedAt | ✓ String | ✓ String | ✗ | ✗ | |
| studentId (number) | ✓ Long | ✓ Long | ✓ Long | ✗ | |
| needDormitory (onOff) | ✓ Boolean | ✓ Boolean | ✓ Boolean | ✗ | |
| specialNeeds (multiSelect) | ✓ List<String> 还是 String? | ✓ ... | ✓ ... | ✗ | 待观察 |
| emergencyPhone (secret/desens) | ✓ String（脱敏） | ✓ String（明文 还是 脱敏？— 待观察） | ✓ String | ✗ | **关键观察项** |
| doorPassword (secret/hidden) | ✗ 不返回 还是 ✓？ | ✗ 还是 ✓？ | ✓ (init=todo 时是否要传？) | ✗ | **关键观察项** |
| ... | | | | | |
```

每个 cell 填实际从 java 读到的事实，不脑补。secret 字段、init/edit pattern = doNot/todo 的字段是观察重点。

- [ ] **Step 3: 三个 form 都填完后，commit contract.md**

```bash
git add docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/contract.md
git commit -m "docs(aligning): contract Phase B3 — DTO field matrix per form"
```

## Task B4: 读 enum 文件，确认枚举编码与类型

**Files:**
- Read-only: 用户路径下的 `**/*Enum.java` 或位于 `enums/` 包下的所有 java
- Modify: `contract.md`

- [ ] **Step 1: Glob 找出所有枚举类**

预期：每个 select / multiSelect 字段会被 form-generator 生成一个枚举（看 `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/EnumService.java` 是已知的）。

- [ ] **Step 2: 对每个枚举，记录"在 DTO 里出现时是 code 字符串还是 ordinal int 还是 name 字符串"**

观察方法：
- 看 DTO 字段类型：是 `String` 还是 `Enum` 还是 `Integer`？
- 看 form-generator 的 ItemType 处理（已知：`mybatis/EnumTypeHandlerEx.java` 是与 mybatis 对接，但 controller resp DTO 直接是不是 String 由 Jackson 序列化策略决定）。
- 关键：select/multiSelect 字段在前端 schema 里 options 都是 `{code, title}`，需要确认前端发出去的 code 字符串和后端接收的是不是同一个。

- [ ] **Step 3: 写 contract.md 的"枚举节"**

```markdown
## 5. 枚举编码

| Form/Field | Java enum class | 序列化形式 | 例 |
|---|---|---|---|
| StudentProfile.grade | GradeEnum | code 字符串 | "1" / "2" |
| ... | ... | ... | ... |
```

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/contract.md
git commit -m "docs(aligning): contract Phase B4 — enum encoding"
```

## Task B5: 写 gaps.md

**Files:**
- Create: `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/gaps.md`

把整个 Phase B 中所有"前端需要但后端没暴露"或"语义模糊"的项目集中。design 已预设的几个观察项必然要在这里出现：

- [ ] **Step 1: 列出已知缺口候选**

至少包括：
1. **secret 在 list vs detail 是否切换明文/脱敏**：观察实际 java，记录事实，给"前端如何处理 secret 字段的展示"建议。
2. **secret initPattern=todo / editPattern=doNot 字段在 save 入参里是必填还是可选**：影响 EditModal 的 required 规则。
3. **multiSelect 在 DTO 是 `List<String>` 还是逗号分隔字符串**：影响 response-parser 是否要做拆分。
4. **base path 派生规则**（如果 B1 里三行不形成清晰规则）：影响 endpoints.ts 的实现。
5. **delete 接口的入参业务主键字段名是不是 `${formName}Code`**（design 默认假设是，但需事实验证）。
6. **FormDef getter 污染 app.json**（C0 发现时从那里提过来）：若 `getVarName` / `getBizIdName` 等没有 `@JsonIgnore`，需决策改哪一侧。

每条记录格式：

```markdown
## Gap 1: secret 字段在 list vs detail 的明文/脱敏行为

**事实**: 从 StudentDormitory 的 List/GetDetail RespDto 中观察到 ...
**前端影响**: ...
**建议处置**:
- 选项 A: 前端 schema 增加一个"secret 显示明文按钮"，仅在 detail 模式下展示；
- 选项 B: 在 field-policy 中固定规则"secret 字段在 search 隐藏、在 table 直接显示后端返回的字符串、在 edit 不预填值"；
- 选项 C: 改 form-generator …
**推荐**: B（理由：…）
**待用户拍板**: ☐
```

- [ ] **Step 2: 把所有缺口写进文件**

如果某条候选缺口在实际观察中并不存在（例如所有 secret 在 list/detail 都是脱敏，没有明文区别），就不写进 gaps.md，而是在 contract.md 里以"观察值"形式记下。gaps.md 只放需要决策的项。

- [ ] **Step 3: Commit**

```bash
git add docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/gaps.md
git commit -m "docs(aligning): Phase B5 — gaps requiring user decision"
```

## Task B6: 移交给用户审阅 contract + gaps

**Files:** none

- [ ] **Step 1: 给用户呈现两份文档的链接**

明确请求用户：
1. 阅读 `contract.md`，纠正任何"我从 java 中读错"的项；
2. 阅读 `gaps.md`，对每条 Gap 拍板（选 A/B/C 或新方案）。

- [ ] **Step 2: 收到用户拍板后，更新 gaps.md 标注每条的最终决议**

把每条 Gap 的"待用户拍板: ☐"改为"用户决议: <内容>，理由: <用户说的>"。Commit：

```bash
git add docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/gaps.md
git commit -m "docs(aligning): record user decisions on gaps"
```

- [ ] **Step 3: 等待用户允许进入 Phase C**

不要在用户没确认 contract+gaps 都通过之前进入 Phase C。

---

# Phase C — 改前端

> 进入此阶段的前置条件：contract.md / gaps.md 用户已审阅 + 缺口决策已记录。

## Task C0: 验证 app.json 不含污染字段

**Files:**
- Read-only: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java`

背景：`AppGenerator.generateFrontend` 用 `new ObjectMapper().writeValueAsString(appDef)` 序列化整个 AppDef。`FormDef` 上有若干计算 getter（`varName`, `bizIdName`, `bizIdGetterName`, `bizIdSetterName`, `entityName`），Jackson 默认会把这些 getter 序列化进 app.json，污染前端 schema。

- [ ] **Step 1: 检查 FormDef 的 @JsonIgnore 覆盖情况**

Read `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/FormDef.java`（已知 `getNonAuditedItems` 有 `@JsonIgnore`；检查 `getVarName` / `getBizIdName` / `getBizIdGetterName` / `getBizIdSetterName` / `getEntityName` 是否也有）。

- [ ] **Step 2: 如有污染 getter，提为 gap**

若这些 getter 没有 `@JsonIgnore`，在 `gaps.md` 里新增一条 Gap，建议：改 form-generator FormDef 给这些 getter 加 `@JsonIgnore`，或改 AppGenerator 用自定义 ObjectMapper 排除。等用户拍板后再改。

如果全都有 `@JsonIgnore`，跳过。

- [ ] **Step 3: 视情况 commit 或跳过**

## Task C1: 扩 schema/types.ts，新增 FilterPattern 类型

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/schema/types.ts`

- [ ] **Step 1: 加 FilterPattern union type**

把 form-generator 已知的 8 个 FilterPattern 取值（**以 contract.md 实际确认为准**）翻译成 ts union。在 types.ts 顶部、`InitOrEditPattern` 之后插入：

```ts
export type FilterPattern =
  | 'in'
  | 'ge'
  | 'gt'
  | 'le'
  | 'lt'
  | 'like'
  | 'dateRange'
  | 'dateTimeRange'
```

**注意**：`ItemDefBase` **不加** `filterPatterns` 字段。FilterPattern 映射由 `field-policy.ts` 里的常量表维护（Task C5），不走 schema 传递。

- [ ] **Step 2: 跑 vue-tsc 确认类型不破**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc --noEmit`
Expected: 通过。**Note**：如果项目还没装依赖，先 `npm install`。

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/schema/types.ts
git commit -m "feat(frontend-skeleton): add FilterPattern type to schema/types.ts"
```

## Task C2: 创建 protocol/endpoints.ts

**Files:**
- Create: `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/endpoints.ts`

- [ ] **Step 1: 写 endpoints.ts**

按 contract.md 的"URL 推导规则"实现。下面给一个**示例骨架**——实际 base path / action path 的常量值**必须替换为 contract.md 里 B1 确认的实际规则**：

```ts
// app-generator/src/main/resources/frontend-skeleton/src/core/protocol/endpoints.ts

import { upperCamelToLowerCamel } from '@/utils/naming'

export type CrudAction = 'list' | 'save' | 'delete' | 'getDetail'

// ⚠️ 以下常量来自 contract.md §1-2，若后端改路径，只改这里
const ACTION_PATH: Record<CrudAction, string> = {
  list:      '/list',       // ← 替换为 contract.md §2 实际值
  save:      '/save',       // ← 替换
  delete:    '/delete',     // ← 替换
  getDetail: '/getDetail'   // ← 替换
}

export function endpointOf(formName: string, action: CrudAction): string {
  // ⚠️ base path 派生规则按 contract.md §2 实际确认；下例假设 /api/v1/${lowerCamel}
  const base = `/api/v1/${upperCamelToLowerCamel(formName)}`
  return base + ACTION_PATH[action]
}
```

- [ ] **Step 2: 跑 vue-tsc**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc --noEmit`
Expected: PASS。

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/protocol/endpoints.ts
git commit -m "feat(frontend-skeleton): add protocol/endpoints.ts"
```

## Task C3: 创建 protocol/request-builder.ts

**Files:**
- Create: `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/request-builder.ts`

- [ ] **Step 1: 写 buildListRequest**

按 contract.md §3"List 入参 DTO 规则"实现。下面给骨架——**所有键名后缀必须按 contract.md 实际值替换**：

```ts
// app-generator/src/main/resources/frontend-skeleton/src/core/protocol/request-builder.ts

import type { ItemDef, FilterPattern } from '@/schema/types'
import { getFilterPatternsByItemType } from './field-policy'

interface PaginationInput {
  pageNo: number      // ← 字段名按 contract.md §3 分页字段
  pageSize: number
}

interface SortInput {
  field: string
  direction: 'asc' | 'desc'
}

// 每条规则：给定 item 和 formState 中该字段的当前值，返回若干键值对
type ListRule = (item: ItemDef, value: any) => Record<string, any>

const filterPatternListRules: Record<FilterPattern, ListRule> = {
  // ⚠️ 以下规则来自 contract.md §3 衍生表，键名后缀按实际替换
  like:          (item, v) => v ? { [`${item.name}Like`]: v } : {},
  in:            (item, v) => Array.isArray(v) && v.length ? { [`${item.name}List`]: v } : {},
  ge:            (item, v) => v != null ? { [`${item.name}Ge`]: v } : {},
  gt:            (item, v) => v != null ? { [`${item.name}Gt`]: v } : {},
  le:            (item, v) => v != null ? { [`${item.name}Le`]: v } : {},
  lt:            (item, v) => v != null ? { [`${item.name}Lt`]: v } : {},
  dateRange:     (item, v) => buildDateRange(item, v, 'Begin', 'End'),
  dateTimeRange: (item, v) => buildDateRange(item, v, 'Begin', 'End')
}

function buildDateRange(item: ItemDef, v: any, beginSuffix: string, endSuffix: string) {
  if (!Array.isArray(v) || v.length !== 2) return {}
  const [begin, end] = v
  const out: Record<string, any> = {}
  if (begin != null) out[`${item.name}${beginSuffix}`] = begin
  if (end != null) out[`${item.name}${endSuffix}`] = end
  return out
}

export function buildListRequest(
  items: ItemDef[],
  formState: Record<string, any>,
  pagination: PaginationInput,
  sort?: SortInput
): Record<string, any> {
  const out: Record<string, any> = {
    pageNo: pagination.pageNo,
    pageSize: pagination.pageSize
  }
  for (const item of items) {
    const patterns = getFilterPatternsByItemType(item.type)
    for (const p of patterns) {
      const rule = filterPatternListRules[p]
      Object.assign(out, rule(item, formState[item.name]))
    }
  }
  if (sort) {
    // ⚠️ 后端尚未支持 sort（design 已预留参数），具体字段名待后续 contract 更新
    out.sortField = sort.field
    out.sortDirection = sort.direction
  }
  return out
}
```

**关于 sort**：design 明确说 buildListRequest 预留 sort 参数。当前后端不支持，所以代码里**只是把入参展开成两个字段**作为占位；调用方暂不传 sort，后端实装后这里再按当时的 contract 调整。在代码里加注释说明。

- [ ] **Step 2: 写 buildSaveRequest**

按 contract.md §4"字段出现矩阵 / save in"列实现。规则：
- 当前 mode = 'create'：`initPattern == 'doNot'` 的字段不传；`initPattern == 'todo'` 的字段是否传由 contract / gaps 决议；其余传 formState[name]。
- 当前 mode = 'edit'：`editPattern == 'doNot'` 的字段不传；其余传；业务主键 (`${formName}Code`) 必须传——但**业务主键的具体名称由调用方传入**，因为 buildSaveRequest 拿不到 formName。

```ts
export function buildSaveRequest(
  items: ItemDef[],
  formState: Record<string, any>,
  mode: 'create' | 'edit'
): Record<string, any> {
  const out: Record<string, any> = {}
  for (const item of items) {
    const pattern = mode === 'create' ? item.initPattern : item.editPattern
    if (pattern === 'doNot') continue
    if (pattern === 'todo') {
      // ⚠️ todo 字段的传输规则按 contract.md / gaps 决议；下例假设也传
      out[item.name] = formState[item.name] ?? null
      continue
    }
    // pattern === 'userInput'
    out[item.name] = formState[item.name] ?? null
  }
  return out
}
```

**关于 todo 字段、bizKey**：必须在写完 contract.md 后回来修改这段代码，使其严格符合 contract+gaps 决议。

- [ ] **Step 3: 跑 vue-tsc**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc --noEmit`
Expected: PASS。

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/protocol/request-builder.ts
git commit -m "feat(frontend-skeleton): add protocol/request-builder.ts"
```

## Task C4: 创建 protocol/response-parser.ts

**Files:**
- Create: `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/response-parser.ts`

- [ ] **Step 1: 写 parseListRow / parseDetailDto**

按 contract.md §4 字段矩阵实现。本任务的核心是处理 secret 字段在 list/detail 的差异、multiSelect 的格式归一化、time 字段的 ISO 字符串处理。

```ts
// app-generator/src/main/resources/frontend-skeleton/src/core/protocol/response-parser.ts

import type { ItemDef } from '@/schema/types'

type Mode = 'list' | 'detail'

type ParseRule = (item: ItemDef, raw: any, mode: Mode) => any

const itemTypeParseRules: Record<ItemDef['type'], ParseRule> = {
  text:         (_item, raw) => raw,
  number:       (_item, raw) => raw,
  onOff:        (_item, raw) => raw,
  time:         (_item, raw) => raw, // 后端通常返回 ISO 字符串；naive-ui 时间组件能消费
  select:       (_item, raw) => raw, // 单 code 字符串
  multiSelect:  (_item, raw) => {
    // ⚠️ 按 contract.md §4 决定：若后端是 List<String> 则原样；若是逗号字符串则 split
    if (Array.isArray(raw)) return raw
    if (typeof raw === 'string') return raw.split(',').filter(Boolean)
    return []
  },
  secret:       (_item, raw, _mode) => raw  // 后端返回什么就用什么；细则按 gaps 决议
}

function parseRow(items: ItemDef[], dto: Record<string, any>, mode: Mode): Record<string, any> {
  const out: Record<string, any> = {}
  for (const item of items) {
    const rule = itemTypeParseRules[item.type]
    out[item.name] = rule(item, dto[item.name], mode)
  }
  // 审计字段：${formName}Code / createdAt / updatedAt 不在 items 里（schema 不定义），但 dto 里有
  // 它们以原值形式透传（前端 DataTable 不展示，但 EditModal/getDetail 流程里可能用到 bizKey）
  // 调用方可以读 dto.<bizKey> / dto.createdAt
  return { ...dto, ...out }  // dto 在前是为了保留审计字段；out 在后覆盖被规则处理过的字段
}

export function parseListRow(items: ItemDef[], dto: Record<string, any>): Record<string, any> {
  return parseRow(items, dto, 'list')
}

export function parseDetailDto(items: ItemDef[], dto: Record<string, any>): Record<string, any> {
  return parseRow(items, dto, 'detail')
}
```

**注意**：上面 `{ ...dto, ...out }` 的写法保证审计字段（`${formName}Code` / `createdAt` / `updatedAt`，它们不在 items 里）原样透传到前端 row；调用方（CrudPage）能用 `row[bizKey]` 取到业务主键传给 delete/getDetail。

- [ ] **Step 2: 跑 vue-tsc**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc --noEmit`
Expected: PASS。

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/protocol/response-parser.ts
git commit -m "feat(frontend-skeleton): add protocol/response-parser.ts"
```

## Task C5: 创建 protocol/field-policy.ts

**Files:**
- Create: `app-generator/src/main/resources/frontend-skeleton/src/core/protocol/field-policy.ts`

- [ ] **Step 1: 写 FILTER_PATTERNS_BY_TYPE + isVisible / isReadonly**

按 design §FilterPattern Inference 的固定映射表实现。

```ts
// app-generator/src/main/resources/frontend-skeleton/src/core/protocol/field-policy.ts

import type { ItemDef, FilterPattern } from '@/schema/types'

// Mirror 后端 ItemService#getFilterPatterns 的固定映射
// 当后端新增 ItemType 时，这里加一条
const FILTER_PATTERNS_BY_TYPE: Record<ItemDef['type'], FilterPattern[]> = {
  text:        ['in', 'like'],
  number:      ['in', 'ge', 'gt', 'le', 'lt'],
  time:        ['in', 'dateRange', 'dateTimeRange'],
  select:      ['in'],
  multiSelect: ['in'],
  onOff:       ['in'],
  secret:      []   // secret 不参与查询
}

export function getFilterPatternsByItemType(type: ItemDef['type']): FilterPattern[] {
  return FILTER_PATTERNS_BY_TYPE[type]
}

export type FieldMode = 'search' | 'table' | 'edit-create' | 'edit-update' | 'detail'

export function isVisible(item: ItemDef, mode: FieldMode): boolean {
  switch (mode) {
    case 'search':
      // secret 不参与搜索；其余有 FilterPattern 的类型才显示搜索项
      return getFilterPatternsByItemType(item.type).length > 0
    case 'table':
      // 默认全部显示；若 contract.md / gaps 决议某类字段不在 list 返回，下面加规则
      return true
    case 'edit-create':
      return item.initPattern !== 'doNot'
    case 'edit-update':
      return item.editPattern !== 'doNot'
    case 'detail':
      return true
  }
}

export function isReadonly(item: ItemDef, mode: 'edit-create' | 'edit-update'): boolean {
  const pattern = mode === 'edit-create' ? item.initPattern : item.editPattern
  // todo 字段表示"前端不应让用户填，后端会自动填"——前端应该 readonly 或干脆隐藏
  // ⚠️ 是 readonly 还是隐藏，按 gaps 决议。下面默认 readonly。
  return pattern === 'todo'
}
```

- [ ] **Step 2: 跑 vue-tsc**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc --noEmit`
Expected: PASS。

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/protocol/field-policy.ts
git commit -m "feat(frontend-skeleton): add protocol/field-policy.ts"
```

## Task C6: 改造 CrudPage.vue

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/CrudPage.vue`

- [ ] **Step 1: 替换接口调用为 protocol 层**

完整替换文件内容（保留 template 和 style 不变）：

```vue
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { NButton, useMessage } from 'naive-ui'
import type { PaginationProps } from 'naive-ui'
import type { FormDef } from '@/schema/types'
import request from '@/utils/request'
import type { ApiBaseResult, PageResult } from '@/utils/request'
import { endpointOf } from './protocol/endpoints'
import { buildListRequest, buildSaveRequest } from './protocol/request-builder'
import { parseListRow, parseDetailDto } from './protocol/response-parser'
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'
import EditModal from './EditModal.vue'

const props = defineProps<{
  schema: FormDef
}>()

const message = useMessage()

// 业务主键字段名 — 来自 contract.md §4 验证（form-generator 自动注入 ${formName}Code）
// ⚠️ 实际值按 contract.md 校准，下面是 design 推断
const bizKey = `${props.schema.name.charAt(0).toLowerCase()}${props.schema.name.slice(1)}Code`

const searchParams = ref<Record<string, any>>({})
const tableData = ref<Record<string, any>[]>([])
const tableLoading = ref(false)
const detailLoading = ref(false)
const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const modalVisible = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const formData = ref<Record<string, any>>({})
const submitLoading = ref(false)

async function fetchData() {
  tableLoading.value = true
  try {
    const reqBody = buildListRequest(
      props.schema.items,
      searchParams.value,
      { pageNo: pagination.page!, pageSize: pagination.pageSize! }
    )
    const { data } = await request.post<ApiBaseResult<PageResult<Record<string, any>>>>(
      endpointOf(props.schema.name, 'list'),
      reqBody
    )
    tableData.value = data.result.list.map(dto => parseListRow(props.schema.items, dto))
    pagination.itemCount = data.result.count
  } catch (e: any) {
    message.error(e.message || '查询失败')
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  pagination.page = 1
  fetchData()
}

function handlePaginationUpdate(p: PaginationProps) {
  pagination.page = p.page
  pagination.pageSize = p.pageSize
  fetchData()
}

function handleCreate() {
  modalMode.value = 'create'
  formData.value = {}
  modalVisible.value = true
}

async function handleEdit(row: Record<string, any>) {
  detailLoading.value = true
  try {
    const { data } = await request.post<ApiBaseResult<Record<string, any>>>(
      endpointOf(props.schema.name, 'getDetail'),
      { [bizKey]: row[bizKey] }
    )
    formData.value = parseDetailDto(props.schema.items, data.result)
    modalMode.value = 'edit'
    modalVisible.value = true
  } catch (e: any) {
    message.error(e.message || '加载详情失败')
    // 不打开 modal
  } finally {
    detailLoading.value = false
  }
}

async function handleDelete(row: Record<string, any>) {
  try {
    await request.post<ApiBaseResult>(
      endpointOf(props.schema.name, 'delete'),
      { [bizKey]: row[bizKey] }
    )
    message.success('删除成功')
    fetchData()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    const reqBody = buildSaveRequest(props.schema.items, formData.value, modalMode.value)
    // 在 edit 模式下补上业务主键
    if (modalMode.value === 'edit') {
      reqBody[bizKey] = formData.value[bizKey]
    }
    await request.post<ApiBaseResult>(endpointOf(props.schema.name, 'save'), reqBody)
    message.success(modalMode.value === 'create' ? '创建成功' : '更新成功')
    modalVisible.value = false
    fetchData()
  } catch (e: any) {
    message.error(e.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchData)
</script>
```

template 部分新加 detailLoading 透传给"编辑"按钮（让它在等待 getDetail 时 loading）：

```vue
<template>
  <div class="crud-page">
    <div class="crud-search-card">
      <SearchForm
        :items="schema.items"
        v-model="searchParams"
        @search="handleSearch"
        @reset="handleReset"
      />
    </div>
    <div class="crud-table-card">
      <div class="crud-table-header">
        <h3 class="crud-table-title">{{ schema.title }}</h3>
        <NButton type="primary" @click="handleCreate">新建</NButton>
      </div>
      <DataTable
        :items="schema.items"
        :data="tableData"
        :loading="tableLoading"
        :detail-loading="detailLoading"
        :pagination="pagination"
        @edit="handleEdit"
        @delete="handleDelete"
        @update:pagination="handlePaginationUpdate"
      />
    </div>
    <EditModal
      :visible="modalVisible"
      :mode="modalMode"
      :items="schema.items"
      v-model="formData"
      :loading="submitLoading"
      @update:visible="modalVisible = $event"
      @submit="handleSubmit"
    />
  </div>
</template>
```

style 部分不动。

- [ ] **Step 2: 跑 vue-tsc**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc --noEmit`
Expected: 因为 DataTable 还没有 detailLoading prop，会报错。这是预期，留待 C7 修。**如果用户要求每步 vue-tsc 都过**，本步把 detailLoading prop 添加放到 C7，这里只验证 script 部分。可改为 `npx vue-tsc --noEmit src/core/CrudPage.vue` 单文件（vue-tsc 不一定支持单文件检查；如不支持，跳过本步，等 C7 后整体跑）。

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/CrudPage.vue
git commit -m "feat(frontend-skeleton): wire CrudPage.vue to protocol layer"
```

## Task C7: 改造 SearchForm.vue / EditModal.vue / DataTable.vue

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/SearchForm.vue`
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/EditModal.vue`
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/DataTable.vue`

- [ ] **Step 1: 改 SearchForm.vue —— 用 isVisible 替换硬编码**

把 line 18-24 的 `searchableItems` 替换：

```ts
import { isVisible } from './protocol/field-policy'

const searchableItems = computed(() =>
  props.items.filter(item => isVisible(item, 'search'))
)
```

其余不变。

- [ ] **Step 2: 改 EditModal.vue —— 用 isVisible / isReadonly**

把 line 23-28 的 `visibleItems` 替换：

```ts
import { isVisible, isReadonly } from './protocol/field-policy'

const editMode = computed(() => props.mode === 'create' ? 'edit-create' : 'edit-update')

const visibleItems = computed(() =>
  props.items.filter(item => isVisible(item, editMode.value))
)
```

把 FieldRenderer 调用增加 `:readonly="isReadonly(item, editMode)"` —— 但 FieldRenderer 当前没有 readonly prop。最小改动：先在 EditModal 计算 readonly，但**不传给 FieldRenderer**（让 todo 字段照常出现可编辑）。这是临时对齐策略，等 gaps 决议明确"todo 字段是 readonly 还是隐藏"后再扩 FieldRenderer。或者：直接用 `isVisible` 在 'edit-create'/'edit-update' 模式下过滤掉 doNot 字段，让 todo 字段保留可编辑。

```vue
<NFormItem v-for="item in visibleItems" :key="item.name" :label="item.title" :path="item.name">
  <FieldRenderer
    :item="item"
    mode="edit"
    :value="modelValue[item.name] ?? null"
    @update:value="updateField(item.name, $event)"
  />
</NFormItem>
```

不动 FieldRenderer。**风险**：如果 gaps 决议 todo 字段应 readonly，此处需后续再改 FieldRenderer + 各 Field 组件加 readonly 状态。本计划先不做，记到下一轮。

- [ ] **Step 3: 改 DataTable.vue —— 用 isVisible('table') 过滤列；新增 detailLoading prop**

修改 line 7-13：

```ts
const props = defineProps<{
  items: ItemDef[]
  data: Record<string, any>[]
  loading: boolean
  detailLoading?: boolean
  pagination: PaginationProps
}>()
```

修改 line 21-25 列计算：

```ts
import { isVisible } from './protocol/field-policy'

const visibleItems = computed(() => props.items.filter(item => isVisible(item, 'table')))

const columns = computed<DataTableColumn[]>(() => {
  const visibleList = visibleItems.value
  const totalItems = visibleList.length
  const shouldFreeze = totalItems > 4

  const cols: DataTableColumn[] = visibleList.map((item, index) => ({
    // ...其余不变
  }))
  // 操作列：编辑按钮加 :loading="props.detailLoading"
  cols.push({
    title: '操作',
    key: '_actions',
    width: 150,
    fixed: shouldFreeze ? 'right' : undefined,
    render(row: Record<string, any>) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, type: 'primary', loading: props.detailLoading, onClick: () => emit('edit', row) }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => emit('delete', row) }, {
            trigger: () => h(NButton, { size: 'small', quaternary: true, type: 'error' }, { default: () => '删除' }),
            default: () => '确定要删除吗？'
          })
        ]
      })
    }
  })
  return cols
})

const scrollX = computed(() => {
  if (visibleItems.value.length > 4) {
    return visibleItems.value.length * 150 + 150
  }
  return undefined
})
```

- [ ] **Step 4: 跑 vue-tsc 整体**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc --noEmit`
Expected: PASS。如果有错，逐条修。

- [ ] **Step 5: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/{SearchForm,EditModal,DataTable}.vue
git commit -m "feat(frontend-skeleton): wire SearchForm/EditModal/DataTable to field-policy"
```

## Task C8: 更新 src/app.json 示例 schema

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/app.json`

- [ ] **Step 1: 给示例字段加 filterPatterns**

只是为了让 frontend-skeleton 在脱离 app-generator 单独 dev 时也能展示新功能。把 line 13-22 的 demoField 改为：

```json
{
  "type": "text",
  "name": "demoField",
  "title": "示例字段",
  "isNonVoid": true,
  "initPattern": "userInput",
  "editPattern": "userInput",
  "maxLength": 100,
  "filterPatterns": ["like"]
}
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/app.json
git commit -m "chore(frontend-skeleton): add filterPatterns to app.json demo schema"
```

## Task C9: 视情况裁剪 utils/naming.ts

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/utils/naming.ts`

- [ ] **Step 1: 决定是否保留 deriveApiBasePath**

C2 创建的 endpoints.ts 已经接管了"派生 base path"职责。`deriveApiBasePath` 在 utils/naming.ts 里如果没有其他调用点，可以删除；如果还有，保留并在 endpoints.ts 里复用 `upperCamelToLowerCamel`。

Run: `grep -rn "deriveApiBasePath" app-generator/src/main/resources/frontend-skeleton/src/`
Expected: 只有 utils/naming.ts 自己。如是，删除 `deriveApiBasePath` 函数。

- [ ] **Step 2: 删函数**

把 utils/naming.ts 修改为：

```ts
export function upperCamelToKebab(str: string): string {
  return str
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1-$2')
    .replace(/([a-z\d])([A-Z])/g, '$1-$2')
    .toLowerCase()
}

export function upperCamelToLowerCamel(str: string): string {
  return str.charAt(0).toLowerCase() + str.slice(1)
}
```

- [ ] **Step 3: 跑 vue-tsc 确认无残留引用**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc --noEmit`
Expected: PASS。

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/utils/naming.ts
git commit -m "refactor(frontend-skeleton): drop unused deriveApiBasePath after protocol layer"
```

## Task C10: 联调

**Files:** none

- [ ] **Step 1: 给用户操作指引**

输出给用户：
1. 跑一次 `app-generator` 用 super-dsl 生成完整应用（含真实 app.json）；
2. 启动后端 + 前端，跑 4 个 form 的 list / save / delete / edit 闭环；
3. 把任何 mismatch 反馈给会话。

- [ ] **Step 2: 收到 mismatch 反馈后**

为每个 mismatch：
- 确认它属于 contract 误读（修 contract.md，回到 C1-C7 改对应规则表）还是 gaps 漏决（补 gap，让用户拍板，再修代码）；
- 修完后 commit，commit msg 形如 `fix(frontend-skeleton): align ${formName} ${action} with backend ${reason}`；
- 回到 Step 1 让用户重新联调，直到清单清空。

- [ ] **Step 3: 收尾 commit**

如果联调阶段产生过 contract.md / gaps.md 的修订，做一个收尾 commit：

```bash
git add docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/
git commit -m "docs(aligning): finalize contract and gaps after end-to-end verification"
```

---

## Self-Review Notes

| Spec 要素 | Plan 任务 |
|---|---|
| Working Loop 阶段 1（取证） | Phase A (A1-A3) |
| Working Loop 阶段 2（提炼契约 + 缺口） | Phase B (B1-B6) |
| Working Loop 阶段 3（改前端） | Phase C (C0-C10) |
| Architecture: protocol/endpoints.ts | C2 |
| Architecture: protocol/request-builder.ts | C3 |
| Architecture: protocol/response-parser.ts | C4 |
| Architecture: protocol/field-policy.ts | C5 |
| Schema Type Extension (filterPatterns) | C1 |
| Data Flow: list / save / delete / getDetail | C6 |
| Data Flow: getDetail 失败不开 modal + detailLoading | C6 + C7 |
| Error Handling: protocol 层不包装错误 | C3/C4/C5 实现里体现 |
| Sustainability: sort 参数预留 | C3 buildListRequest |
| Verification: TS 编译 + 联调 + contract.md | 每个 C 任务的 vue-tsc step；C10 联调 |
| Non-Goal: 不为 frontend 写单测 | 全计划无 *.test.ts 任务 |
| Non-Goal: 不读 form-generator 源码 | A1 仅读 dsl 包；不读 service/impl |
| Non-Goal: 不改 form-generator | C0 Step 3 把"如需改 form-generator"列为新 gap |
| Risk: 规则表假设错 | contract.md 实际值替换；C10 联调兜底 |
| Risk: 缺口语义不明 | gaps.md + B6 用户拍板 |
| Risk: form-generator 迭代影响 | protocol 层规则表设计本身就是缓解 |
