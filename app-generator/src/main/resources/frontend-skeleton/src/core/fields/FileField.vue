<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  NUploadDragger,
  NButton,
  NIcon,
  NModal,
  NImage,
  NSpin,
  useMessage,
  type UploadCustomRequestOptions,
} from 'naive-ui'
import { DocumentTextOutline, EyeOutline, TrashOutline, CloudUploadOutline } from '@vicons/ionicons5'
import type { FileItemDef, FileValue } from '@/schema/types'
import { acceptOf, hintOf, isExtensionAllowed, isPreviewableImage, isPreviewablePdf } from './file-category'
import { uploadFile, fetchDownloadToken, downloadUrlOf } from '@/core/protocol/file-api'

const props = defineProps<{
  item: FileItemDef
  mode: 'search' | 'edit' | 'display'
  value: FileValue | string | null
}>()

const emit = defineEmits<{
  'update:value': [val: FileValue | null]
}>()

const message = useMessage()

const accept = computed(() => acceptOf(props.item.category))
const hint = computed(() => hintOf(props.item.category, props.item.maxFileSize))

// Normalize incoming value (string from merged column, or FileValue object) to FileValue
const fileValue = computed<FileValue | null>(() => {
  const v = props.value
  if (!v) return null
  if (typeof v === 'string') {
    const bar = v.indexOf('/')
    if (bar < 0) return null
    return { fileKey: v.substring(0, bar), originFileName: v.substring(bar + 1) }
  }
  return v
})

const previewVisible = ref(false)
const previewLoading = ref(false)
const previewToken = ref('')
const isImage = computed(() => isPreviewableImage(fileValue.value?.originFileName))
const isPdf = computed(() => isPreviewablePdf(fileValue.value?.originFileName))
const downloadUrl = computed(() => (previewToken.value ? downloadUrlOf(previewToken.value) : ''))

async function handleUpload({ file }: UploadCustomRequestOptions) {
  const raw = file.file
  if (!raw) return
  if (!isExtensionAllowed(props.item.category, raw.name)) {
    message.error('文件扩展名不被允许')
    return
  }
  if (props.item.maxFileSize && raw.size > props.item.maxFileSize * 1024 * 1024) {
    message.error(`文件大小超过 ${props.item.maxFileSize}MB`)
    return
  }
  try {
    const result = await uploadFile(raw, props.item.category || 'general')
    emit('update:value', result)
    message.success('上传成功')
  } catch {
    message.error('上传失败')
  }
}

function removeFile() {
  emit('update:value', null)
}

async function openPreview() {
  if (!fileValue.value) return
  previewVisible.value = true
  previewLoading.value = true
  try {
    previewToken.value = await fetchDownloadToken(fileValue.value.fileKey)
  } catch {
    message.error('获取下载令牌失败')
    previewVisible.value = false
  } finally {
    previewLoading.value = false
  }
}
</script>

<template>
  <!-- display mode (table / detail / read-only edit) -->
  <span v-if="mode === 'display'" class="file-field-display">
    <template v-if="fileValue">
      <NIcon size="16" class="file-icon"><DocumentTextOutline /></NIcon>
      <span class="file-name" :title="fileValue.originFileName">{{ fileValue.originFileName }}</span>
      <NButton text @click="openPreview">
        <template #icon><NIcon><EyeOutline /></NIcon></template>
      </NButton>
    </template>
    <span v-else>-</span>
  </span>

  <!-- edit mode -->
  <div v-else class="file-field-edit">
    <!-- not uploaded yet -->
    <NUploadDragger
      v-if="!fileValue"
      :accept="accept"
      :show-file-list="false"
      :custom-request="handleUpload"
    >
      <div class="upload-area">
        <NIcon size="28" class="upload-icon"><CloudUploadOutline /></NIcon>
        <div class="upload-title">点击或拖拽文件到此上传</div>
        <div class="upload-hint">{{ hint }}</div>
      </div>
    </NUploadDragger>

    <!-- uploaded -->
    <div v-else class="file-card">
      <NIcon size="20" class="file-icon"><DocumentTextOutline /></NIcon>
      <span class="file-name" :title="fileValue.originFileName">{{ fileValue.originFileName }}</span>
      <NButton text @click="openPreview">
        <template #icon><NIcon><EyeOutline /></NIcon></template>
      </NButton>
      <NButton text @click="removeFile">
        <template #icon><NIcon><TrashOutline /></NIcon></template>
      </NButton>
    </div>
  </div>

  <!-- preview modal -->
  <NModal v-model:show="previewVisible" preset="card" :title="fileValue?.originFileName" style="width: 720px">
    <div v-if="previewLoading" class="preview-loading">
      <NSpin />
    </div>
    <div v-else-if="previewToken" class="preview-content">
      <NImage v-if="isImage" :src="downloadUrl" object-fit="contain" style="max-width: 100%" />
      <iframe v-else-if="isPdf" :src="downloadUrl" class="preview-iframe" />
      <div v-else class="preview-fallback">
        <NButton tag="a" :href="downloadUrl" target="_blank">下载</NButton>
      </div>
    </div>
  </NModal>
</template>

<style scoped>
.file-field-display {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.file-field-edit .file-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border: 1px solid var(--n-border-color, #e0e0e6);
  border-radius: 4px;
}
.file-icon {
  color: var(--n-icon-color, #909399);
  flex-shrink: 0;
}
.file-name {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.upload-area {
  padding: 16px;
  text-align: center;
}
.upload-icon {
  color: #909399;
}
.upload-title {
  margin-top: 8px;
  font-size: 14px;
  color: #303133;
}
.upload-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
.preview-loading,
.preview-fallback {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}
.preview-iframe {
  width: 100%;
  height: 70vh;
  border: none;
}
</style>
