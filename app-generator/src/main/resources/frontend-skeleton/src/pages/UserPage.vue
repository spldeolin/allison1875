<script setup lang="ts">
import { h, ref, computed, onMounted } from 'vue'
import type { FormDef } from '@/schema/types'
import { useCrudPage } from '@/core/composables/useCrudPage'
import SearchForm from '@/core/SearchForm.vue'
import EditModal from '@/core/EditModal.vue'
import {
  NDataTable, NButton, NSpace, NPopconfirm, NModal, NCheckbox, NCheckboxGroup,
  NTag, NPopover,
  useMessage
} from 'naive-ui'
import type { DataTableColumn } from 'naive-ui'
import request from '@/utils/request'
import { checkPermission } from '@/directives/usePermission'

const props = defineProps<{
  schema: FormDef
  permissions?: { list: string; create: string; update: string; delete: string }
}>()
const message = useMessage()

const {
  searchParams, tableData, tableLoading, pagination,
  checkedRowKeys, editingRowKey, bizKey,
  modalVisible, modalMode, formData, submitLoading,
  fetchData, handleSearch, handleReset,
  handleCreate, handleEdit, handleDelete, handleBatchDelete,
  handleSubmit, handlePaginationUpdate,
} = useCrudPage(() => props.schema)

// ─── Permission Groups (for popover display) ───────────────
interface PermissionItem {
  code: string
  title: string
  baseOn: string | null
}
interface PermissionGroup {
  groupCode: string
  groupTitle: string
  permissions: PermissionItem[]
}

const allPermissionGroups = ref<PermissionGroup[]>([])

onMounted(async () => {
  try {
    const res = await request.post('/api/v1/permission/listPermissions')
    allPermissionGroups.value = res.data.data as PermissionGroup[]
  } catch (_) {
    // silent — popover will show codes if groups fail to load
  }
})

function getGroupedPermissions(codes: string[]): { groupTitle: string; items: string[] }[] {
  const codeSet = new Set(codes)
  const result: { groupTitle: string; items: string[] }[] = []
  for (const group of allPermissionGroups.value) {
    const matched = group.permissions.filter(p => codeSet.has(p.code))
    if (matched.length > 0) {
      result.push({ groupTitle: group.groupTitle, items: matched.map(p => p.title) })
    }
  }
  return result
}

// ─── Table Columns ────────────────────────────────────────
const MAX_VISIBLE_TAGS = 3

function renderTimeCell(val: unknown) {
  if (val === null || val === undefined || val === '') {
    return h('span', { style: 'color: #cbd5e1' }, '-')
  }
  const str = String(val)
  const spaceIdx = str.indexOf(' ')
  if (spaceIdx > 0 && spaceIdx < str.length - 1) {
    return h('div', { style: 'display: flex; flex-direction: column; gap: 2px; line-height: 1.4' }, [
      h('span', { style: 'font-size: 13px; color: #374151' }, str.slice(0, spaceIdx)),
      h('span', { style: 'font-size: 13px; color: #374151' }, str.slice(spaceIdx + 1)),
    ])
  }
  return h('span', { style: 'font-size: 13px; color: #374151' }, str)
}

