<script setup lang="ts">
import { ref, computed, watch, h, onBeforeUnmount } from 'vue'
import {
  NUpload,
  NUploadDragger,
  NIcon,
  NModal,
  NImage,
  NSpin,
  NButton,
  NText,
  useMessage,
  type UploadFileInfo,
  type UploadCustomRequestOptions,
} from 'naive-ui'
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
import type { Component } from 'vue'
import type { FileItemDef, FileValue } from '@/schema/types'
import {
  acceptOf,
  categoryKeyOf,
  filledHintOf,
  isExtensionAllowed,
  previewKindOf,
  type CategoryKey,
  type PreviewKind,
} from './file-category'
import { uploadFile, fetchDownloadToken, downloadUrlOf } from '@/core/protocol/file-api'

const props = defineProps<{
  item: FileItemDef
  mode: 'search' | 'edit' | 'display'
  value: FileValue | string | null
  /** Read-only field inside an edit modal (mode is 'display' but shown richly, not editable). */
  readonly?: boolean
}>()

const emit = defineEmits<{
  'update:value': [val: FileValue | null]
}>()

const message = useMessage()

const categoryKey = computed<CategoryKey>(() => categoryKeyOf(props.item.category))
const accept = computed(() => acceptOf(props.item.category))
const isImageCategory = computed(() => categoryKey.value === 'image')

// Per-category icon for the empty dragger and the (non-image) file-list rows.
const CATEGORY_ICONS: Record<CategoryKey, Component> = {
  image: ImageOutline,
  document: DocumentTextOutline,
  archive: ArchiveOutline,
  audio: MusicalNotesOutline,
  video: VideocamOutline,
  general: DocumentOutline,
}
const categoryIcon = computed(() => CATEGORY_ICONS[categoryKey.value])

// Normalize the incoming value (merged "fileKey/originFileName" string, or FileValue) to FileValue.
const fileValue = computed<FileValue | null>(() => {
  const v = props.value
  if (!v) return null
  if (typeof v === 'string') {
    const slash = v.indexOf('/')
    if (slash < 0) return null
    return { fileKey: v.substring(0, slash), originFileName: v.substring(slash + 1) }
  }
  return v
})

// 填充态图片类缩略图 URL（新上传走 blob，已落库走下载令牌）。
// :show-file-list=false 关闭了 naive-ui 原生缩略图渲染，图片类需自渲染。
// 序号守卫：快速连续变更时，丢弃过期异步结果，避免旧令牌 URL 覆盖新值。
const thumbUrl = ref('')
let thumbSeq = 0
watch(fileValue, async (fv) => {
  if (!fv || !isImageCategory.value) {
    thumbSeq++
    thumbUrl.value = ''
    return
  }
  const seq = ++thumbSeq
  const url = await createThumbnailUrl(null, { id: fv.fileKey } as UploadFileInfo)
  if (seq === thumbSeq) {
    thumbUrl.value = url
  }
}, { immediate: true })

// ----- naive-ui controlled file list (its native rendering is our source of display truth) -----
const fileList = ref<UploadFileInfo[]>([])

function toFileList(fv: FileValue | null): UploadFileInfo[] {
  if (!fv) return []
  return [{ id: fv.fileKey, name: fv.originFileName, status: 'finished' as const }]
}

watch(fileValue, (fv) => { fileList.value = toFileList(fv) }, { immediate: true })

// Object URLs created for fresh-upload thumbnails; revoked on unmount to avoid leaks.
const objectUrls: string[] = []
onBeforeUnmount(() => objectUrls.forEach((u) => URL.revokeObjectURL(u)))

// Resolve an image thumbnail: fresh uploads use the local blob; loaded records fetch a signed token.
async function createThumbnailUrl(file: File | null, fileInfo: UploadFileInfo): Promise<string> {
  if (file) {
    const url = URL.createObjectURL(file)
    objectUrls.push(url)
    return url
  }
  try {
    const token = await fetchDownloadToken(fileInfo.id)
    return downloadUrlOf(token)
  } catch {
    return ''
  }
}

// ----- upload (two-step): validate accept + size, then POST, then emit FileValue -----
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

function handleRemove(): boolean {
  emit('update:value', null)
  return true
}

// 填充态按钮位于 NUploadDragger 内部，点击必须阻止冒泡，否则触发文件选择对话框。
function stopProp(e: Event) {
  e.stopPropagation()
}

// ----- preview modal (naive-ui has no inline media player, so an adaptive modal is used) -----
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewToken = ref('')
const previewName = computed(() => fileValue.value?.originFileName ?? '')
const previewKind = computed<PreviewKind>(() => previewKindOf(previewName.value))
const previewUrl = computed(() => (previewToken.value ? downloadUrlOf(previewToken.value) : ''))

