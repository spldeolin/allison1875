<script setup lang="ts">
import { computed } from 'vue'
import { NSelect, NTag } from 'naive-ui'
import type { SelectItemDef } from '@/schema/types'

const props = defineProps<{
  item: SelectItemDef
  mode: 'search' | 'edit' | 'display'
  value: string | null
}>()

const emit = defineEmits<{
  'update:value': [val: string | null]
}>()

const selectOptions = computed(() =>
  props.item.options.map(opt => ({ label: opt.title, value: opt.code }))
)

const displayTitle = computed(() => {
  const opt = props.item.options.find(o => o.code === props.value)
  return opt?.title ?? ''
})
</script>

<template>
  <template v-if="mode === 'display'">
    <NTag v-if="value" size="small">{{ displayTitle }}</NTag>
    <span v-else></span>
  </template>
  <template v-else>
    <NSelect
      :value="value"
      :options="selectOptions"
      clearable
      filterable
      placeholder="请选择"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