const columns = computed<DataTableColumn[]>(() => {
  const cols: DataTableColumn[] = [
    { type: 'selection', fixed: 'left' },
    {
      title: `${props.schema.title}ID`,
      key: bizKey.value,
      width: 140,
      fixed: 'left',
      ellipsis: { tooltip: true },
    },
    {
      title: '用户名',
      key: 'username',
      width: 150,
      fixed: 'left',
      ellipsis: { tooltip: true },
    },
    {
      title: '用户昵称',
      key: 'nickName',
      width: 120,
      ellipsis: { tooltip: true },
    },
    {
      title: '已授予权限',
      key: '_grantedPermissions',
      width: 220,
      render(row: Record<string, any>) {
        const roles = (row.grantedRoles || []) as { bizId: string; roleName: string }[]
        const perms = (row.grantedPermissions || []) as string[]
        if (roles.length === 0) {
          return h('span', { style: 'color: #94a3b8; font-size: 13px' }, '暂无')
        }
        const visibleRoles = roles.slice(0, MAX_VISIBLE_TAGS)
        const overflow = roles.length - MAX_VISIBLE_TAGS
        const tags = visibleRoles.map(r =>
          h(NTag, { size: 'small', type: 'info', bordered: false }, { default: () => r.roleName })
        )
        if (overflow > 0) {
          tags.push(h(NTag, { size: 'small', bordered: false }, { default: () => `+${overflow}` }))
        }
        const tagRow = h('div', { style: 'display: flex; flex-wrap: wrap; gap: 4px; align-items: center' }, tags)

        const grouped = getGroupedPermissions(perms)
        const popoverContent = perms.length === 0
          ? h('div', { style: 'color: #94a3b8; padding: 8px' }, '暂无权限')
          : h('div', { style: 'max-width: 340px; max-height: 380px; overflow-y: auto; padding: 4px 0' }, [
              h('div', { style: 'font-size: 12px; color: #64748b; margin-bottom: 8px' },
                `已授予权限（共 ${perms.length} 项）`),
              ...grouped.map(g => h('div', { style: 'margin-bottom: 10px' }, [
                h('div', { style: 'font-weight: 600; font-size: 13px; color: #334155; margin-bottom: 4px' }, g.groupTitle),
                h('div', { style: 'display: flex; flex-wrap: wrap; gap: 4px 12px; padding-left: 8px' },
                  g.items.map(title => h('span', { style: 'font-size: 12px; color: #475569' }, `· ${title}`))
                ),
              ]))
            ])

        return h(NPopover, { trigger: 'hover', placement: 'bottom' }, {
          trigger: () => tagRow,
          default: () => popoverContent,
        })
      },
    },
    {
      title: '最后登录时间',
      key: 'lastLoginAt',
      width: 150,
      render(row: Record<string, any>) {
        return renderTimeCell(row.lastLoginAt)
      },
    },
    {
      title: '创建时间',
      key: 'createdAt',
      width: 150,
      render(row: Record<string, any>) {
        return renderTimeCell(row.createdAt)
      },
    },
    {
      title: '操作',
      key: '_actions',
      width: 200,
      fixed: 'right',
      render(row: Record<string, any>) {
        const btns: any[] = []
        if (checkPermission('GRANT_ROLE')) {
          btns.push(h(NButton, {
            size: 'small', quaternary: true, type: 'info',
            disabled: editingRowKey.value != null,
            onClick: () => handleOpenGrantModal(row),
          }, { default: () => '授予角色' }))
        }
        if (checkPermission(props.permissions?.update)) {
          btns.push(h(NButton, {
            size: 'small', quaternary: true, type: 'primary',
            disabled: editingRowKey.value != null,
            onClick: () => handleEdit(row),
          }, { default: () => '编辑' }))
        }
        if (checkPermission(props.permissions?.delete)) {
          btns.push(h(NPopconfirm, {
            onPositiveClick: () => handleDelete(row),
          }, {
            trigger: () => h(NButton, {
              size: 'small', quaternary: true, type: 'error',
              disabled: editingRowKey.value != null,
            }, { default: () => '删除' }),
            default: () => '确定要删除该记录吗？',
          }))
        }
        return h(NSpace, { wrap: false, size: 4 }, { default: () => btns })
      },
    },
  ]
  return cols
})

// ─── Grant Roles Modal ─────────────────────────────────
interface RoleBrief {
  bizId: string
  roleName: string
}

const grantModalVisible = ref(false)
const grantModalTitle = ref('')
const grantUserBizId = ref('')
const allRoles = ref<RoleBrief[]>([])
const selectedRoleBizIds = ref<string[]>([])
const grantLoading = ref(false)

