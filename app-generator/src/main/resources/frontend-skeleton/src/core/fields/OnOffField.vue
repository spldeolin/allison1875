<script setup lang="ts">
import { computed } from 'vue'
import { NSwitch, NSelect, NTag } from 'naive-ui'
import type { OnOffItemDef } from '@/schema/types'

const props = defineProps<{
  item: OnOffItemDef
  mode: 'search' | 'edit' | 'display'
  value: boolean | null
}>()

const emit = defineEmits<{
  'update:value': [val: boolean | null]
}>()

const searchOptions = [
  { label: '是', value: 'true' },
  { label: '否', value: 'false' }
]

const searchValue = computed(() => {
  if (props.value === null) return null
  return String(props.value)
})

function handleSearchUpdate(val: string | null) {
  if (val === null) emit('update:value', null)
  else emit('update:value', val === 'true')
}
</script>

<template>
  <template v-if="mode === 'display'">
    <NTag :type="value ? 'success' : 'default'" size="small">{{ value ? '是' : '否' }}</NTag>
  </template>
  <template v-else-if="mode === 'search'">
    <NSelect :value="searchValue" :options="searchOptions" clearable placeholder="请选择" style="min-width: 120px;" @update:value="handleSearchUpdate" />
  </template>
  <template v-else>
    <NSwitch :value="value ?? false" @update:value="emit('update:value', $event)" />
  </template>
</template>
