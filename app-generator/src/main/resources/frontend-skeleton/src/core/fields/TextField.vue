<script setup lang="ts">
import { NInput, NEllipsis } from 'naive-ui'
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
    <NEllipsis>{{ value ?? '' }}</NEllipsis>
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
