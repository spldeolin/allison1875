# FileField 紧凑一体化实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `FileField.vue` 编辑态改造为「状态自适应的单一拖拽区」——空态紧凑、填充态文件条目即拖拽/点击区、上传后可直接覆盖替换。

**Architecture:** 保留 `NUpload` + `NUploadDragger` 承载上传机制（accept、custom-request、拖拽、下载令牌管线复用），去掉 `:max="1"`、加 `:show-file-list="false"`，让 dragger 内容按 `fileValue` 空与非空自适应渲染。提取一个纯函数 `filledHintOf` 用于填充态/空态第二行提示文本，可单测。

**Tech Stack:** Vue 3 `<script setup>` + naive-ui（NUpload/NUploadDragger/NIcon/NImage/NButton/NText）+ @vicons/ionicons5 + TypeScript + vitest

**Spec:** `docs/superpowers/specs/2026-07-06-file-field-compact-unified-design.md`

**工作目录：** `app-generator/src/main/resources/frontend-skeleton/`（所有 `npm`/路径均相对此目录）

**测试现实：** 项目无 Vue 组件测试基建（无 `@vue/test-utils`、无 jsdom 组件挂载），现有测试均为纯逻辑（`tests/utils|protocol|schema`）。本计划不引入组件测试基建（超出 spec 范围），改为：提取纯函数走 TDD；组件层以 `npm run build`（含 `vue-tsc` 类型检查）+ spec 第 3–9 项手动行为验证为保证。

---

## 文件结构

| 文件 | 责任 | 操作 |
|---|---|---|
| `src/core/fields/file-category.ts` | 新增 `filledHintOf(category, maxFileSize)` 纯函数，返回空态/填充态第二行提示文本 | 修改（追加导出） |
| `tests/fields/file-category.test.ts` | `filledHintOf` 单元测试 | 新建 |
| `src/core/fields/FileField.vue` | 状态自适应 dragger 渲染、`show-file-list=false`、去掉 `max`、按钮 `stopPropagation`、`onFinish` 重置 `fileList`、图片类自渲染缩略图、只读态统一 | 修改 |

---

### Task 1: 提取 `filledHintOf` 纯函数（TDD）

**Files:**
- Create: `tests/fields/file-category.test.ts`
- Modify: `src/core/fields/file-category.ts`（追加导出）

- [ ] **Step 1: 写失败测试**

创建 `tests/fields/file-category.test.ts`：

```typescript
import { describe, it, expect } from 'vitest'
import { filledHintOf } from '@/core/fields/file-category'

describe('filledHintOf', () => {
  it('returns category title only when no size limit', () => {
    expect(filledHintOf('document', undefined)).toBe('文档')
  })

  it('appends size limit when provided', () => {
    expect(filledHintOf('document', 10)).toBe('文档 · ≤ 10MB')
  })

  it('defaults to general category', () => {
    expect(filledHintOf(undefined, 5)).toBe('普通文件 · ≤ 5MB')
  })

  it('falls back to general title for unknown category', () => {
    expect(filledHintOf('unknown', undefined)).toBe('普通文件')
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `npm run test -- --run tests/fields/file-category.test.ts`
Expected: FAIL，`filledHintOf is not a function` / 导出不存在。

- [ ] **Step 3: 实现纯函数**

在 `src/core/fields/file-category.ts` 末尾追加：

```typescript
/**
 * 紧凑拖拽区第二行提示文本：仅「类别标题」或「类别 · ≤ NMB」。
 * 用于空态与填充态共享的 hint 行（spec: 去掉支持格式明细，仅保留大类与大小上限）。
 */
