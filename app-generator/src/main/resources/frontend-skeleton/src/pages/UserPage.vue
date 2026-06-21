<script setup lang="ts">
import { ref } from 'vue'
import type { FormDef } from '@/schema/types'
import { useCrudPage } from '@/core/composables/useCrudPage'
import SearchForm from '@/core/SearchForm.vue'
import DataTable from '@/core/DataTable.vue'
import EditModal from '@/core/EditModal.vue'
import {
  NButton, NSpace, NPopconfirm, NModal, NCheckbox, NCheckboxGroup,
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
          { label: '授予角色', type: 'info', permission: 'GRANT_ROLE', onClick: handleOpenGrantModal }
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
