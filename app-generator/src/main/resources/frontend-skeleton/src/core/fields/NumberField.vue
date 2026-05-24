<script setup lang="ts">
import { NInputNumber } from 'naive-ui'
import type { NumberItemDef } from '@/schema/types'

defineProps<{
  item: NumberItemDef
  mode: 'search' | 'edit' | 'display'
  value: number | null
}>()

const emit = defineEmits<{
  'update:value': [val: number | null]
}>()

function handleKeydown(e: KeyboardEvent) {
  const allowed = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'Home', 'End', '.', '-']
  if (allowed.includes(e.key)) return
  if (e.ctrlKey || e.metaKey) return
  if (!/^\d$/.test(e.key)) {
    e.preventDefault()
  }
}
</script>

<template>
  <template v-if="mode === 'display'">
    <span>{{ value ?? '' }}</span>
  </template>
  <template v-else>
    <NInputNumber
      :value="value"
      clearable
      :show-button="false"
      :precision="item.canBeDecimal ? undefined : 0"
      placeholder="请输入"
      style="width: 100%;"
      @keydown="handleKeydown"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
