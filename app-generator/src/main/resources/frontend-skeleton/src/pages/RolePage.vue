<script setup lang="ts">
import { ref } from 'vue'
import type { FormDef } from '@/schema/types'
import { useCrudPage } from '@/core/composables/useCrudPage'
import SearchForm from '@/core/SearchForm.vue'
import DataTable from '@/core/DataTable.vue'
import EditModal from '@/core/EditModal.vue'
import {
  NButton, NSpace, NPopconfirm, NModal, NCheckbox, NDivider,
  useMessage
} from 'naive-ui'
import request from '@/utils/request'

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

// ─── Grant Permissions Modal ─────────────────────────────────
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

const grantModalVisible = ref(false)
const grantModalTitle = ref('')
const grantRoleBizId = ref('')
const allPermissionGroups = ref<PermissionGroup[]>([])
const selectedPermissionCodes = ref<string[]>([])
const grantLoading = ref(false)

function findPermission(code: string): PermissionItem | undefined {
  for (const group of allPermissionGroups.value) {
    const found = group.permissions.find(p => p.code === code)
    if (found) return found
  }
  return undefined
}

async function handleOpenGrantModal(row: Record<string, any>) {
  grantRoleBizId.value = row[bizKey.value] as string
  grantModalTitle.value = `授予权限 — ${row.roleName}`
  grantLoading.value = true
  grantModalVisible.value = true

  try {
    const [permRes, rolePermRes] = await Promise.all([
      request.post('/api/v1/permission/listPermissions'),
      request.post('/api/v1/role/listRolePermissions', { roleBizId: grantRoleBizId.value }),
    ])
    allPermissionGroups.value = permRes.data.data as PermissionGroup[]
    selectedPermissionCodes.value = rolePermRes.data.data as string[]
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '加载权限数据失败')
    grantModalVisible.value = false
  } finally {
    grantLoading.value = false
  }
}

function isGroupAllSelected(group: PermissionGroup): boolean {
  return group.permissions.every(p => selectedPermissionCodes.value.includes(p.code))
}

function isGroupPartialSelected(group: PermissionGroup): boolean {
  const selected = group.permissions.filter(p => selectedPermissionCodes.value.includes(p.code))
  return selected.length > 0 && selected.length < group.permissions.length
}

function toggleGroupAll(group: PermissionGroup, checked: boolean) {
  if (checked) {
    const set = new Set([...selectedPermissionCodes.value, ...group.permissions.map(p => p.code)])
    selectedPermissionCodes.value = [...set]
  } else {
    const codes = new Set(group.permissions.map(p => p.code))
    selectedPermissionCodes.value = selectedPermissionCodes.value.filter(c => !codes.has(c))
  }
}

function handlePermissionCheck(code: string, checked: boolean) {
  const set = new Set(selectedPermissionCodes.value)
  if (checked) {
    set.add(code)
    const perm = findPermission(code)
    if (perm?.baseOn) {
      set.add(perm.baseOn)
    }
  } else {
    set.delete(code)
    for (const group of allPermissionGroups.value) {
      for (const p of group.permissions) {
        if (p.baseOn === code) {
          set.delete(p.code)
        }
      }
    }
  }
  selectedPermissionCodes.value = [...set]
}

async function handleGrantSubmit() {
  grantLoading.value = true
  try {
    await request.post('/api/v1/role/grantPermissions', {
      roleBizId: grantRoleBizId.value,
      permissionCodes: selectedPermissionCodes.value,
    })
    message.success('权限授予成功')
    grantModalVisible.value = false
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '权限授予失败')
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
      <DataTable
        :items="schema.items"
        :data="tableData"
        :loading="tableLoading"
        :form-title="schema.title"
        :editing-row-key="editingRowKey"
        :biz-key="bizKey"
        :pagination="pagination"
        :checked-row-keys="checkedRowKeys"
        :permissions="permissions"
        :extra-actions="[
          { label: '授予权限', type: 'info', permission: 'GRANT_PERMISSION', onClick: handleOpenGrantModal }
        ]"
        @edit="handleEdit"
        @delete="handleDelete"
        @update:pagination="handlePaginationUpdate"
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
      :style="{ width: '600px' }"
      :mask-closable="false"
    >
      <div v-if="grantLoading" style="text-align: center; padding: 40px 0; color: #94a3b8">
        加载中...
      </div>
      <div v-else>
        <div v-for="group in allPermissionGroups" :key="group.groupCode" style="margin-bottom: 16px">
          <div style="display: flex; align-items: center; margin-bottom: 8px">
            <NCheckbox
              :checked="isGroupAllSelected(group)"
              :indeterminate="isGroupPartialSelected(group)"
              @update:checked="(v: boolean) => toggleGroupAll(group, v)"
            >
              <span style="font-weight: 600; font-size: 14px; color: #1e293b">{{ group.groupTitle }}</span>
            </NCheckbox>
          </div>
          <div style="padding-left: 24px; display: flex; flex-wrap: wrap; gap: 8px 16px">
            <NCheckbox
              v-for="perm in group.permissions"
              :key="perm.code"
              :checked="selectedPermissionCodes.includes(perm.code)"
              :label="perm.title"
              @update:checked="(v: boolean) => handlePermissionCheck(perm.code, v)"
            />
          </div>
          <NDivider style="margin: 12px 0" />
        </div>
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
