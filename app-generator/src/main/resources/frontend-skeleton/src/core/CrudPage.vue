<script setup lang="ts">
import { NButton, NSpace, NPopconfirm, NTooltip } from 'naive-ui'
import type { FormDef } from '@/schema/types'
import { useCrudPage } from './composables/useCrudPage'
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'
import EditModal from './EditModal.vue'

const props = defineProps<{
  schema: FormDef
  permissions?: { list: string; create: string; update: string; delete: string }
}>()

const {
  searchParams, tableData, tableLoading, pagination,
  checkedRowKeys, editingRowKey, bizKey, currentSort,
  modalVisible, modalMode, formData, submitLoading,
  handleSearch, handleReset,
  handleCreate, handleEdit, handleDelete, handleBatchDelete,
  handleSubmit, handlePaginationUpdate, handleSortChange,
} = useCrudPage(() => props.schema)
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
        <div class="crud-table-header-left">
          <h3 class="crud-table-title">{{ schema.title }}</h3>
          <NTooltip v-if="schema.desc && schema.desc.length > 20" :style="{ maxWidth: '360px' }">
            <template #trigger>
              <span class="crud-table-desc">{{ schema.desc }}</span>
            </template>
            {{ schema.desc }}
          </NTooltip>
          <span v-else-if="schema.desc" class="crud-table-desc crud-table-desc--short">{{ schema.desc }}</span>
        </div>
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
        :current-sort="currentSort"
        @edit="handleEdit"
        @delete="handleDelete"
        @update:pagination="handlePaginationUpdate"
        @update:checked-row-keys="checkedRowKeys = $event"
        @sort-change="handleSortChange"
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

.crud-table-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.crud-table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  flex-shrink: 0;
}

.crud-table-desc {
  font-size: 13px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 360px;
  cursor: default;
}

.crud-table-desc--short {
  overflow: visible;
  text-overflow: unset;
  max-width: none;
}
</style>
