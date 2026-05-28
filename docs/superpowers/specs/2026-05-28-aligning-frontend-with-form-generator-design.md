# Aligning frontend-skeleton With form-generator Output

**Date**: 2026-05-28
**Author**: brainstorming session
**Status**: design

## Background

`app-generator` 在生成应用时会调用 `form-generator` 产出后端 CRUD 代码（controller、req/resp DTO、enum、entity、mapper 等）。`app-generator` 同时把 `frontend-skeleton`（Vue3 + Naive UI）作为前端骨架复制出去，并把 `AppDef` 序列化成 `app.json` 供前端运行时驱动渲染。

当前 `frontend-skeleton` 的接口调用是基于猜测的，与 `form-generator` 实际生成的后端代码并未对齐：

- `src/utils/naming.ts` 里 `deriveApiBasePath` 注释明确写着 "Actual rule to be confirmed with Allison1875 backend code generation logic"；
- `CrudPage.vue` 里 4 个接口调用 (`/list`、`/save`、`/delete`、未实现的 detail) 路径与请求体均为占位；
- `delete` 用 `{ id: row.id }`，但后端实际业务主键由 `form-generator` 自动注入为 `${formName}Code`；
- `SearchForm.vue` 把 `formState` 原样发给 `/list`，未考虑后端 `FilterPattern` 衍生的字段名（`xxxKw` / `xxxBegin&End` / `xxxes`）；
- 编辑流程当前直接复用列表行数据，未调 getDetail，对 secret 等"列表脱敏、详情明文"的字段语义错误。

## Goal

让 `frontend-skeleton` 的 list / save / delete / getDetail 四个接口的 **URL、请求体、响应体** 严格对齐 `form-generator` 生成的后端代码，并且 SearchForm 的查询参数严格遵循后端 list 入参 DTO（含 `FilterPattern` 衍生形式）。

## Non-Goals

