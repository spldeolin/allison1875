<script setup lang="ts">
import { h, computed } from 'vue'
import type { FormDef } from '@/schema/types'
import { useCrudPage } from '@/core/composables/useCrudPage'
import SearchForm from '@/core/SearchForm.vue'
import { NDataTable, NTag, NTooltip } from 'naive-ui'
import type { DataTableColumn, SortState } from 'naive-ui'
import appDef from '@/app.json'

const props = defineProps<{
  schema: FormDef
  permissions?: { list: string; create: string; update: string; delete: string }
}>()

const operationTypeMap: Record<string, string> = {
  login: '登录',
  changePassword: '修改密码',
  logout: '退出登录',
  grantRole: '授予角色',
  grantPermission: '授予权限',
  createUser: '创建用户',
  updateUser: '编辑用户',
  deleteUser: '删除用户',
  createRole: '创建角色',
  updateRole: '编辑角色',
  deleteRole: '删除角色',
}

function getOperationTypeTitle(code: string | null | undefined): string {
  if (!code) return '-'
  if (operationTypeMap[code]) return operationTypeMap[code]
  const actionPrefixes: [string, string][] = [
    ['create', '创建'],
    ['update', '编辑'],
    ['delete', '删除'],
  ]
  for (const [prefix, label] of actionPrefixes) {
    if (code.startsWith(prefix) && code.length > prefix.length) {
      const formName = code.slice(prefix.length)
      const menu = (appDef as any).menus?.find(
        (m: any) => m.form?.name === formName
      )
      if (menu?.form?.title) {
        return `${label}${menu.form.title}`
      }
    }
  }
  return code
}

const {
  searchParams,
  tableData,
  tableLoading,
  pagination,
  checkedRowKeys,
  currentSort,
  handleSearch,
  handleReset,
  handlePaginationUpdate,
  handleSortChange,
} = useCrudPage(() => props.schema)

function sortOrderFor(key: string): 'ascend' | 'descend' | false {
  if (currentSort.value && currentSort.value.columnKey === key) {
    return currentSort.value.order
  }
  return false
}

function handleSorterChange(sorter: SortState | SortState[] | null) {
  const s = Array.isArray(sorter) ? sorter[0] : sorter
  if (!s || s.order === false) {
    handleSortChange(null)
  } else {
    handleSortChange({ columnKey: s.columnKey as string, order: s.order })
  }
}

