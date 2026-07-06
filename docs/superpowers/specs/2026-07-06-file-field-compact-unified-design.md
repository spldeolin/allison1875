# FileField 紧凑一体化设计

**日期**：2026-07-06
**范围**：`app-generator/src/main/resources/frontend-skeleton/src/core/fields/FileField.vue`
**关联**：`docs/superpowers/specs/2026-07-01-file-component-design.md`（文件组件整体设计）

## 背景与动机

当前 `FileField.vue` 在编辑态（create/update 弹框）使用 naive-ui 的 `NUpload` + `NUploadDragger`：

- 空态拖拽区较高（图标 34px + 标题 + 三段 hint：类别 / 支持格式 / 大小上限），在弹框中占位偏大。
- 上传一个文件后，naive-ui 原生文件列表在拖拽区**下方**单独渲染一条文件条目，与拖拽区视觉割裂。
- `:max="1"` 使 `maxReachedRef` 在已有文件时为 true，naive-ui **禁用**后续 input 点击与拖拽放入——用户无法直接覆盖替换，必须先点 ✕ 清除再上传。

用户期望：

1. 弹框中的上传组件高度缩短，与单条已上传文件条目高度一致；作为妥协，仅保留**上传大类**与**大小限制**的显示（去掉「支持格式」明细）。
2. 文件条目在样式上成为「上传组件的一部分」——条目本身即拖拽/点击区。
3. 上传一个文件后，仍可拖拽或点击该组件覆盖已上传的文件条目（替换，而非追加）。

## 设计决策

### 总体方案：状态自适应的单一拖拽区（Approach A）

保留 `NUpload` + `NUploadDragger` 承载上传机制（accept 过滤、`custom-request`、拖拽事件、对象 URL / 下载令牌管线全部复用），但：

- **去掉 `:max="1"`**，改用 `:show-file-list="false"` 隐藏 naive-ui 原生文件列表渲染。
- **`NUploadDragger` 的内容改为状态自适应**：
  - **空态**：渲染紧凑上传提示（图标 + 标题 + 一行 hint：类别 · 大小上限）。
  - **填充态**：渲染文件条目（类别图标 + 文件名 + 预览/下载/清除按钮），整个条目本身即拖拽/点击区。
- 文件条目的值仍由 `fileValue`（props.value 派生）驱动；`fileList` ref 仅作 naive-ui 内部账本，`:show-file-list="false"` 后不再可见。

### 行为细节

**覆盖替换语义**：
- 去掉 `:max="1"` 后，`maxReachedRef` 恒为 false，input 点击与拖拽放入始终可用。
- `handleUpload` 已 `emit('update:value', result)` 单个 `FileValue`；`watch(fileValue)` 把 `fileList` 重置为新值对应单元素数组。新上传自然替换旧条目，无需额外清除逻辑。
- naive-ui 在 `:show-file-list="false"` 下仍会向 `fileList` 追加临时 `uploading` 条目。由于 `:show-file-list="false"`，这些临时条目不可见，但为避免 `fileList` 残留脏数据，在 `handleUpload` 的 `onFinish` 内**显式重置** `fileList.value = toFileList(result)`（`result` 为刚上传成功的 `FileValue`），不依赖 props 回流时序。`watch(fileValue)` 仍保留以处理外部值变化（如弹框重开、清除）。

**填充态按钮的事件隔离**：
- 预览（👁）、下载（⬇）、清除（✕）三个按钮置于 `NUploadDragger` 内部。点击它们必须 `event.stopPropagation()`，否则会冒泡到 dragger 触发文件选择对话框。
- 清除按钮调 `handleRemove()`（`emit('update:value', null)`），不触发文件选择。

**图片类缩略图**：
- 当前 `list-type="image"` 依赖 naive-ui 原生渲染缩略图；改 `:show-file-list="false"` 后失去该渲染。
- 在填充态，图片类（`categoryKey === 'image'`）自行渲染缩略图：用已有的 `createThumbnailUrl(file, fileInfo)` 解析 URL（新上传走 blob，已落库记录走下载令牌），以 `<NImage>` 或 `<img>` 显示在文件名左侧，替代类别图标。
- 非图片类仍显示类别图标。

**空态紧凑化**：
- 图标尺寸由 34px 降至 ~22px；padding 由 `8px 12px` 收紧到 `6px 12px`，使空态高度接近单条文件行高度（目标 ~48–56px）。
- hint 仅保留：`{类别标题}` · `单文件 ≤ {maxFileSize}MB`（无 maxFileSize 则仅显示类别）。去掉 `acceptSummaryOf` 行与「点击或拖拽文件到此处上传」长标题，改为更短的「点击或拖拽上传」。

**填充态布局**：
```
┌──────────────────────────────────────────────────┐
│ [icon/thumb]  originFileName        👁  ⬇  ✕    │
│              {类别标题} · ≤ {maxFileSize}MB        │
└──────────────────────────────────────────────────┘
```
- 整条为 dragger，hover 时 naive-ui 默认 dragger hover 态给出可交互暗示。
- 文件名 `text-overflow: ellipsis`，`title` 悬停显示全名。
- 第二行 hint 与空态一致（类别 · 大小上限），无 maxFileSize 时仅类别。

**只读态（`readonly`）/ 表格显示态（`mode === 'display'`）**：
- 现有 `readonly` 分支用 disabled `NUpload` + 原生文件列表渲染。为保持视觉一致，`readonly` 分支也改为 `:show-file-list="false"` + 自渲染文件行（无 ✕，保留预览/下载）。这样编辑弹框中的只读 file 字段与可编辑态视觉统一。
- `mode === 'display'`（表格单元格）维持现状（紧凑 inline），不受本次改动影响。

### 不在范围内

- `mode === 'display'` 表格单元格样式。
- 预览 modal（`NModal`）内部行为。
- `file-api.ts`、`file-category.ts` 等协议/工具层。
- 后端、form-generator itemType。

## 受影响文件

仅 `FileField.vue`（`<script setup>` / `<template>` / `<style scoped>` 三段均改动）。无新增文件，无协议层变更。

## 验证清单

1. **构建**：`cd app-generator/src/main/resources/frontend-skeleton && npm run build` 通过（含 `vue-tsc`）。
2. **既有测试**：`npm run test`（vitest）保持既有通过数（允许 `naming.test.ts` 的预存失败，见 memory）。
3. **空态**：高度 ~48–56px；仅显示类别 + 大小上限；点击/拖拽触发上传。
4. **填充态替换**：上传 A 后，直接点击或拖拽 B，条目变为 B（无需先清除）。
5. **填充态按钮隔离**：点 👁/⬇/✕ 不触发文件选择对话框；👁 打开预览 modal，⬇ 下载，✕ 清空。
6. **图片类缩略图**：图片类填充态显示缩略图而非类别图标。
7. **只读态**：编辑弹框中只读 file 字段与可编辑态视觉一致（无 ✕，有预览/下载）。
8. **清除后回空态**：点 ✕ 后回到空态，可再次上传。
9. **`fileList` 不残留**：上传完成后 `fileList` 仅含一条 `finished` 条目（`handleUpload` 的 `onFinish` 内显式重置）。
