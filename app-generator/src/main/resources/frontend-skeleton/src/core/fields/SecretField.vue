<script setup lang="ts">
import { computed } from 'vue'
import { NInput, NButton, NSpace } from 'naive-ui'
import type { SecretItemDef } from '@/schema/types'

const props = defineProps<{
  item: SecretItemDef
  mode: 'search' | 'edit' | 'display'
  value: string | null
  /** 弹框场景：'edit-create'（创建）或 'edit-update'（编辑已有记录）。 */
  editMode?: 'edit-create' | 'edit-update'
}>()

const emit = defineEmits<{
  'update:value': [val: string | null]
}>()

// 编辑已有记录时的特殊态：详情接口不返回明文，允许"不修改即保留"。
const isEditUpdate = computed(() => props.mode === 'edit' && props.editMode === 'edit-update')

// 仅非必填 secret 显示清空按钮（必填 secret 清空必被后端拒绝）。
const showClearButton = computed(() => isEditUpdate.value && props.item.isNonVoid === false)

function onClear() {
  emit('update:value', '')
}
</script>

<template>
  <template v-if="mode === 'display'">
    <span>***</span>
  </template>
  <template v-else-if="mode === 'edit' && isEditUpdate">
    <NSpace align="center" :wrap="false" style="width: 100%">
      <NInput
        type="password"
        :value="value"
        show-password-on="click"
        placeholder="••••••"
        style="flex: 1"
        @update:value="emit('update:value', $event)"
      />
      <NButton v-if="showClearButton" quaternary size="small" @click="onClear">
        清空
      </NButton>
    </NSpace>
  </template>
  <template v-else-if="mode === 'edit'">
    <NInput
      type="password"
      :value="value"
      show-password-on="click"
      clearable
      placeholder="请输入"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