async function handleOpenGrantModal(row: Record<string, any>) {
  grantUserBizId.value = row[bizKey.value] as string
  grantModalTitle.value = `授予角色 — ${row.username}`
  grantLoading.value = true
  grantModalVisible.value = true

  try {
    const [rolesRes, userRolesRes] = await Promise.all([
      request.post('/api/v1/role/listRoles', { pageNum: 1, pageSize: 9999 }),
      request.post('/api/v1/user/listUserRoles', { userBizId: grantUserBizId.value }),
    ])
    const pageResult = rolesRes.data.data as { list: Record<string, any>[] }
    allRoles.value = pageResult.list.map((r: Record<string, any>) => ({
      bizId: r.roleCode as string,
      roleName: r.roleName as string,
    }))
    const userRoles = userRolesRes.data.data as RoleBrief[]
    selectedRoleBizIds.value = userRoles.map(r => r.bizId)
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '加载角色数据失败')
    grantModalVisible.value = false
  } finally {
    grantLoading.value = false
  }
}

async function handleGrantSubmit() {
  grantLoading.value = true
  try {
    await request.post('/api/v1/user/grantRoles', {
      userBizId: grantUserBizId.value,
      roleBizIds: selectedRoleBizIds.value,
    })
    message.success('角色授予成功')
    grantModalVisible.value = false
    fetchData()
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '角色授予失败')
  } finally {
    grantLoading.value = false
  }
}
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
        <NSpace>
          <NButton type="primary" v-permission="permissions?.create" @click="handleCreate">创建</NButton>
          <NPopconfirm
            :disabled="checkedRowKeys.length === 0"
            @positive-click="handleBatchDelete"
          >
            <template #trigger>
              <NButton
                type="error"
                :disabled="checkedRowKeys.length === 0"
                v-permission="permissions?.delete"
              >
                批量删除{{ checkedRowKeys.length > 0 ? `（${checkedRowKeys.length}）` : '' }}
              </NButton>
            </template>
            确定要删除选中的 {{ checkedRowKeys.length }} 条记录吗？
          </NPopconfirm>
        </NSpace>
      </div>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="tableLoading"
        :pagination="pagination"
        :checked-row-keys="checkedRowKeys"
        :row-key="(row: Record<string, any>) => row[bizKey]"
        :scroll-x="1100"
        @update:page="(page: number) => handlePaginationUpdate({ ...pagination, page })"
        @update:page-size="(size: number) => handlePaginationUpdate({ ...pagination, pageSize: size, page: 1 })"
        @update:checked-row-keys="checkedRowKeys = $event"
      />
    </div>
    <EditModal
      :visible="modalVisible"
      :mode="modalMode"
      :form-title="schema.title"
      :items="schema.items"
      v-model="formData"
      :loading="submitLoading"
      @update:visible="modalVisible = $event"
      @submit="handleSubmit"
    />

    <NModal
      v-model:show="grantModalVisible"
      preset="card"
      :title="grantModalTitle"
      :style="{ width: '480px' }"
      :mask-closable="false"
    >
      <div v-if="grantLoading" style="text-align: center; padding: 40px 0; color: #94a3b8">
        加载中...
      </div>
      <div v-else>
        <div v-if="allRoles.length === 0" style="text-align: center; padding: 20px; color: #94a3b8">
          暂无可用角色
        </div>
        <NCheckboxGroup v-else v-model:value="selectedRoleBizIds">
          <div style="display: flex; flex-direction: column; gap: 8px">
            <NCheckbox
              v-for="role in allRoles"
              :key="role.bizId"
              :value="role.bizId"
              :label="role.roleName"
            />
          </div>
        </NCheckboxGroup>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px">
          <NButton @click="grantModalVisible = false">取消</NButton>
          <NButton type="primary" :loading="grantLoading" @click="handleGrantSubmit">确定</NButton>
        </div>
      </template>
    </NModal>
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
  justify-content: space-between;
  margin-bottom: 16px;
}
.crud-table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
</style>