// Whether the current file can be previewed inline. Non-previewable rows expose no
// preview affordance and ignore preview clicks (download stays available).
const isPreviewable = computed(() => !!fileValue.value && previewKindOf(fileValue.value.originFileName) !== 'other')

// Modal sizing per kind: images hug their content (capped), documents/video get wide, media stays compact.
const previewModalStyle = computed(() => {
  switch (previewKind.value) {
    case 'image':
      return { width: 'fit-content', maxWidth: 'min(90vw, 640px)' }
    case 'audio':
      return { width: '460px', maxWidth: '92vw' }
    default:
      return { width: '820px', maxWidth: '92vw' }
  }
})

async function openPreview() {
  if (!fileValue.value || !isPreviewable.value) return
  previewVisible.value = true
  previewLoading.value = true
  previewToken.value = ''
  try {
    previewToken.value = await fetchDownloadToken(fileValue.value.fileKey)
  } catch {
    message.error('获取预览令牌失败')
    previewVisible.value = false
  } finally {
    previewLoading.value = false
  }
}

// naive-ui's own download would use file.url (null here); suppress it and download via a signed token.
async function handleDownload(): Promise<boolean> {
  if (!fileValue.value) return false
  try {
    const token = await fetchDownloadToken(fileValue.value.fileKey)
    const a = document.createElement('a')
    a.href = downloadUrlOf(token)
    a.download = fileValue.value.originFileName
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  } catch {
    message.error('下载失败')
  }
  return false
}

const listType = computed(() => (isImageCategory.value ? 'image' : 'text'))
const renderCategoryIcon = () => h(NIcon, null, { default: () => h(categoryIcon.value) })
</script>

<template>
  <!-- Compact display (table cell): icon + filename + preview eye, read-only -->
  <span v-if="mode === 'display' && !readonly" class="file-display">
    <template v-if="fileValue">
      <NIcon size="16" class="file-display__icon"><component :is="categoryIcon" /></NIcon>
      <span class="file-display__name" :title="fileValue.originFileName">{{ fileValue.originFileName }}</span>
      <NButton v-if="isPreviewable" text type="primary" @click="openPreview">
        <template #icon><NIcon><EyeOutline /></NIcon></template>
      </NButton>
    </template>
    <span v-else class="file-display__empty">-</span>
  </span>

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

  <!-- Adaptive preview modal: image / pdf / text / audio / video / download-only -->
  <NModal
    v-model:show="previewVisible"
    preset="card"
    :title="previewName"
    class="file-preview-modal"
    :style="previewModalStyle"
    :bordered="false"
  >
    <div v-if="previewLoading" class="file-preview__loading">
      <NSpin />
    </div>
    <div v-else-if="previewUrl" class="file-preview__body">
      <NImage
        v-if="previewKind === 'image'"
        :src="previewUrl"
        object-fit="contain"
        class="file-preview__image"
      />
      <iframe
        v-else-if="previewKind === 'pdf' || previewKind === 'text'"
        :src="previewUrl"
        class="file-preview__iframe"
      />
      <audio v-else-if="previewKind === 'audio'" :src="previewUrl" controls class="file-preview__audio" />
      <video v-else-if="previewKind === 'video'" :src="previewUrl" controls class="file-preview__video" />
      <div v-else class="file-preview__fallback">
        <NIcon :size="40" :depth="3"><DocumentOutline /></NIcon>
        <NText depth="3">该文件类型不支持在线预览</NText>
        <NButton tag="a" :href="previewUrl" target="_blank" type="primary">
          <template #icon><NIcon><DownloadOutline /></NIcon></template>
          下载查看
        </NButton>
      </div>
    </div>
  </NModal>
</template>

<style scoped>
/* Compact table-cell display */
.file-display {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}
.file-display__icon {
  color: var(--n-text-color-3, #909399);
  flex-shrink: 0;
}
.file-display__name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-display__empty {
  color: #cbd5e1;
}

/* Read-only inside modal */
.file-readonly {
  width: 100%;
}

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

/* Preview modal (width comes from the per-kind inline style binding) */
.file-preview__loading,
.file-preview__fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  min-height: 220px;
}
.file-preview__body {
  display: flex;
  justify-content: center;
}
.file-preview__image {
  max-width: 100%;
  max-height: 70vh;
}
.file-preview__iframe {
  width: 100%;
  height: 72vh;
  border: none;
}
.file-preview__audio {
  width: 100%;
  margin: 40px 0;
}
.file-preview__video {
  max-width: 100%;
  max-height: 72vh;
}
</style>
