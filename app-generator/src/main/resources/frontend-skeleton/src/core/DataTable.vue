<script setup lang="ts">
import { computed, h } from 'vue'
import { NDataTable, NButton, NSpace, NPopconfirm } from 'naive-ui'
import type { DataTableColumn, PaginationProps } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'
import { isVisible } from './protocol/field-policy'

const props = defineProps<{
  items: ItemDef[]
  data: Record<string, any>[]
  loading: boolean
  pagination: PaginationProps
  /** The bizKey value of the row whose detail is currently loading, or null */
  editingRowKey?: unknown
  /** The bizKey field name (e.g. "studentProfileCode") */
  bizKey?: string
}>()

const emit = defineEmits<{
  edit: [row: Record<string, any>]
  delete: [row: Record<string, any>]
  'update:pagination': [pagination: PaginationProps]
}>()

const visibleItems = computed(() =>
  props.items.filter(item => isVisible(item, 'table'))
)

const columns = computed<DataTableColumn[]>(() => {
  const totalItems = visibleItems.value.length
  const shouldFreeze = totalItems > 4

  const cols: DataTableColumn[] = visibleItems.value.map((item, index) => {
    const isMultiline = item.type === 'text' && (item as any).isMultilineOrRich
    return {
    title: item.title,
    key: item.name,
    // Multiline fields: constrain tooltip width and allow word-wrap so a single
    // very long line doesn't stretch the tooltip bubble across the entire screen.
    ellipsis: isMultiline
      ? { tooltip: { contentStyle: 'max-width: 360px; max-height: 240px; overflow-y: auto; white-space: pre-wrap; word-break: break-all; overflow-wrap: break-word' } as const }
      : { tooltip: true },
    resizable: true,
    minWidth: 120,
    // Multiline/rich text can be very long — cap column width so it doesn't blow out
    ...(isMultiline ? { width: 200 } : {}),
    ...(shouldFreeze && index < 4 ? { fixed: 'left' as const } : {}),
    render(row: Record<string, any>) {
      return h(FieldRenderer, {
        item,
        mode: 'display',
        value: row[item.name] ?? null
      })
    }
  }})

  cols.push({
    title: '操作',
    key: '_actions',
    width: 120,
    fixed: shouldFreeze ? 'right' : undefined,
    render(row: Record<string, any>) {
      const isThisRowLoading = props.editingRowKey != null
        && props.bizKey != null
        && row[props.bizKey] === props.editingRowKey
      return h(NSpace, { wrap: false, size: 4 }, {
        default: () => [
          h(NButton, {
            size: 'small',
            quaternary: true,
            type: 'primary',
            loading: isThisRowLoading,
            disabled: props.editingRowKey != null && !isThisRowLoading,
            onClick: () => emit('edit', row)
          }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => emit('delete', row) }, {
            trigger: () => h(NButton, {
              size: 'small',
              quaternary: true,
              type: 'error',
              disabled: props.editingRowKey != null
            }, { default: () => '删除' }),
            default: () => '确定要删除吗？'
          })
        ]
      })
    }
  })

  return cols
})

const scrollX = computed(() => {
  if (visibleItems.value.length > 4) {
    return visibleItems.value.length * 150 + 120
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
