# 前端骨架交互优化设计

**日期**：2026-07-05
**范围**：`app-generator/src/main/resources/frontend-skeleton/`
**目标**：迭代前端骨架，优化三类 CRUD 页面交互，并补充二次开发约定文档。

## 背景

前端骨架的 `src/core/` 提供标准 CRUD 能力（CrudPage、SearchForm、EditModal、useCrudPage composable）。当前交互存在三处可优化点：表单校验失败无聚焦提示、筛选区无法回车查询、筛选条件无法通过 URL 分享。本次迭代在不破坏既有协议（secret 三态、protocol 层不变）的前提下补齐这些能力。

## 需求

1. 创建/编辑弹框保存时，若存在字段校验未通过，将焦点移动到第一个校验不通过字段的校验框。
2. 在筛选区的输入框输入内容后按回车直接进行查询。
3. 筛选区输入内容时，自动将值体现在地址栏的 URL query 部分；进入带 URL query 的表单页时，直接按条件进行查询。目的是分享浏览器地址时同时分享筛选条件。
4. 将本次优化涉及的后续迭代注意事项简洁地写到合适层级的 CLAUDE.md。

## 关键决策（已与用户确认）

- **URL query 同步范围**：业务字段 + 创建时间范围，时间用格式化字符串（与组件 `formatted-value` 口径一致）。
- **进入带 query 页面**：自动触发查询，无需用户再点查询按钮。
- **同步时机**：双向实时，输入侧 debounce 300ms；前进/后退/分享链接变化时回填并查询。
- **校验聚焦方案**：通用 DOM 查询，不为每个 field 组件暴露 focus 方法。
- **回车查询范围**：仅 text/number/time 纯输入框；select/multiSelect 不绑定（避免与下拉选中冲突）。
- **URL 同步实现位置**：集中在 `useCrudPage` composable 内，覆盖所有走 composable 的页面（含 CrudPage、AuditLogPage 及未来覆盖页）。
- **AuditLogPage**：也加 URL query 同步（它走 useCrudPage，自动获得）。
- **query key 命名**：字段名直用（如 `?name=foo&status=ACTIVE`），时间用 `{name}Start`/`{name}End`，创建时间用 `createdAtStart`/`createdAtEnd`；multiSelect 用逗号分隔。

## 设计

### §1 校验失败聚焦到首个出错字段

**改动文件**：`src/core/EditModal.vue`

**实现**：

1. 为 `visibleItems` 渲染的 `NFormItem` 收集 DOM 引用。采用函数 ref 将每个 NFormItem 的根 DOM 存入一个 `Map<string, HTMLElement>`（key 为 `item.name`）。NFormItem 渲染出的根元素含 `.n-form-item` 类。
2. 改造 `handleSubmit`：
   - `await formRef.value?.validate()` 成功则照常 `emit('submit')`。
   - catch 到 reject 时，Naive UI v2.41 的 `validate()` reject 值结构为 `{ [path: string]: Array<{ field, message }> }`。按 `visibleItems.value` 顺序遍历，找到第一个 `errors[item.name]` 存在且该字段可编辑（`isEditable(item, editMode)`）的项。
   - 在该项 NFormItem 根 DOM 上执行 `querySelector('input, [tabindex]:not([tabindex="-1"])')`，取第一个匹配元素并 `.focus()`。
   - 用 `nextTick` 包裹 focus，确保校验错误 UI（红色边框）已渲染。
3. **兜底**：若 reject 值结构异常（拿不到结构化 errors），退化为遍历 visibleItems，对每个可编辑字段查 DOM 内是否存在 `.n-form-item-feedback__line`（Naive UI 错误提示）或 `.n-form-item--error` 类，定位第一个出错项。

**约束**：

- 不改动 secret 三态协议（`isSecretExemptFromRequired` 仍生效，secret 在 edit-update 下不生成 required 规则，自然不会成为"首个出错字段"）。
- 聚焦只针对可见且可编辑字段，与 rules 生成口径一致。
- 不改 `protocol/` 任何文件。

