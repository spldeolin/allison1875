<script setup lang="ts">
import { computed, h } from 'vue'
import { NDataTable, NButton, NSpace, NPopconfirm } from 'naive-ui'
import type { DataTableColumn, PaginationProps } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'

const props = defineProps<{
  items: ItemDef[]
  data: Record<string, any>[]
  loading: boolean
  pagination: PaginationProps
}>()

const emit = defineEmits<{
  edit: [row: Record<string, any>]
  delete: [row: Record<string, any>]
  'update:pagination': [pagination: PaginationProps]
}>()

const columns = computed<DataTableColumn[]>(() => {
  const totalItems = props.items.length
  const shouldFreeze = totalItems > 4

  const cols: DataTableColumn[] = props.items.map((item, index) => ({
    title: item.title,
    key: item.name,
    ellipsis: { tooltip: true },
    resizable: true,
    minWidth: 120,
    ...(shouldFreeze && index < 4 ? { fixed: 'left' as const } : {}),
    render(row: Record<string, any>) {
      return h(FieldRenderer, {
        item,
        mode: 'display',
        value: row[item.name] ?? null
      })
    }
  }))

  cols.push({
    title: '操作',
    key: '_actions',
    width: 150,
    fixed: shouldFreeze ? 'right' : undefined,
    render(row: Record<string, any>) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => emit('edit', row) }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => emit('delete', row) }, {
            trigger: () => h(NButton, { size: 'small', quaternary: true, type: 'error' }, { default: () => '删除' }),
            default: () => '确定要删除吗？'
          })
        ]
      })
    }
  })

  return cols
})

const scrollX = computed(() => {
  if (props.items.length > 4) {
    return props.items.length * 150 + 150
  }
  return undefined
})

function handlePageChange(page: number) {
  emit('update:pagination', { ...props.pagination, page })
}

function handlePageSizeChange(pageSize: number) {
  emit('update:pagination', { ...props.pagination, pageSize, page: 1 })
}
</script>

<template>
  <NDataTable
    :columns="columns"
    :data="data"
    :loading="loading"
    :pagination="pagination"
    :scroll-x="scrollX"
    flex-height
    style="flex: 1; min-height: 0;"
    remote
    @update:page="handlePageChange"
    @update:page-size="handlePageSizeChange"
  />
</template>

<style scoped>
:deep(.n-data-table-th) {
  white-space: nowrap !important;
}
</style>
