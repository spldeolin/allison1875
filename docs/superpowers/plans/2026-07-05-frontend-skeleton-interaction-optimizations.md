# 前端骨架交互优化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为前端骨架补齐三类 CRUD 交互：校验失败聚焦首个出错字段、筛选区回车查询、筛选条件与 URL query 双向实时同步，并更新二次开发文档。

**Architecture:** 改动集中在 `src/core/`：EditModal 增加校验失败 DOM 聚焦；SearchForm 对 text/number/time 字段绑定 `@keyup.enter`；useCrudPage composable 内集中处理 URL query 双向同步（抽取纯函数 `protocol/query-sync.ts` 便于单测）。不动 `protocol/` 既有文件（endpoints/request-builder/response-parser/field-policy）与 secret 三态协议。

**Tech Stack:** Vue 3.5 + TypeScript + Naive UI 2.41 + vue-router 4 (hash history) + vitest 2.1 + @vue/test-utils 2.4 + happy-dom

**Spec:** `docs/superpowers/specs/2026-07-05-frontend-skeleton-interaction-optimizations-design.md`

**Working directory:** `app-generator/src/main/resources/frontend-skeleton/`（下文路径均相对此根，除非另注）

**Test command:** `npx vitest run`（在该目录下）。typecheck：`npx vue-tsc -b`。

---

## File Structure

| 文件 | 责任 | 改动 |
|---|---|---|
| `src/core/protocol/query-sync.ts` | 纯函数：searchParams ↔ URL query 序列化/反序列化 | 新建 |
| `tests/protocol/query-sync.test.ts` | query-sync 纯函数单测 | 新建 |
| `src/core/composables/useCrudPage.ts` | 引入 query-sync，双向 watch + 初始化回填 + 路由切换清 query | 修改 |
| `src/core/SearchForm.vue` | text/number/time 字段 `@keyup.enter` 触发 search | 修改 |
| `src/core/EditModal.vue` | NFormItem 收集 ref，校验失败聚焦首个出错字段 | 修改 |
| `app-generator/src/main/resources/frontend-skeleton/CLAUDE.md` | 追加「筛选与校验交互约定」节 | 修改 |

---

### Task 1: 新建 query-sync 纯函数 + 单测

**Files:**
- Create: `src/core/protocol/query-sync.ts`
- Create: `tests/protocol/query-sync.test.ts`

- [ ] **Step 1: 写失败的单测**

创建 `tests/protocol/query-sync.test.ts`：