### §2 筛选区输入框回车查询

**改动文件**：`src/core/SearchForm.vue`

**实现**：

1. 在 search 模式的 `NFormItem` 内，对 `['text', 'number', 'time'].includes(item.type)` 的字段，在其 FieldRenderer 外层包一个 `<div @keyup.enter="emit('search')">`（或直接在 NFormItem 上绑定，因 NFormItem 不透传原生事件时用 wrapper div 更稳妥）。
2. select/multiSelect/onOff/file 字段不绑定回车。
3. 创建时间 datetimerange 选择器不绑定（非文本输入）。
4. 利用键盘事件冒泡：NInput/NInputNumber/NDatePicker 内部 input 的回车事件冒泡到 wrapper 触发 search。

**约束**：

- 不改任何 field 组件（TextField/NumberField/TimeField 等）。
- 回车查询等价于点击「查询」按钮，复用 `emit('search')` → 父组件 `handleSearch`（重置 page=1 + fetchData）。

### §3 URL query 双向实时同步

**改动文件**：`src/core/composables/useCrudPage.ts`

**Router 依赖**：当前 router 用 `createWebHashHistory()`，URL 形如 `/#/users?name=foo`，query 在 hash 之后。通过 `useRoute()`/`useRouter()` 读写 `route.query`。composable 内 `import { useRoute, useRouter } from 'vue-router'`。

**双向同步逻辑**：

#### 序列化（searchParams → URL）

`watch(searchParams, handler, { deep: true })` + debounce 300ms。

序列化规则（按 schema.items 中可见 search 字段）：

| 字段类型 | 序列化 |
|---|---|
| text | `v != null && v !== ''` → `query[name] = String(v)`；否则删除 key |
| number | 同上，转 String |
| select | 同上 |
| multiSelect | 非空数组 → `query[name] = v.join(',')`；空数组删除 key |
| time | `[start,end]` 字符串数组（TimeField search 模式经 `formatted-value` 已返回 `yyyy-MM-dd`/`HH:mm:ss`/`yyyy-MM-dd HH:mm:ss` 格式串）→ `query[name+'Start']`、`query[name+'End']`；无值删除 |
| secret | 不参与（search 不可见） |
| onOff | search 不可见，不参与 |
| file | search 不可见，不参与 |

创建时间范围（SearchForm 写入 formState 的 `createdAtStart`/`createdAtEnd`/`_createdAtRange`）：
- 有值 → `query.createdAtStart`、`query.createdAtEnd`（与现有 `fmt()` 格式一致：`yyyy-MM-dd HH:mm:ss`）
- 无值 → 删除两个 key
- 反序列化时 `_createdAtRange` 重建为 `[Date.parse(start), Date.parse(end)]`（NDatePicker 用 timestamp 回显，与 SearchForm 现有写入口径一致）

写入方式：`router.replace({ query })`，避免输入过程污染浏览器历史。

**循环防护**：序列化写 URL 前置 `internal = true` flag，写完清掉；序列化前比较"新 query 与当前 route.query 的 JSON 串"是否变化，无变化则不 replace。

#### 反序列化（URL → searchParams）

`watch(() => route.query, handler)`。

按 schema.items 顺序解析：

| 字段类型 | 反序列化 |
|---|---|
| text | `query[name]` 存在 → 字符串；否则 null |
| number | `query[name]` 存在 → `Number(v)`（NaN 视为 null）；否则 null |
| select | 同 text |
| multiSelect | `query[name]` 存在 → `v.split(',')`；否则 null |
| time | `query[name+'Start']` 或 `query[name+'End']` 存在 → `[start, end]`（字符串，任一缺失对应位 `''`）；否则 null |

创建时间：`query.createdAtStart`/`query.createdAtEnd` 存在 → 同时写 `searchParams.createdAtStart`/`createdAtEnd`（字符串）和 `_createdAtRange`（`[Date.parse(start), Date.parse(end)]`，供 NDatePicker 回显）；缺失则删除三者。