- 不读 `form-generator` 源码以推断接口契约（上下文成本过高，已废弃）。改用"超级 DSL → form-generator 实际生成 java → 读生成代码"的取证方式。
- 不为 frontend-skeleton 写任何单测；联调即验证手段。
- 不引入构建期插件或代码生成。frontend-skeleton 维持"运行时 schema 驱动"的现状。
- 不重构当前 frontend-skeleton 已有的组件分层（CrudPage / SearchForm / EditModal / DataTable / fields/*）。

## Approach

### Working Loop

工作分三阶段，每阶段产物明确：

**阶段 1 —— 取证据**
- 产出 1 份"超级 DSL"（YAML，3-4 个 form），覆盖以下维度：
  - 7 种 ItemType (text / number / select / multiSelect / time / onOff / secret) 的所有子变体（multiline、regex、decimal、negative、date|time|dateTime、desensitization|hidden、单选 vs multi）；
  - form-generator 所有 `FilterPattern` 枚举值；
  - `is_non_void` 的 true/false 组合；
  - `init_pattern` / `edit_pattern` 的 doNot / userInput / todo；
  - `check_duplicate` 字段；
  - 复合索引 `indices` 多字段；
  - 自动注入的审计字段 (`${formName}Code` / `createdAt` / `updatedAt`) 的实际效果。
- 同时产出"DSL 设计说明"，标注每个 form 用来覆盖哪些维度。
- 用户执行 `form-generator`，把生成的 controller / req&resp DTO / enum 三类文件留在本地某个绝对路径，把路径告诉本会话。**不在对话里贴源码**；本会话用 Read/Glob 自取。**不读** 持久层 / 业务层。

**阶段 2 —— 提炼契约 + 缺口**
- 产出 `contract.md`：每个 form 的 4 个接口的 URL、入参字段表、出参字段表，以及枚举类型定义。这是 protocol 层的规格输入。
- 产出 `gaps.md`：所有"前端需要但后端没暴露"或"前后端语义不明"的项目，每项给出建议处理方式（前端硬编码 / 改 form-generator / 妥协）。
- 用户审阅两份文档并逐项拍板。
- 这两份产物建议放在 `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/` 子目录下，与本 design 同名根。

**阶段 3 —— 改前端**
- 改动清单见下面 "Changes" 节。
- 用户跑前后端联调，反馈 mismatch。

### Architecture: Protocol Layer

新增 `src/core/protocol/` 目录，集中所有"前端 schema ↔ 后端 DTO"的翻译。组件层不再直接拼 URL/参数。

```
src/core/protocol/
  endpoints.ts         # 给定 formName + action → URL
  request-builder.ts   # buildListRequest / buildSaveRequest
  response-parser.ts   # parseListRow / parseDetailDto
  field-policy.ts      # isVisible(item, mode) / isReadonly(item, mode)
```

### Design Principle: Dispatcher + Rule Tables

每个文件内部用 **对象字面量规则表 + 纯函数**：

```ts
const filterPatternRules: Record<FilterPattern, (item, value) => Record<string, any>> = {
  kwLike: (item, v) => ...,
  range:  (item, v) => ...,
  in:     (item, v) => ...,
  eq:     (item, v) => ...,
  // 具体键名后缀以阶段 2 contract.md 为准
}
```

- 对象字面量：新增 ItemType / FilterPattern 时 TypeScript 在编译期会强制提示漏掉的规则；
- 纯函数：无副作用、易调试，组件层调用点也保持简单；
- 不引入 class、注册中心、插件机制 —— 规则条目数小且固定，不需要这种灵活性。

### Sustainability for Future form-generator Iterations

`form-generator` 后续会迭代（明确的近期规划：list 接口支持排序）。protocol 层的设计目标是"未来一次后端协议迭代 = 前端在规则表里加/改一条规则"：

- `buildListRequest(items, formState, pagination, sort?)` 预留 `sort` 参数；
- 新 FilterPattern → filterPatternRules 表加一条；
- 新 ItemType → response-parser 与 field-policy 各加一条；
- 不为更远的未来做更多预留，遵循"不透支可扩展性"原则。

### Data Flow

**list（页面初载 / 搜索 / 翻页）**
```
SearchForm 收集 formState  →  CrudPage.fetchData
  → buildListRequest(items, formState, pagination)
  → POST endpointOf(formName, 'list')
  → resp.result.list.map(dto => parseListRow(items, dto))
  → DataTable 渲染
```

**save（新建 / 编辑提交）**
```
EditModal 收集 formState  →  CrudPage.handleSubmit
  → buildSaveRequest(items, formState, mode)
  → POST endpointOf(formName, 'save')
  → 关闭 modal + 重新 fetchData
```

**delete**
```
DataTable 行操作  →  CrudPage.handleDelete
  → POST endpointOf(formName, 'delete') with { [bizKey]: row[bizKey] }
  → 重新 fetchData
```

bizKey 实际名称（预计是 `${formName}Code`）由 contract.md 在阶段 2 确认。

**getDetail（点击编辑时）**
```
DataTable 行操作 → CrudPage.handleEdit
  → 设 detailLoading
  → POST endpointOf(formName, 'getDetail') with { [bizKey]: row[bizKey] }
  → parseDetailDto(items, resp.result)
  → 填 EditModal、打开
  失败：message.error，不打开 modal
```

### Error Handling

- `src/utils/request.ts` 的 axios 拦截器策略不变（200 走业务、401 跳登录、其他 reject）。
- protocol 层不包装错误，原样冒泡。
- getDetail 失败时编辑流程中断，不打开 modal —— 避免用户在空表单上编辑。

### FilterPattern Inference

form-generator 已将 `filterPatterns` 从 DSL 中移除，改为按 ItemType 在 `ItemService#getFilterPatterns` 里固定返回：

| ItemType    | 支持的 FilterPatterns（后端固定）                 |
|-------------|--------------------------------------------------|
| text        | `in`, `like`                                     |
| number      | `in`, `ge`, `gt`, `le`, `lt`                     |
| time        | `in`, `dateRange`, `dateTimeRange`               |
| select      | `in`                                             |
| multiSelect | `in`                                             |
| onOff       | `in`                                             |
| secret      | 无（不可作为过滤条件）                            |

因此：
- **`src/schema/types.ts` 不需要新增 `filterPatterns` 字段**；app.json 中也不含此字段。
- **前端 `field-policy.ts` 维护一份 `FILTER_PATTERNS_BY_TYPE` 常量表**，与上表一致，替代从 schema 读取。
- SearchForm 里判断某 item 是否显示为搜索条件，直接查 `FILTER_PATTERNS_BY_TYPE[item.type]` 是否非空。

## Changes

### app-generator/src/main/resources/frontend-skeleton/

**新增**
- `src/core/protocol/endpoints.ts`
- `src/core/protocol/request-builder.ts`
- `src/core/protocol/response-parser.ts`
- `src/core/protocol/field-policy.ts`

**修改**
- `src/schema/types.ts`：新增 `FilterPattern` 类型（从后端 enum 翻译，固定集合）。`ItemDefBase` **不加** `filterPatterns` 字段。
- `src/core/CrudPage.vue`：4 个接口调用走 protocol 层；handleEdit 改 async + detailLoading + 失败不开 modal；handleDelete 改用 bizKey。
- `src/core/SearchForm.vue`：去掉对 secret/time 类型的 filter 硬编码 (line 19-24)，改走 `field-policy.isVisible(item, 'search')`；提交查询时不直接用 modelValue，而是经 `buildListRequest`（在 CrudPage 里）。
- `src/core/EditModal.vue`：可见性/只读规则走 `field-policy.isVisible(item, mode)` / `isReadonly(item, mode)`。
- `src/core/DataTable.vue`：列可见性走 `field-policy.isVisible(item, 'table')`。
- `src/app.json`：示例 schema 跟着新字段更新。
- `src/utils/naming.ts`：`deriveApiBasePath` 注释更新；具体规则按 contract.md 校准；可能整体迁入 `core/protocol/endpoints.ts`，待阶段 3 决定。

### app-generator/src/main/java/

无需改动。`filterPatterns` 已从 form-generator DSL 移除，前端从 ItemType 直接推导，不依赖序列化结果。

## Verification

不为 frontend-skeleton 写任何单测。验证手段：

- TypeScript 编译期类型检查（`Record<FilterPattern, ...>` 等规则表会在编译期强制提示漏项）；
- 用户跑前后端联调，反馈 mismatch；
- protocol 层规则的正确性以阶段 2 的 `contract.md` 为准，contract.md 由读真实生成代码得到，不靠运行时断言。

## Open Questions

无。设计阶段所有决策均已闭环。阶段 2 的 `gaps.md` 是工作产物（取证之后的发现），不属于设计本身的 open question。

## Risk

- **风险 1：form-generator 实际生成的接口形式与本设计的规则表假设不一致。**
  缓解：阶段 2 contract.md 是规则表的唯一真相来源；规则表里所有键名/形式（如 `xxxKw` / `xxxBegin&End`）目前都是占位推测，全部以 contract.md 为准。
- **风险 2：发现后端没暴露的语义信息（典型：list secret 字段返回的字符串无法判断是脱敏值还是明文）。**
  缓解：阶段 2 gaps.md 集中收集，逐项决策（前端硬编码 / 改 form-generator / 妥协）。
- **风险 3：form-generator 后续迭代影响已对齐的代码。**
  缓解：protocol 层"分发器 + 规则表"的设计就是为这个风险服务的；不为更远的未来做额外预留。