```typescript
import { describe, it, expect } from 'vitest'
import { serializeSearchToQuery, parseQueryToSearch } from '@/core/protocol/query-sync'
import type { ItemDef } from '@/schema/types'

function textItem(name: string): ItemDef {
  return { name, title: name, type: 'text', isNonVoid: true } as unknown as ItemDef
}
function numberItem(name: string): ItemDef {
  return { name, title: name, type: 'number', isNonVoid: true } as unknown as ItemDef
}
function selectItem(name: string): ItemDef {
  return { name, title: name, type: 'select', isNonVoid: true, options: [] } as unknown as ItemDef
}
function multiSelectItem(name: string): ItemDef {
  return { name, title: name, type: 'multiSelect', isNonVoid: true, options: [] } as unknown as ItemDef
}
function timeItem(name: string): ItemDef {
  return { name, title: name, type: 'time', isNonVoid: true, format: 'dateTime' } as unknown as ItemDef
}

const items: ItemDef[] = [
  textItem('name'),
  numberItem('age'),
  selectItem('status'),
  multiSelectItem('tags'),
  timeItem('createdAt'),
]

describe('serializeSearchToQuery', () => {
  it('serializes text/number/select/multiSelect/time', () => {
    const formState = {
      name: 'foo',
      age: 30,
      status: 'ACTIVE',
      tags: ['a', 'b'],
      createdAt: ['2026-07-05 00:00:00', '2026-07-05 23:59:59'],
    }
    expect(serializeSearchToQuery(items, formState)).toEqual({
      name: 'foo',
      age: '30',
      status: 'ACTIVE',
      tags: 'a,b',
      createdAtStart: '2026-07-05 00:00:00',
      createdAtEnd: '2026-07-05 23:59:59',
    })
  })

  it('omits null/empty/blank values', () => {
    const formState = { name: '', age: null, status: undefined, tags: [], createdAt: null }
    expect(serializeSearchToQuery(items, formState)).toEqual({})
  })

  it('serializes createdAtStart/createdAtEnd range', () => {
    const formState = {
      createdAtStart: '2026-07-05 00:00:00',
      createdAtEnd: '2026-07-05 23:59:59',
      _createdAtRange: [1751673600000, 1751762399000],
    }
    expect(serializeSearchToQuery(items, formState)).toEqual({
      createdAtStart: '2026-07-05 00:00:00',
      createdAtEnd: '2026-07-05 23:59:59',
    })
  })

  it('omits createdAt range when partial', () => {
    expect(serializeSearchToQuery(items, { createdAtStart: '2026-07-05 00:00:00' })).toEqual({})
  })
})

describe('parseQueryToSearch', () => {
  it('parses text/number/select/multiSelect/time', () => {
    const query = {
      name: 'foo',
      age: '30',
      status: 'ACTIVE',
      tags: 'a,b',
      createdAtStart: '2026-07-05 00:00:00',
      createdAtEnd: '2026-07-05 23:59:59',
    }
    const result = parseQueryToSearch(items, query)
    expect(result).toMatchObject({
      name: 'foo',
      age: 30,
      status: 'ACTIVE',
      tags: ['a', 'b'],
      createdAt: ['2026-07-05 00:00:00', '2026-07-05 23:59:59'],
      createdAtStart: '2026-07-05 00:00:00',
      createdAtEnd: '2026-07-05 23:59:59',
    })
    // _createdAtRange rebuilt as [number, number] for NDatePicker (TZ-robust: only check shape + numeric)
    expect(Array.isArray(result._createdAtRange)).toBe(true)
    expect(result._createdAtRange).toHaveLength(2)
    expect(typeof result._createdAtRange[0]).toBe('number')
    expect(typeof result._createdAtRange[1]).toBe('number')
  })

  it('returns empty object for empty query', () => {
    expect(parseQueryToSearch(items, {})).toEqual({})
  })

  it('treats NaN number as null', () => {
    expect(parseQueryToSearch(items, { age: 'abc' })).toEqual({ age: null })
  })

  it('parses partial time range as tuple with empty slot', () => {
    expect(parseQueryToSearch(items, { createdAtStart: '2026-07-05 00:00:00' })).toEqual({
      createdAt: ['2026-07-05 00:00:00', ''],
    })
  })

  it('ignores query keys not in schema', () => {
    expect(parseQueryToSearch(items, { unknownField: 'x' })).toEqual({})
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vitest run tests/protocol/query-sync.test.ts`
Expected: FAIL — `Failed to resolve import "@/core/protocol/query-sync"`

- [ ] **Step 3: 实现 query-sync.ts**

创建 `src/core/protocol/query-sync.ts`：

