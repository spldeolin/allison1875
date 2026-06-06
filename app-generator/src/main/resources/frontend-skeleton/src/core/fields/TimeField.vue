<script setup lang="ts">
import { NDatePicker, NTimePicker } from 'naive-ui'
import type { TimeItemDef } from '@/schema/types'

defineProps<{
  item: TimeItemDef
  mode: 'search' | 'edit' | 'display'
  value: string | [string, string] | null
}>()

const emit = defineEmits<{
  'update:value': [val: string | [string, string] | null]
}>()

const FORMAT_MAP = {
  date: 'yyyy-MM-dd',
  time: 'HH:mm:ss',
  dateTime: 'yyyy-MM-dd HH:mm:ss',
} as const
</script>

<template>
  <template v-if="mode === 'display'">
    <span>{{ value ?? '' }}</span>
  </template>
  <template v-else-if="mode === 'search'">
    <NDatePicker
      v-if="item.format === 'date'"
      type="daterange"
      :formatted-value="value as [string, string] | null"
      :value-format="FORMAT_MAP.date"
      clearable
      @update:formatted-value="emit('update:value', $event)"
    />
    <NDatePicker
      v-else-if="item.format === 'dateTime'"
      type="datetimerange"
      :formatted-value="value as [string, string] | null"
      :value-format="FORMAT_MAP.dateTime"
      clearable
      @update:formatted-value="emit('update:value', $event)"
    />
    <template v-else-if="item.format === 'time'" />
  </template>
  <template v-else>
    <NDatePicker
      v-if="item.format === 'date'"
      type="date"
      :formatted-value="value as string | null"
      :value-format="FORMAT_MAP.date"
      clearable
      @update:formatted-value="emit('update:value', $event)"
    />
    <NDatePicker
      v-else-if="item.format === 'dateTime'"
      type="datetime"
      :formatted-value="value as string | null"
      :value-format="FORMAT_MAP.dateTime"
      clearable
      @update:formatted-value="emit('update:value', $event)"
    />
    <NTimePicker
      v-else
      :formatted-value="value as string | null"
      :value-format="FORMAT_MAP.time"
      clearable
      @update:formatted-value="emit('update:value', $event)"
    />
  </template>
</template>
