<script setup lang="ts">
import { computed } from 'vue'
import { NSelect, NTag, NSpace } from 'naive-ui'
import type { MultiSelectItemDef } from '@/schema/types'

const props = defineProps<{
  item: MultiSelectItemDef
  mode: 'search' | 'edit' | 'display'
  value: string[] | null
}>()

const emit = defineEmits<{
  'update:value': [val: string[] | null]
}>()

const selectOptions = computed(() =>
  props.item.options.map(opt => ({ label: opt.title, value: opt.code }))
)

const displayTitles = computed(() => {
  if (!props.value) return []
  return props.value.map(code => {
    const opt = props.item.options.find(o => o.code === code)
    return opt?.title ?? code
  })
})
</script>

<template>
  <template v-if="mode === 'display'">
    <NSpace>
      <NTag v-for="t in displayTitles" :key="t" size="small">{{ t }}</NTag>
    </NSpace>
  </template>
  <template v-else>
    <NSelect
      :value="value"
      :options="selectOptions"
      multiple
      clearable
      filterable
      placeholder="请选择"
      style="min-width: 200px;"
      :consistent-menu-width="false"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