```typescript
// protocol/query-sync.ts
// Bidirectional serialization between search form state and URL query params.
// Field name and time-range suffix rules sourced from request-builder.ts and SearchForm.vue.

import type { ItemDef } from '@/schema/types'

type QueryValue = string | string[] | number | null | undefined
type Query = Record<string, string>

/**
 * Serialize search form state into a flat URL query object.
 * - text/number/select → string (omitted when null/empty/blank)
 * - multiSelect → comma-joined (omitted when empty)
 * - time → split into {name}Start / {name}End (omitted when missing)
 * - createdAtStart/createdAtEnd range → kept as-is when both present
 */
export function serializeSearchToQuery(
  items: ItemDef[],
  formState: Record<string, unknown>
): Query {
  const out: Query = {}
  for (const item of items) {
    const v = formState[item.name]
    switch (item.type) {
      case 'text':
      case 'select':
        if (v != null && v !== '') out[item.name] = String(v)
        break
      case 'number':
        if (v != null && v !== '') out[item.name] = String(v)
        break
      case 'multiSelect':
        if (Array.isArray(v) && v.length > 0) out[item.name] = v.join(',')
        break
      case 'time': {
        if (Array.isArray(v) && v.length === 2) {
          const [start, end] = v as [unknown, unknown]
          if (start != null && end != null && start !== '' && end !== '') {
            out[`${item.name}Start`] = String(start)
            out[`${item.name}End`] = String(end)
          }
        }
        break
      }
      default:
        break
    }
  }
  // Created-at range (SearchForm writes createdAtStart/createdAtEnd strings + _createdAtRange timestamps)
  const cs = formState.createdAtStart
  const ce = formState.createdAtEnd
  if (cs != null && cs !== '' && ce != null && ce !== '') {
    out.createdAtStart = String(cs)
    out.createdAtEnd = String(ce)
  }
  return out
}

/**
 * Parse a URL query object back into search form state.
 * Inverse of serializeSearchToQuery. createdAt range also rebuilds _createdAtRange
 * timestamps (NDatePicker uses timestamps for display).
 */
export function parseQueryToSearch(
  items: ItemDef[],
  query: Record<string, unknown>
): Record<string, unknown> {
  const out: Record<string, unknown> = {}
  for (const item of items) {
    switch (item.type) {
      case 'text':
      case 'select': {
        const raw = query[item.name]
        if (raw != null && raw !== '') out[item.name] = String(raw)
        else out[item.name] = null
        break
      }
      case 'number': {
        const raw = query[item.name]
        if (raw != null && raw !== '') {
          const n = Number(raw)
          out[item.name] = Number.isNaN(n) ? null : n
        } else {
          out[item.name] = null
        }
        break
      }
      case 'multiSelect': {
        const raw = query[item.name]
        if (raw != null && raw !== '') {
          out[item.name] = String(raw).split(',')
        } else {
          out[item.name] = null
        }
        break
      }
      case 'time': {
        const start = query[`${item.name}Start`]
        const end = query[`${item.name}End`]
        if ((start != null && start !== '') || (end != null && end !== '')) {
          out[item.name] = [start != null && start !== '' ? String(start) : '', end != null && end !== '' ? String(end) : '']
        }
        break
      }
      default:
        break
    }
  }
  // Created-at range
  const cs = query.createdAtStart
  const ce = query.createdAtEnd
  if ((cs != null && cs !== '') || (ce != null && ce !== '')) {
    const startStr = cs != null && cs !== '' ? String(cs) : ''
    const endStr = ce != null && ce !== '' ? String(ce) : ''
    out.createdAtStart = startStr || null
    out.createdAtEnd = endStr || null
    const startTs = startStr ? Date.parse(startStr) : NaN
    const endTs = endStr ? Date.parse(endStr) : NaN
    out._createdAtRange = (!Number.isNaN(startTs) || !Number.isNaN(endTs))
      ? [Number.isNaN(startTs) ? null : startTs, Number.isNaN(endTs) ? null : endTs]
      : null
  }
  return out
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vitest run tests/protocol/query-sync.test.ts`
Expected: PASS — all tests green

- [ ] **Step 5: 提交**

```bash
cd app-generator/src/main/resources/frontend-skeleton
git add src/core/protocol/query-sync.ts tests/protocol/query-sync.test.ts
git commit -m "feat: add query-sync pure functions for URL filter serialization"
```

---

### Task 2: useCrudPage 接入 URL query 双向同步

**Files:**
- Modify: `src/core/composables/useCrudPage.ts`

- [ ] **Step 1: 修改 useCrudPage.ts — 引入依赖与 query 同步**

在文件顶部 import 区追加（`watch` 已存在）：

```typescript
import { useRoute, useRouter } from 'vue-router'
import { serializeSearchToQuery, parseQueryToSearch } from '../protocol/query-sync'
```

- [ ] **Step 2: 在 composable 内部、`onMounted(fetchData)` 之前追加 query 同步逻辑**

在 `useCrudPage` 函数体内，找到 `onMounted(fetchData)` 行，替换为以下整段（含 route/router、双向 watch、初始化、路由切换改造）：

