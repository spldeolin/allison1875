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
  /** Currently checked row keys for batch delete */
  checkedRowKeys?: (string | number)[]
}>()

const emit = defineEmits<{
  edit: [row: Record<string, any>]
  delete: [row: Record<string, any>]
  'update:pagination': [pagination: PaginationProps]
  'update:checkedRowKeys': [keys: (string | number)[]]
}>()

const visibleItems = computed(() =>
  props.items.filter(item => isVisible(item, 'table'))
)

/**
 * Render a datetime string ("yyyy-MM-dd HH:mm:ss") as two equal-weight lines.
 * Pure date ("yyyy-MM-dd") or pure time strings are rendered as a single line.
 */
function renderTimeCell(val: unknown) {
  if (val === null || val === undefined || val === '') {
    return h('span', { style: 'color: #cbd5e1' }, '-')
  }
  const str = String(val)
  // Detect "yyyy-MM-dd HH:mm:ss" pattern: split on the space separator
  const spaceIdx = str.indexOf(' ')
  if (spaceIdx > 0 && spaceIdx < str.length - 1) {
    const datePart = str.slice(0, spaceIdx)
    const timePart = str.slice(spaceIdx + 1)
    return h('div', { style: 'display: flex; flex-direction: column; gap: 2px; line-height: 1.4' }, [
      h('span', { style: 'font-size: 13px; color: #374151' }, datePart),
      h('span', { style: 'font-size: 13px; color: #374151' }, timePart),
    ])
  }
  return h('span', { style: 'font-size: 13px; color: #374151' }, str)
}

const columns = computed<DataTableColumn[]>(() => {
  const totalItems = visibleItems.value.length
  // +2 for createdAt / updatedAt
  const shouldFreeze = totalItems > 4

  // 最左侧勾选列（type: 'selection' 是 Naive UI 内置多选列）
  const cols: DataTableColumn[] = [{ type: 'selection', fixed: shouldFreeze ? 'left' : undefined }]

  cols.push(...visibleItems.value.map((item, index) => {
    const isMultiline = item.type === 'text' && (item as any).isMultilineOrRich
    return {
    title: item.title,
    key: item.name,
    ellipsis: isMultiline
      ? { tooltip: { contentStyle: 'max-width: 360px; max-height: 240px; overflow-y: auto; white-space: pre-wrap; word-break: break-all; overflow-wrap: break-word' } as const }
      : { tooltip: true },
    resizable: true,
    minWidth: 120,
    ...(isMultiline ? { width: 200 } : {}),
    ...(shouldFreeze && index < 4 ? { fixed: 'left' as const } : {}),
    render(row: Record<string, any>) {
      return h(FieldRenderer, {
        item,
        mode: 'display',
        value: row[item.name] ?? null
      })
    }
  }}))

  // 固定追加：创建时间、更新时间
  cols.push({
    title: '创建时间',
    key: 'createdAt',
    width: 150,
    resizable: true,
    render(row: Record<string, any>) {
      return renderTimeCell(row.createdAt)
    }
  })
  cols.push({
    title: '更新时间',
    key: 'updatedAt',
    width: 150,
    resizable: true,
    render(row: Record<string, any>) {
      return renderTimeCell(row.updatedAt)
    }
  })

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
  // +2 for createdAt/updatedAt columns (150 each), +1 for checkbox column (48)
  if (visibleItems.value.length > 4) {
    return visibleItems.value.length * 150 + 120 + 300 + 48
  }
  return undefined
})

function handlePageChange(page: number) {
  emit('update:pagination', { ...props.pagination, page })
}

function handlePageSizeChange(pageSize: number) {
  emit('update:pagination', { ...props.pagination, pageSize, page: 1 })
}

function handleCheckedRowKeysChange(keys: (string | number)[]) {
  emit('update:checkedRowKeys', keys)
}
</script>

<template>
  <NDataTable
    :columns="columns"
    :data="data"
    :loading="loading"
    :pagination="pagination"
    :scroll-x="scrollX"
    :checked-row-keys="checkedRowKeys"
    :row-key="(row: Record<string, any>) => bizKey ? row[bizKey] : row._rowIndex"
    striped
    flex-height
    style="flex: 1; min-height: 0;"
    remote
    @update:page="handlePageChange"
    @update:page-size="handlePageSizeChange"
    @update:checked-row-keys="handleCheckedRowKeysChange"
  />
</template>

<style scoped>
:deep(.n-data-table-th) {
  white-space: nowrap !important;
}

:deep(.n-data-table-tr:hover > .n-data-table-td) {
  background-color: #e8f4f0 !important;
}
</style>
