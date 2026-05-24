<script setup lang="ts">
import { computed } from 'vue'
import { NForm, NFormItem, NButton, NSpace } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'

const props = defineProps<{
  items: ItemDef[]
  modelValue: Record<string, any>
}>()

const emit = defineEmits<{
  'update:modelValue': [val: Record<string, any>]
  search: []
  reset: []
}>()

const searchableItems = computed(() =>
  props.items.filter(item => {
    if (item.type === 'secret') return false
    if (item.type === 'time' && item.format === 'time') return false
    return true
  })
)

function updateField(name: string, value: any) {
  emit('update:modelValue', { ...props.modelValue, [name]: value })
}

function handleReset() {
  const empty: Record<string, any> = {}
  for (const item of searchableItems.value) {
    empty[item.name] = null
  }
  emit('update:modelValue', empty)
  emit('reset')
}
</script>

<template>
  <NForm inline label-placement="left" style="flex-wrap: wrap; gap: 0 16px;">
    <NFormItem v-for="item in searchableItems" :key="item.name" :label="item.title">
      <FieldRenderer
        :item="item"
        mode="search"
        :value="modelValue[item.name] ?? null"
        @update:value="updateField(item.name, $event)"
      />
    </NFormItem>
    <NFormItem>
      <NSpace>
        <NButton type="primary" @click="emit('search')">查询</NButton>
        <NButton @click="handleReset">重置</NButton>
      </NSpace>
    </NFormItem>
  </NForm>
</template>