```typescript
  const route = useRoute()
  const router = useRouter()
  let syncing = false

  function applyQueryToSearch() {
    const parsed = parseQueryToSearch(getSchema().items, route.query as Record<string, unknown>)
    if (Object.keys(parsed).length === 0) return false
    syncing = true
    searchParams.value = parsed
    pagination.page = 1
    void nextTick(() => { syncing = false })
    return true
  }

  function writeSearchToQuery() {
    if (syncing) return
    const next = serializeSearchToQuery(getSchema().items, searchParams.value)
    const cur = route.query
    const sameKeys = Object.keys(next).length === Object.keys(cur).length
      && Object.entries(next).every(([k, v]) => cur[k] === v)
    if (sameKeys) return
    syncing = true
    router.replace({ query: next }).catch(() => { /* ignore NavigationDuplicated */ })
    void nextTick(() => { syncing = false })
  }

  // 输入 → URL（debounce 300ms）
  let writeTimer: ReturnType<typeof setTimeout> | null = null
  watch(searchParams, () => {
    if (syncing) return
    if (writeTimer) clearTimeout(writeTimer)
    writeTimer = setTimeout(writeSearchToQuery, 300)
  }, { deep: true })

  // URL → 输入（前进/后退/分享链接）
  watch(() => route.query, () => {
    if (syncing) return
    if (applyQueryToSearch()) {
      fetchData()
    }
  })

  // 初始化：优先从 URL query 回填，再 fetchData
  onMounted(() => {
    if (!applyQueryToSearch()) {
      fetchData()
    } else {
      fetchData()
    }
  })
  // 当 schema 切换时（Vue Router 复用组件实例），重新加载数据 + 清 URL query
  watch(() => getSchema().name, () => {
    pagination.page = 1
    searchParams.value = {}
    sortState.value = null
    syncing = true
    router.replace({ query: {} }).catch(() => { /* ignore */ })
    void nextTick(() => { syncing = false })
    fetchData()
  })
```

注意：`nextTick` 需要从 vue import — 检查顶部 import 行，若没有则追加：

```typescript
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
```

- [ ] **Step 3: typecheck**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc -b`
Expected: 无错误。若有 `useRoute`/`useRouter` 在 composable 顶层调用的告警，确认 vue-router 4 支持组合式 API 内调用（支持）。

- [ ] **Step 4: 运行全部测试确认无回归**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vitest run`
Expected: PASS — query-sync 测试 + 既有测试全绿

- [ ] **Step 5: 提交**

```bash
cd app-generator/src/main/resources/frontend-skeleton
git add src/core/composables/useCrudPage.ts
git commit -m "feat: two-way URL query sync for filter conditions in useCrudPage"
```

---

### Task 3: SearchForm 回车查询

**Files:**
- Modify: `src/core/SearchForm.vue`

- [ ] **Step 1: 修改 SearchForm.vue 模板 — 给 text/number/time 字段包回车 wrapper**

找到 search 模式的 `<NFormItem v-for="item in searchableItems" ...>` 块（约第 76-83 行），替换为：

```vue
        <NFormItem v-for="item in searchableItems" :key="item.name" :label="item.title">
          <div
            :class="{ 'search-enter-wrap': ['text', 'number', 'time'].includes(item.type) }"
            @keyup.enter="['text', 'number', 'time'].includes(item.type) ? emit('search') : undefined"
          >
            <FieldRenderer
              :item="item"
              mode="search"
              :value="modelValue[item.name] ?? null"
              @update:value="updateField(item.name, $event)"
            />
          </div>
        </NFormItem>
```

- [ ] **Step 2: 追加 wrapper 样式（让 wrapper 不破坏原有布局）**

在 `<style scoped>` 末尾追加：

```css
.search-enter-wrap {
  display: contents;
}
```

注：`display: contents` 让 wrapper 不产生盒子，NFormItem 内部布局不变；同时仍能接收冒泡的 keyup 事件。