写入 `searchParams.value`（设 `internal` flag 跳过序列化 watch），然后 `pagination.page = 1` + `fetchData()`。

**循环防护**：反序列化写 searchParams 前置 `internal` flag，跳过序列化 watch。

#### 初始化

`onMounted`：先从 `route.query` 反序列化回填 `searchParams`（若有 query），再 `fetchData()`。替换原 `onMounted(fetchData)`。

#### 路由切换

现有 `watch(() => getSchema().name)`（Vue Router 复用组件实例时重新加载）：保持清空 `searchParams`/`sortState`/重置 page 的逻辑，额外 `router.replace({ query: {} })` 清掉 URL query，避免上个页面 query 残留。注意避免与序列化 watch 的 replace 冲突（用 internal flag）。

#### 边界

- number 字段从 URL 读回时转回 number；空字符串视为未设置。
- 反序列化时若 query 中某个 key 不在 schema 可见 search 字段内（如遗留的旧字段），忽略。
- `router.replace` 失败（如 NavigationDuplicated）静默捕获，不打扰用户。

### §4 文档更新

按 CLAUDE.md 维护规则，本次"变更了 core/ 组件交互约定"，在 `app-generator/src/main/resources/frontend-skeleton/CLAUDE.md` 追加一节 **「筛选与校验交互约定」**（建议放在「Secret 字段编辑交互」之后、「功能权限体系」之前），简述：

1. **校验失败聚焦**：EditModal `handleSubmit` 校验失败时自动 focus 首个出错字段（按 visibleItems 顺序，通用 DOM 查询）。覆盖 EditModal 时若改提交逻辑需保留此行为。
2. **回车查询**：SearchForm 对 text/number/time 字段绑定 `@keyup.enter` 触发 search；select/multiSelect 不绑定。新增 search 字段类型时需评估是否纳入回车。
3. **URL query 同步**：`useCrudPage` 双向同步 searchParams 与 `route.query`（debounce 300ms，`router.replace` 不污染历史），覆盖所有走 composable 的页面；key 用字段名，时间用 `{name}Start/End` 与 `createdAtStart/End`，multiSelect 逗号分隔。**覆盖页若不使用 useCrudPage 则不获得此能力**。

只写约定层面，不写技术实现细节。根 CLAUDE.md 不改（模块地图/构建命令未变）。

## 影响面

- **改动文件**：
  - `src/core/EditModal.vue`（§1）
  - `src/core/SearchForm.vue`（§2）
  - `src/core/composables/useCrudPage.ts`（§3）
  - `app-generator/src/main/resources/frontend-skeleton/CLAUDE.md`（§4）
- **不改动的协议层**：`protocol/`（endpoints、request-builder、response-parser、field-policy）全部不动。secret 三态、field-policy 可见性逻辑不动。
- **覆盖页影响**：`RolePage.vue`/`UserPage.vue`/`AuditLogPage.vue` 走 `useCrudPage`，自动获得 §2/§3 能力；§1 改在 EditModal，这些页面只要用 EditModal 也自动获得。
- **测试**：现有 `tests/schema/`、`tests/utils/` 是纯函数测试，不涉及交互。本次改动主要是组件交互逻辑，不强制新增单元测试（vitest + @vue/test-utils 可选加 URL 序列化/反序列化纯函数测试，若抽取为独立工具函数则纳入）。

## 风险与对策

1. **双向 watch 循环**：用 `internal` flag + query 变化比较双重防护。
2. **Naive UI validate reject 值结构不稳定**：兜底用 DOM 错误类定位。
3. **router.replace 频繁触发**：debounce 300ms + 变化比较，避免无意义写入。
4. **hash 模式下 query 解析**：vue-router 的 `useRoute().query` 已正确解析 hash 之后的 query，无需特殊处理。
5. **覆盖页不用 useCrudPage**：明确文档说明不获得 URL 同步能力（如未来新增此类页面需自行实现）。
