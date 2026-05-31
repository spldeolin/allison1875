<script setup lang="ts">
import { NInput } from 'naive-ui'
import type { TextItemDef } from '@/schema/types'

defineProps<{
  item: TextItemDef
  mode: 'search' | 'edit' | 'display'
  value: string | null
}>()

const emit = defineEmits<{
  'update:value': [val: string | null]
}>()
</script>

<template>
  <template v-if="mode === 'display'">
    <!-- Plain span: NDataTable's ellipsis column config owns truncation and tooltip -->
    <span>{{ value ?? '' }}</span>
  </template>
  <template v-else-if="mode === 'search'">
    <NInput :value="value" clearable placeholder="请输入" @update:value="emit('update:value', $event)" />
  </template>
  <template v-else>
    <NInput
      v-if="item.isMultilineOrRich"
      type="textarea"
      :value="value"
      :maxlength="item.maxLength"
      show-count
      placeholder="请输入"
      :autosize="{ minRows: 3, maxRows: 8 }"
      style="padding-bottom: 22px"
      @update:value="emit('update:value', $event)"
    />
    <NInput
      v-else
      :value="value"
      :maxlength="item.maxLength"
      clearable
      placeholder="请输入"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