- [ ] **Step 3: typecheck**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc -b`
Expected: 无错误

- [ ] **Step 4: 提交**

```bash
cd app-generator/src/main/resources/frontend-skeleton
git add src/core/SearchForm.vue
git commit -m "feat: enter-to-search on text/number/time filter inputs"
```

---

### Task 4: EditModal 校验失败聚焦首个出错字段

**Files:**
- Modify: `src/core/EditModal.vue`

- [ ] **Step 1: 修改 EditModal.vue — 收集 NFormItem DOM ref 并在 handleSubmit 中聚焦**

在 `<script setup>` 内，`const formRef = ref<FormInst | null>(null)` 之后追加：

```typescript
const itemRefs = new Map<string, HTMLElement>()

function setItemRef(name: string) {
  return (el: Element | { $el?: HTMLElement } | null) => {
    if (el) {
      const dom = (el as { $el?: HTMLElement }).$el ?? (el as HTMLElement)
      itemRefs.set(name, dom as HTMLElement)
    } else {
      itemRefs.delete(name)
    }
  }
}
```

在 `handleSubmit` 内替换 `try { await formRef.value?.validate() } catch { return }` 为：

```typescript
async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch (errors) {
    await nextTick()
    focusFirstError(errors)
    return
  }
  emit('submit')
}

function focusFirstError(errors: unknown) {
  const errObj = (errors && typeof errors === 'object' ? errors : {}) as Record<string, unknown>
  for (const item of visibleItems.value) {
    if (!isEditable(item, editMode.value)) continue
    if (!errObj[item.name]) continue
    const dom = itemRefs.get(item.name)
    if (!dom) continue
    const focusable = dom.querySelector('input, [tabindex]:not([tabindex="-1"])') as HTMLElement | null
    focusable?.focus()
    return
  }
  // 兜底：未匹配到结构化 errors 时，按 DOM 错误类定位
  for (const item of visibleItems.value) {
    if (!isEditable(item, editMode.value)) continue
    const dom = itemRefs.get(item.name)
    if (!dom) continue
    if (dom.classList.contains('n-form-item--error') || dom.querySelector('.n-form-item--error')) {
      const focusable = dom.querySelector('input, [tabindex]:not([tabindex="-1"])') as HTMLElement | null
      focusable?.focus()
      return
    }
  }
}
```

确保 `nextTick` 已 import — 检查顶部 `import { computed, ref, reactive, watch } from 'vue'`，追加 `nextTick`：

```typescript
import { computed, ref, reactive, watch, nextTick } from 'vue'
```

- [ ] **Step 2: 模板 — 给 NFormItem 挂 :ref**

找到 `<NFormItem v-for="item in visibleItems" :key="item.name" ...>`（约第 101-107 行），在 `:key` 之后追加 `:ref="setItemRef(item.name)"`。完整起见，把该 NFormItem 起始标签改为：

```vue
          <NFormItem
            v-for="item in visibleItems"
            :key="item.name"
            :ref="setItemRef(item.name)"
            :label="item.title"
            :path="isEditable(item, editMode) ? item.name : undefined"
            :required="item.isNonVoid && isEditable(item, editMode) && !isSecretExemptFromRequired(item, editMode)"
          >
```

- [ ] **Step 3: typecheck**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vue-tsc -b`
Expected: 无错误。若 `:ref` 函数式签名有类型告警，确认 vue 3.5 支持函数 ref（支持）。

- [ ] **Step 4: 运行全部测试确认无回归**

Run: `cd app-generator/src/main/resources/frontend-skeleton && npx vitest run`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
cd app-generator/src/main/resources/frontend-skeleton
git add src/core/EditModal.vue
git commit -m "feat: focus first invalid field on edit modal submit"
```

---

### Task 5: 更新 frontend-skeleton CLAUDE.md

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/CLAUDE.md`

- [ ] **Step 1: 在「Secret 字段编辑交互」节之后、「功能权限体系」节之前插入新节**

找到 `## 功能权限体系` 行，在其**之前**插入：

