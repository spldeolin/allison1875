<script setup lang="ts">
import { computed } from 'vue'
import { NDatePicker, NTimePicker } from 'naive-ui'
import type { TimeItemDef } from '@/schema/types'

const props = defineProps<{
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

function parseTime(str: string | null | undefined): [number, number, number] | null {
  if (!str) return null
  const parts = str.split(':')
  if (parts.length !== 3) return null
  return [parseInt(parts[0]), parseInt(parts[1]), parseInt(parts[2])]
}

const startParsed = computed(() => {
  if (!Array.isArray(props.value)) return null
  return parseTime(props.value[0])
})

const endParsed = computed(() => {
  if (!Array.isArray(props.value)) return null
  return parseTime(props.value[1])
})

function endHourDisabled(hour: number) {
  if (!startParsed.value) return false
  return hour < startParsed.value[0]
}

function endMinuteDisabled(minute: number, selectedHour: number | null) {
  if (!startParsed.value || selectedHour === null) return false
  if (selectedHour > startParsed.value[0]) return false
  if (selectedHour < startParsed.value[0]) return true
  return minute < startParsed.value[1]
}

function endSecondDisabled(second: number, selectedMinute: number | null, selectedHour: number | null) {
  if (!startParsed.value || selectedHour === null || selectedMinute === null) return false
  if (selectedHour > startParsed.value[0]) return false
  if (selectedHour < startParsed.value[0]) return true
  if (selectedMinute > startParsed.value[1]) return false
  if (selectedMinute < startParsed.value[1]) return true
  return second < startParsed.value[2]
}

function startHourDisabled(hour: number) {
  if (!endParsed.value) return false
  return hour > endParsed.value[0]
}

function startMinuteDisabled(minute: number, selectedHour: number | null) {
  if (!endParsed.value || selectedHour === null) return false
  if (selectedHour < endParsed.value[0]) return false
  if (selectedHour > endParsed.value[0]) return true
  return minute > endParsed.value[1]
}

function startSecondDisabled(second: number, selectedMinute: number | null, selectedHour: number | null) {
  if (!endParsed.value || selectedHour === null || selectedMinute === null) return false
  if (selectedHour < endParsed.value[0]) return false
  if (selectedHour > endParsed.value[0]) return true
  if (selectedMinute < endParsed.value[1]) return false
  if (selectedMinute > endParsed.value[1]) return true
  return second > endParsed.value[2]
}

function handleTimeRangeUpdate(idx: 0 | 1, val: string | null) {
  const current = Array.isArray(props.value) ? props.value : [null, null]
  const updated = [current[0], current[1]] as [string | null, string | null]
  updated[idx] = val
  if (!updated[0] && !updated[1]) {
    emit('update:value', null)
  } else {
    emit('update:value', [updated[0] ?? '', updated[1] ?? ''])
  }
}
</script>

<template>
  <template v-if="mode === 'display'">
    <span v-if="!value" style="color: #cbd5e1">-</span>
    <template v-else-if="item.format === 'dateTime' && String(value).includes(' ')">
      <div style="display: flex; flex-direction: column; gap: 2px; line-height: 1.4">
        <span style="font-size: 13px; color: #374151">{{ String(value).split(' ')[0] }}</span>
        <span style="font-size: 13px; color: #374151">{{ String(value).split(' ')[1] }}</span>
      </div>
    </template>
    <span v-else>{{ value }}</span>
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
    <template v-else-if="item.format === 'time'">
      <NTimePicker
        :formatted-value="Array.isArray(value) && value[0] ? value[0] : null"
        :value-format="FORMAT_MAP.time"
        :is-hour-disabled="startHourDisabled"
        :is-minute-disabled="startMinuteDisabled"
        :is-second-disabled="startSecondDisabled"
        clearable
        placeholder="开始时间"
        style="width: 130px"
        @update:formatted-value="handleTimeRangeUpdate(0, $event)"
      />
      <span style="margin: 0 4px; color: #94a3b8">~</span>
      <NTimePicker
        :formatted-value="Array.isArray(value) && value[1] ? value[1] : null"
        :value-format="FORMAT_MAP.time"
        :is-hour-disabled="endHourDisabled"
        :is-minute-disabled="endMinuteDisabled"
        :is-second-disabled="endSecondDisabled"
        clearable
        placeholder="结束时间"
        style="width: 130px"
        @update:formatted-value="handleTimeRangeUpdate(1, $event)"
      />
    </template>
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
