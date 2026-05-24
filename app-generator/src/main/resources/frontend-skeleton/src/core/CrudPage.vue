<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { NButton, useMessage } from 'naive-ui'
import type { PaginationProps } from 'naive-ui'
import type { FormDef } from '@/schema/types'
import { deriveApiBasePath } from '@/utils/naming'
import request from '@/utils/request'
import type { ApiBaseResult, PageResult } from '@/utils/request'
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'
import EditModal from './EditModal.vue'

const props = defineProps<{
  schema: FormDef
}>()

const message = useMessage()
const apiBase = deriveApiBasePath(props.schema.name)

const searchParams = ref<Record<string, any>>({})
const tableData = ref<Record<string, any>[]>([])
const tableLoading = ref(false)
const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const modalVisible = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const formData = ref<Record<string, any>>({})
const submitLoading = ref(false)

async function fetchData() {
  tableLoading.value = true
  try {
    const { data } = await request.post<ApiBaseResult<PageResult<Record<string, any>>>>(`${apiBase}/list`, {
      ...searchParams.value,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    })
    tableData.value = data.result.list
    pagination.itemCount = data.result.count
  } catch (e: any) {
    message.error(e.message || '查询失败')
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  pagination.page = 1
  fetchData()
}

function handlePaginationUpdate(p: PaginationProps) {
  pagination.page = p.page
  pagination.pageSize = p.pageSize
  fetchData()
}

function handleCreate() {
  modalMode.value = 'create'
  formData.value = {}
  modalVisible.value = true
}

function handleEdit(row: Record<string, any>) {
  modalMode.value = 'edit'
  formData.value = { ...row }
  modalVisible.value = true
}

async function handleDelete(row: Record<string, any>) {
  try {
    await request.post<ApiBaseResult>(`${apiBase}/delete`, { id: row.id })
    message.success('删除成功')
    fetchData()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await request.post<ApiBaseResult>(`${apiBase}/save`, formData.value)
    message.success(modalMode.value === 'create' ? '创建成功' : '更新成功')
    modalVisible.value = false
    fetchData()
  } catch (e: any) {
    message.error(e.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchData)
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
        <NButton type="primary" @click="handleCreate">新建</NButton>
      </div>
      <DataTable
        :items="schema.items"
        :data="tableData"
        :loading="tableLoading"
        :pagination="pagination"
        @edit="handleEdit"
        @delete="handleDelete"
        @update:pagination="handlePaginationUpdate"
      />
    </div>
    <EditModal
      :visible="modalVisible"
      :mode="modalMode"
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

.crud-table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
</style>