```markdown
## 筛选与校验交互约定

### 校验失败聚焦

`EditModal.handleSubmit` 在 `NForm.validate()` reject 时，按 `visibleItems` 顺序找到第一个出错的可编辑字段，通过 DOM 查询其内部 `input` / `[tabindex]` 元素并 `.focus()`。覆盖 EditModal 时若改提交逻辑需保留此行为。

### 回车查询

`SearchForm` 对 `text`/`number`/`time` 类型的 search 字段绑定 `@keyup.enter` 触发 search 事件；`select`/`multiSelect` 不绑定（避免与下拉选中冲突）。新增 search 字段类型时需评估是否纳入回车。

### URL query 双向同步

`useCrudPage` 双向同步 `searchParams` 与 `route.query`（hash 模式下 query 在 `#` 之后）：

- 输入 → URL：debounce 300ms，`router.replace` 不污染浏览器历史
- URL → 输入：前进/后退/分享链接变化时回填 `searchParams` 并自动 `fetchData`
- 初始化：`onMounted` 从 `route.query` 回填后查询
- 路由切换：清空 `searchParams` 并 `router.replace({ query: {} })`

key 命名：字段名直用（`?name=foo&status=ACTIVE`），时间用 `{name}Start`/`{name}End` 与 `createdAtStart`/`createdAtEnd`，`multiSelect` 逗号分隔。序列化逻辑在 `protocol/query-sync.ts`。

**覆盖页若不使用 `useCrudPage` 则不获得此能力**，需自行实现。
```

- [ ] **Step 2: 提交**

```bash
git add app-generator/src/main/resources/frontend-skeleton/CLAUDE.md
git commit -m "docs: document filter and validation interaction conventions in frontend skeleton CLAUDE.md"
```

---

### Task 6: 全量验证

- [ ] **Step 1: 全量 typecheck + 测试**

Run:
```bash
cd app-generator/src/main/resources/frontend-skeleton
npx vue-tsc -b && npx vitest run
```
Expected: typecheck 无错误，所有测试通过

- [ ] **Step 2: 检查 git 状态干净**

Run: `git status`
Expected: nothing to commit, working tree clean

- [ ] **Step 3: 人工交互验证清单（生成后项目中）**

由于骨架无独立运行实例，验证在生成后的项目中执行。生成一个测试应用后启动 `npm run dev`，逐项验证：

1. 校验聚焦：打开创建/编辑弹框，留空必填字段点确定 → 焦点落在第一个出错字段
2. 回车查询：在 text/number/time 筛选框输入内容按回车 → 触发查询
3. URL 同步输入→URL：输入筛选条件 → 约 300ms 后地址栏 URL query 更新
4. URL 同步 URL→输入：直接修改地址栏 query 回车 → 筛选框回填并自动查询
5. 分享链接：复制带 query 的 URL 到新标签页打开 → 自动按条件查询
6. select 回车不查询：在 select 筛选框按回车 → 不触发查询（仅选中高亮项）
7. 路由切换：切换菜单 → URL query 清空，新页面无残留筛选

---

## Self-Review

**1. Spec coverage:**
- §1 校验聚焦 → Task 4 ✓
- §2 回车查询 → Task 3 ✓
- §3 URL query 双向同步 → Task 1（纯函数）+ Task 2（composable 接入）✓
- §4 文档更新 → Task 5 ✓
- 全量验证 → Task 6 ✓

**2. Placeholder scan:** 无 TBD/TODO；每个 step 含完整代码或确切命令。✓

**3. Type consistency:** `serializeSearchToQuery`/`parseQueryToSearch` 在 Task 1 定义、Task 2 import 调用，签名一致；`setItemRef`/`focusFirstError` 在 Task 4 内定义并使用，一致；`visibleItems`/`isEditable`/`editMode`/`isSecretExemptFromRequired` 均为 EditModal 既有符号。✓

**注意点：**
- Task 2 的 `applyQueryToSearch` 在 `onMounted` 中无论返回 true/false 都 `fetchData()` — 这是有意的：有 query 时先回填再查，无 query 时直接查。逻辑等价于无条件 fetchData，但保留 `applyQueryToSearch` 调用以确保回填先于查询。
- Task 4 函数式 ref 的 `el` 类型用 `Element | { $el?: HTMLElement } | null` 兼容 Naive UI 组件实例（其 DOM 在 `$el`）。
