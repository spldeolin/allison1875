<script setup lang="ts">
import { NDatePicker, NTimePicker } from 'naive-ui'
import type { TimeItemDef } from '@/schema/types'

const props = defineProps<{
  item: TimeItemDef
  mode: 'search' | 'edit' | 'display'
  value: number | [number, number] | null
}>()

const emit = defineEmits<{
  'update:value': [val: number | [number, number] | null]
}>()

function formatDisplay(val: number | [number, number] | string | null): string {
  if (val === null || val === undefined) return ''
  // Backend may return ISO strings ("yyyy-MM-dd", "HH:mm:ss", "yyyy-MM-dd HH:mm:ss") — display as-is
  if (typeof val === 'string') return val
  const ts = typeof val === 'number' ? val : val[0]
  const d = new Date(ts)
  if (props.item.format === 'date') return d.toLocaleDateString('zh-CN')
  if (props.item.format === 'time') return d.toLocaleTimeString('zh-CN')
  return d.toLocaleString('zh-CN')
}
</script>

<template>
  <template v-if="mode === 'display'">
    <span>{{ formatDisplay(value) }}</span>
  </template>
  <template v-else-if="mode === 'search'">
    <NDatePicker
      v-if="item.format === 'date'"
      type="daterange"
      :value="value as [number, number] | null"
      clearable
      @update:value="emit('update:value', $event)"
    />
    <NDatePicker
      v-else-if="item.format === 'dateTime'"
      type="datetimerange"
      :value="value as [number, number] | null"
      clearable
      @update:value="emit('update:value', $event)"
    />
    <!-- time-format range search not yet implemented; field hidden in search -->
    <template v-else-if="item.format === 'time'" />
  </template>
  <template v-else>
    <NDatePicker
      v-if="item.format === 'date'"
      type="date"
      :value="value as number | null"
      clearable
      @update:value="emit('update:value', $event)"
    />
    <NDatePicker
      v-else-if="item.format === 'dateTime'"
      type="datetime"
      :value="value as number | null"
      clearable
      @update:value="emit('update:value', $event)"
    />
    <NTimePicker
      v-else
      :value="value as number | null"
      clearable
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