function renderTimeCell(val: unknown) {
  if (val === null || val === undefined || val === '') {
    return h('span', { style: 'color: #cbd5e1' }, '-')
  }
  const str = String(val)
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

function renderContent(val: unknown) {
  if (val === null || val === undefined || val === '') {
    return h('span', { style: 'color: #cbd5e1' }, '-')
  }
  const str = String(val)
  if (str === '无变更内容') {
    return h('span', { style: 'color: #94a3b8; font-style: italic' }, str)
  }
  let parsed: Record<string, any>
  try {
    parsed = JSON.parse(str)
  } catch {
    return h('span', null, str)
  }
  if (typeof parsed !== 'object' || parsed === null) {
    return h('span', null, str)
  }
  const entries = Object.entries(parsed)
  const isUpdateDiff = entries.length > 0 && entries.every(
    ([, v]) => v !== null && typeof v === 'object' && 'old' in v && 'new' in v
  )
  const items = isUpdateDiff
    ? entries.map(([key, v]) => h('div', { style: 'margin-bottom: 4px; line-height: 1.5' }, [
        h('span', { style: 'font-weight: 500; color: #374151' }, `${key}: `),
        h('span', { style: 'color: #94a3b8' }, formatVal(v.old)),
        h('span', { style: 'color: #374151' }, ' → '),
        h('span', { style: 'color: #059669; font-weight: 500' }, formatVal(v.new)),
      ]))
    : entries.map(([key, v]) => h('div', { style: 'margin-bottom: 4px; line-height: 1.5' }, [
        h('span', { style: 'font-weight: 500; color: #374151' }, `${key}: `),
        h('span', { style: 'color: #374151' }, formatVal(v)),
      ]))

  const preview = items.slice(0, 3)
  const hasMore = items.length > 3
  const previewNode = h('div', { style: 'max-width: 280px' }, [
    ...preview,
    ...(hasMore ? [h('span', { style: 'color: #94a3b8; font-size: 12px' }, `...共${items.length}项`)] : []),
  ])
  const fullNode = h('div', { style: 'max-width: 400px; max-height: 300px; overflow-y: auto; padding: 4px 0' }, items)

  if (items.length <= 2) {
    return previewNode
  }
  return h(NTooltip, { placement: 'left', style: 'max-width: 420px' }, {
    trigger: () => h('div', { style: 'cursor: pointer' }, [previewNode]),
    default: () => fullNode,
  })
}

function formatVal(v: unknown): string {
  if (v === null || v === undefined) return '空'
  if (Array.isArray(v)) return v.join(', ')
  return String(v)
}

const columns = computed<DataTableColumn[]>(() => [
  {
    title: '审计日志ID',
    key: 'auditLogCode',
    width: 140,
    fixed: 'left',
    ellipsis: { tooltip: { contentStyle: 'max-width: 360px; white-space: pre-wrap; word-break: break-all' } },
  },
  {
    title: '操作类型',
    key: 'operationType',
    width: 130,
    render(row: Record<string, any>) {
      return h(NTag, { size: 'small', bordered: false }, { default: () => getOperationTypeTitle(row.operationType) })
    }
  },
  {
    title: '是否成功',
    key: 'success',
    width: 90,
    render(row: Record<string, any>) {
      return row.success
        ? h(NTag, { type: 'success', size: 'small', bordered: false }, { default: () => '成功' })
        : h(NTag, { type: 'error', size: 'small', bordered: false }, { default: () => '失败' })
    }
  },
  {
    title: '操作内容',
    key: 'content',
    minWidth: 240,
    resizable: true,
    render(row: Record<string, any>) {
      return renderContent(row.content)
    }
  },
  {
    title: '失败原因',
    key: 'failReason',
    width: 180,
    ellipsis: { tooltip: { contentStyle: 'max-width: 360px; white-space: pre-wrap; word-break: break-all' } },
    render(row: Record<string, any>) {
      if (!row.failReason) return h('span', { style: 'color: #cbd5e1' }, '-')
      return h('span', { style: 'color: #dc2626' }, row.failReason)
    }
  },
  {
    title: '操作人',
    key: 'createdBy',
    width: 100,
    sorter: true,
    sortOrder: sortOrderFor('createdBy'),
    ellipsis: { tooltip: true },
  },
  {
    title: '操作时间',
    key: 'createdAt',
    width: 150,
    sorter: true,
    sortOrder: sortOrderFor('createdAt'),
    render(row: Record<string, any>) {
      return renderTimeCell(row.createdAt)
    }
  },
])

const scrollX = computed(() => 140 + 130 + 90 + 240 + 180 + 100 + 150 + 40)
</script>

<template>
  <div class="crud-page">
    <div class="crud-search-card">
      <SearchForm
        :items="schema.items"
        v-model="searchParams"
        @search="handleSearch"
        @reset="handleReset"
      />
    </div>
    <div class="crud-table-card">
      <div class="crud-table-header">
        <h3 class="crud-table-title">{{ schema.title }}</h3>
      </div>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="tableLoading"
        :pagination="pagination"
        :checked-row-keys="checkedRowKeys"
        :row-key="(row: Record<string, any>) => row.auditLogCode"
        :scroll-x="scrollX"
        flex-height
        striped
        remote
        style="flex: 1; min-height: 0"
        @update:page="(p: number) => handlePaginationUpdate({ ...pagination, page: p })"
        @update:page-size="(ps: number) => handlePaginationUpdate({ ...pagination, pageSize: ps, page: 1 })"
        @update:checked-row-keys="checkedRowKeys = $event"
        @update:sorter="handleSorterChange"
      />
    </div>
  </div>
</template>

<style scoped>
.crud-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.crud-search-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  border: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.crud-table-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  border: 1px solid #f1f5f9;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.crud-table-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.crud-table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

:deep(.n-data-table-th) {
  white-space: nowrap !important;
}

:deep(.n-data-table-sorter) {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  height: 18px;
  width: 14px;
}

:deep(.n-data-table-sorter .n-base-icon) {
  display: none !important;
}

:deep(.n-data-table-sorter::before),
:deep(.n-data-table-sorter::after) {
  content: '';
  display: block;
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
}

:deep(.n-data-table-sorter::before) {
  border-bottom: 5px solid #c0c4cc;
}

:deep(.n-data-table-sorter::after) {
  border-top: 5px solid #c0c4cc;
}

:deep(.n-data-table-sorter.n-data-table-sorter--asc::before) {
  border-bottom-color: var(--n-th-icon-color-active, #18a058);
}

:deep(.n-data-table-sorter.n-data-table-sorter--desc::after) {
  border-top-color: var(--n-th-icon-color-active, #18a058);
}
</style>