export function filledHintOf(category: string | undefined, maxFileSize?: number): string {
  const title = categoryTitleOf(category)
  if (maxFileSize) {
    return `${title} · ≤ ${maxFileSize}MB`
  }
  return title
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `npm run test -- --run tests/fields/file-category.test.ts`
Expected: PASS（4 个用例全过）。

- [ ] **Step 5: 提交**

```bash
git add src/core/fields/file-category.ts tests/fields/file-category.test.ts
git commit -m "feat: add filledHintOf helper for compact file dragger hint"
```

---

### Task 2: FileField 状态自适应渲染改造

**Files:**
- Modify: `src/core/fields/FileField.vue`（`<script setup>` + `<template>` + `<style scoped>` 三段）

本任务改动较大，分步小提交。**每步后都跑 `npm run build` 确认类型与编译通过。**

- [ ] **Step 1: script 段——引入 `filledHintOf`、删除 `acceptSummaryOf` 引用、新增填充态缩略图 URL ref、`handleUpload` 的 `onFinish` 内重置 `fileList`、新增按钮 `stopPropagation` handler**

把 `<script setup>` 中的 import 块（第 29–38 行）改为：

```typescript
import {
  acceptOf,
  categoryKeyOf,
  categoryTitleOf,
  filledHintOf,
  isExtensionAllowed,
  previewKindOf,
  isPreviewableImage,
  type CategoryKey,
  type PreviewKind,
} from './file-category'
```

（去掉 `acceptSummaryOf`，新增 `filledHintOf`、`isPreviewableImage`。）

在 `const listType = ...` 那一行之前，新增填充态图片缩略图 URL：

```typescript
// 填充态图片类缩略图 URL（新上传走 blob，已落库走下载令牌）。
// :show-file-list=false 关闭了 naive-ui 原生缩略图渲染，图片类需自渲染。
const thumbUrl = ref('')
watch(fileValue, async (fv) => {
  if (!fv || !isImageCategory.value) {
    thumbUrl.value = ''
    return
  }
  thumbUrl.value = await createThumbnailUrl(null, { id: fv.fileKey } as UploadFileInfo)
}, { immediate: true })
```

把 `handleUpload` 函数（第 112–137 行）中 `onFinish()` 调用替换为显式重置 `fileList`：

```typescript
async function handleUpload({ file, onFinish, onError }: UploadCustomRequestOptions) {
  const raw = file.file
  if (!raw) {
    onError()
    return
  }
  if (!isExtensionAllowed(props.item.category, raw.name)) {
    message.error('文件扩展名不被允许')
    onError()
    return
  }
  if (props.item.maxFileSize && raw.size > props.item.maxFileSize * 1024 * 1024) {
    message.error(`文件大小超过 ${props.item.maxFileSize}MB`)
    onError()
    return
  }
  try {
    const result = await uploadFile(raw, props.item.category || 'general')
    emit('update:value', result)
    // :show-file-list=false 下 naive-ui 仍会追加临时 uploading 条目；
    // 显式重置为最终值，避免 fileList 残留脏数据。
    fileList.value = toFileList(result)
    onFinish()
    message.success('上传成功')
  } catch {
    message.error('上传失败')
    onError()
  }
}
```

新增按钮事件隔离 helper（放在 `handleRemove` 之后）：

```typescript
// 填充态按钮位于 NUploadDragger 内部，点击必须阻止冒泡，否则触发文件选择对话框。
function stopProp(e: Event) {
  e.stopPropagation()
}
```

- [ ] **Step 2: template 段——可编辑态：去 `max`、加 `show-file-list=false`、dragger 内容状态自适应**

把可编辑态 `<NUpload>` 块（第 236–267 行）整段替换为：

```html
  <!-- Editable: single state-adaptive dragger — empty prompt OR filled file row, both are the drop/click target -->
  <NUpload
    v-else
    v-model:file-list="fileList"
    :accept="accept"
    :show-file-list="false"
    :list-type="listType"
    :render-icon="renderCategoryIcon"
    :create-thumbnail-url="createThumbnailUrl"
    :custom-request="handleUpload"
    :show-download-button="true"
    :show-preview-button="isPreviewable"
    @preview="openPreview"
    @download="handleDownload"
    @remove="handleRemove"
  >
    <NUploadDragger class="file-dragger" :class="{ 'file-dragger--filled': fileValue }">
      <!-- 空态：紧凑上传提示 -->
      <template v-if="!fileValue">
        <div class="file-dragger__empty">
          <NIcon :size="22" :depth="3" class="file-dragger__icon"><CloudUploadOutline /></NIcon>
          <span class="file-dragger__title">点击或拖拽上传</span>
          <span class="file-dragger__hint">{{ filledHintOf(props.item.category, props.item.maxFileSize) }}</span>
        </div>
      </template>
      <!-- 填充态：文件条目即拖拽区 -->
      <template v-else>
        <div class="file-row">
          <NImage
            v-if="isImageCategory && thumbUrl"
            :src="thumbUrl"
            object-fit="cover"
            class="file-row__thumb"
            :preview-disabled="true"
            @click="stopProp"
          />
          <NIcon v-else size="20" class="file-row__icon"><component :is="categoryIcon" /></NIcon>
          <span class="file-row__name" :title="fileValue.originFileName">{{ fileValue.originFileName }}</span>
          <span class="file-row__hint">{{ filledHintOf(props.item.category, props.item.maxFileSize) }}</span>
          <div class="file-row__actions" @click="stopProp">
            <NButton v-if="isPreviewable" text type="primary" @click="openPreview">
              <template #icon><NIcon><EyeOutline /></NIcon></template>
            </NButton>
            <NButton text type="primary" @click="handleDownload">
              <template #icon><NIcon><DownloadOutline /></NIcon></template>
            </NButton>
            <NButton text @click="handleRemove">
              <template #icon><NIcon><CloseOutline /></NIcon></template>
            </NButton>
          </div>
        </div>
      </template>
    </NUploadDragger>
  </NUpload>
```

在 import 块（第 16–26 行的 `@vicons/ionicons5` 导入）中加入 `CloseOutline`：

```typescript
import {
  ImageOutline,
  DocumentTextOutline,
  ArchiveOutline,
  MusicalNotesOutline,
  VideocamOutline,
  DocumentOutline,
  CloudUploadOutline,
  DownloadOutline,
  EyeOutline,
  CloseOutline,
} from '@vicons/ionicons5'
```

- [ ] **Step 3: template 段——只读态：同样 `show-file-list=false` + 自渲染行**

把只读态 `<div v-else-if="readonly">` 块（第 218–233 行）整段替换为：

```html
  <!-- Read-only inside an edit modal: self-rendered row (no clear button), preview/download kept -->
  <div v-else-if="readonly" class="file-readonly">
    <div v-if="fileValue" class="file-row file-row--readonly">
      <NImage
        v-if="isImageCategory && thumbUrl"
        :src="thumbUrl"
        object-fit="cover"
        class="file-row__thumb"
        :preview-disabled="true"
      />
      <NIcon v-else size="20" class="file-row__icon"><component :is="categoryIcon" /></NIcon>
      <span class="file-row__name" :title="fileValue.originFileName">{{ fileValue.originFileName }}</span>
      <span class="file-row__hint">{{ filledHintOf(props.item.category, props.item.maxFileSize) }}</span>
      <div class="file-row__actions">
        <NButton v-if="isPreviewable" text type="primary" @click="openPreview">
          <template #icon><NIcon><EyeOutline /></NIcon></template>
        </NButton>
        <NButton text type="primary" @click="handleDownload">
          <template #icon><NIcon><DownloadOutline /></NIcon></template>
        </NButton>
      </div>
    </div>
    <NText v-else depth="3">未上传文件</NText>
  </div>
```

注意：只读态无 `NUpload` 包裹，`thumbUrl` watch 依赖 `fileValue`，只读态同样有 `fileValue`，缩略图正常解析。但只读态**没有** `NUpload` 提供 `create-thumbnail-url` 上下文——`createThumbnailUrl(null, {id: fileKey})` 在 `fetchDownloadToken` 分支不依赖 NUpload，可独立工作。`isImageCategory`、`thumbUrl` 均为组件级 computed/ref，只读态可访问。

- [ ] **Step 4: style 段——替换空态 dragger 样式、新增填充态行样式**

把 `<style scoped>` 中 `.file-dragger ...` 一组规则（第 334–368 行）整段替换，并追加 `.file-row` 规则：

```css
/* 状态自适应 dragger —— 空态与填充态等高 */
.file-dragger {
  padding: 6px 12px;
}
.file-dragger--filled {
  padding: 4px 10px;
}
.file-dragger__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 44px;
}
.file-dragger__icon {
  flex-shrink: 0;
  line-height: 1;
}
.file-dragger__title {
  font-size: 13px;
  color: var(--n-text-color-2, #333639);
  white-space: nowrap;
}
.file-dragger__hint {
  font-size: 12px;
  color: var(--n-text-color-3, #909399);
  white-space: nowrap;
}

/* 填充态文件行 —— 即拖拽区 */
.file-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 44px;
  width: 100%;
}
.file-row--readonly {
  cursor: default;
}
.file-row__thumb {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  flex-shrink: 0;
  object-fit: cover;
}
.file-row__icon {
  color: var(--n-text-color-3, #909399);
  flex-shrink: 0;
}
.file-row__name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: var(--n-text-color-2, #333639);
  flex: 1 1 auto;
  min-width: 0;
}
.file-row__hint {
  font-size: 12px;
  color: var(--n-text-color-3, #909399);
  white-space: nowrap;
  flex-shrink: 0;
}
.file-row__actions {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}
```

保留 `.file-readonly { width: 100%; }`、`.file-display*`、`.file-preview*` 等其余样式不动。

- [ ] **Step 5: 构建验证**

Run: `npm run build`
Expected: 成功，`vue-tsc` 无类型错误，`vite build` 产出 `dist/`。

- [ ] **Step 6: 全量测试回归**

Run: `npm run test -- --run`
Expected: 全部通过，**允许** `tests/utils/naming.test.ts` 的 1 个预存失败（见 memory `frontend-skeleton-preexisting-naming-test-failure`）。新增的 `file-category.test.ts` 4 例通过。

- [ ] **Step 7: 提交**

```bash
git add src/core/fields/FileField.vue
git commit -m "$(cat <<'EOF'
feat: unify file upload dragger and file row in edit modal

1. Shorten empty dragger to match a single file row height
2. Render uploaded file row as the drop/click target itself
3. Allow overwriting an uploaded file by drag/click without clearing first
4. Self-render image thumbnails and unify read-only state visuals

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
EOF
)"
```

---

### Task 3: 手动行为验证（spec 第 3–9 项）

无代码改动，按 spec「验证清单」逐项人工核验。若任一项不通过，回到 Task 2 对应步骤修复。

- [ ] **Step 1: 启动开发服务器**

Run: `npm run dev`（端口 5180，proxy 到 8080 后端）

- [ ] **Step 2: 逐项核验**

逐条确认：
1. 空态高度 ~44–56px；仅显示「类别 · ≤ NMB」；点击/拖拽触发上传。
2. 上传 A 后，直接点击或拖拽 B，条目变为 B（无需先 ✕）。
3. 点 👁/⬇/✕ 不触发文件选择框；👁 打开预览 modal，⬇ 下载，✕ 清空回空态。
4. 图片类填充态显示缩略图（`NImage`）而非类别图标。
5. 编辑弹框中只读 file 字段：无 ✕，有 👁/⬇，视觉与可编辑态一致。
6. ✕ 清除后回到空态，可再次上传。
7. 上传完成后无 `uploading` 残留条目（`fileList` 仅一条 finished）。

- [ ] **Step 3: 记录结果**

如全部通过，无需提交（无代码变更）。如有修复，回到 Task 2 修复后重新构建并提交。

---

## Self-Review 结论

**Spec 覆盖：** spec 各节均有任务对应——
- 高度缩短/去支持格式 → Task 1（`filledHintOf`）+ Task 2 Step 2/4（空态样式）
- 条目即拖拽区 → Task 2 Step 2（填充态 dragger 内容）
- 覆盖替换 → Task 2 Step 1（去 `max`、`onFinish` 重置）+ Step 2（dragger 始终可点）
- 按钮 stopPropagation → Task 2 Step 1（`stopProp`）+ Step 2（绑定）
- 图片缩略图 → Task 2 Step 1（`thumbUrl`）+ Step 2（`NImage`）
- 只读态统一 → Task 2 Step 3
- 验证清单 → Task 3

**占位符扫描：** 无 TBD/TODO；每步均含完整代码或确切命令。

**类型一致性：** `filledHintOf(category: string | undefined, maxFileSize?: number)` 在 Task 1 定义，Task 2 Step 2 调用签名一致；`thumbUrl: Ref<string>`、`stopProp(e: Event)` 定义与使用一致；`CloseOutline` 在 import 与模板中一致。

**测试基建说明：** 已在计划头部坦陈无组件测试基建，纯函数走 TDD，组件层以 build + 手动验证为保证——非占位符，而是与项目现状一致的诚实取舍。
