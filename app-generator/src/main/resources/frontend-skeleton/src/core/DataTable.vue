<script setup lang="ts">
import { computed, h } from 'vue'
import { NDataTable, NButton, NSpace, NPopconfirm } from 'naive-ui'
import type { DataTableColumn, PaginationProps } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'
import { isVisible } from './protocol/field-policy'
import { checkPermission } from '@/directives/usePermission'

const props = defineProps<{
  items: ItemDef[]
  data: Record<string, any>[]
  loading: boolean
  pagination: PaginationProps
  /** Form title for display (e.g. "学生档案") */
  formTitle?: string
  /** The bizKey value of the row whose detail is currently loading, or null */
  editingRowKey?: unknown
  /** The bizKey field name (e.g. "studentProfileCode") */
  bizKey?: string
  /** Currently checked row keys for batch delete */
  checkedRowKeys?: (string | number)[]
  /** Permission codes for the current form */
  permissions?: { list: string; create: string; update: string; delete: string }
  /** Extra action buttons to render in the actions column */
  extraActions?: Array<{
    label: string
    type?: 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error'
    permission?: string
    onClick: (row: Record<string, any>) => void
  }>
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

const bizKeyColWidth = computed(() => {
  const title = `${props.formTitle || ''}ID`
  return Math.max(140, title.length * 14 + 24)
})

const columns = computed<DataTableColumn[]>(() => {
  const cols: DataTableColumn[] = [{ type: 'selection', fixed: 'left' }]

  if (props.bizKey) {
    cols.push({
      title: `${props.formTitle || ''}ID`,
      key: props.bizKey,
      width: bizKeyColWidth.value,
      fixed: 'left',
      ellipsis: { tooltip: { contentStyle: 'max-width: 360px; max-height: 240px; overflow-y: auto; white-space: pre-wrap; word-break: break-all; overflow-wrap: break-word' } },
      render(row: Record<string, any>) {
        const val = row[props.bizKey!]
        if (!val) return h('span', { style: 'color: #cbd5e1' }, '-')
        return h('span', null, String(val))
      }
    })
  }

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
    ...(index === 0 ? { fixed: 'left' as const, width: 150 } : {}),
    render(row: Record<string, any>) {
      return h(FieldRenderer, {
        item,
        mode: 'display',
        value: row[item.name] ?? null
      })
    }
  }}))

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
    width: 120 + (props.extraActions?.length ?? 0) * 80,
    fixed: 'right',
    render(row: Record<string, any>) {
      const isThisRowLoading = props.editingRowKey != null
        && props.bizKey != null
        && row[props.bizKey] === props.editingRowKey
      return h(NSpace, { wrap: false, size: 4 }, {
        default: () => [
          ...(props.extraActions || []).filter(action => checkPermission(action.permission)).map(action =>
            h(NButton, {
              size: 'small',
              quaternary: true,
              type: (action.type || 'info') as any,
              disabled: props.editingRowKey != null,
              onClick: () => action.onClick(row)
            }, { default: () => action.label })
          ),
          ...(checkPermission(props.permissions?.update) ? [h(NButton, {
            size: 'small',
            quaternary: true,
            type: 'primary',
            loading: isThisRowLoading,
            disabled: props.editingRowKey != null && !isThisRowLoading,
            onClick: () => emit('edit', row)
          }, { default: () => '编辑' })] : []),
          ...(checkPermission(props.permissions?.delete) ? [h(NPopconfirm, { onPositiveClick: () => emit('delete', row) }, {
            trigger: () => h(NButton, {
              size: 'small',
              quaternary: true,
              type: 'error',
              disabled: props.editingRowKey != null
            }, { default: () => '删除' }),
            default: () => '确定要删除吗？'
          })] : [])
        ]
      })
    }
  })

  return cols
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

const scrollX = computed(() => {
  let width = 50 // selection column
  if (props.bizKey) width += bizKeyColWidth.value
  const visibleCount = visibleItems.value.length
  if (visibleCount > 0) width += 150 // first visible column (fixed left)
  if (visibleCount > 1) width += (visibleCount - 1) * 120
  width += 300 // createdAt + updatedAt
  width += 120 + (props.extraActions?.length ?? 0) * 80 // actions
  return width
})

function rowProps(row: Record<string, any>) {
  return {
    style: 'cursor: default',
    onClick: (e: MouseEvent) => {
      const target = e.target as HTMLElement
      if (target.closest('button') || target.closest('.n-popconfirm') || target.closest('.n-checkbox')) return
      const key = props.bizKey ? row[props.bizKey] : row._rowIndex
      const keys = props.checkedRowKeys ? [...props.checkedRowKeys] : []
      const idx = keys.indexOf(key)
      if (idx >= 0) {
        keys.splice(idx, 1)
      } else {
        keys.push(key)
      }
      emit('update:checkedRowKeys', keys)
    }
  }
}
</script>

<template>
  <NDataTable
    :columns="columns"
    :data="data"
    :loading="loading"
    :pagination="pagination"
    :checked-row-keys="checkedRowKeys"
    :row-key="(row: Record<string, any>) => bizKey ? row[bizKey] : row._rowIndex"
    :row-props="rowProps"
    :scroll-x="scrollX"
    flex-height
    striped
    remote
    style="flex: 1; min-height: 0"
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

:deep(.n-data-table-tr--checked > .n-data-table-td) {
  background-color: transparent !important;
}
</style>
