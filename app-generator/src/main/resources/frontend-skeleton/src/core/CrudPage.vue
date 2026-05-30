<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { NButton, useMessage } from 'naive-ui'
import type { PaginationProps } from 'naive-ui'
import type { FormDef } from '@/schema/types'
import request from '@/utils/request'
import { endpointOf } from './protocol/endpoints'
import { buildListRequest, buildSaveRequest } from './protocol/request-builder'
import { parseListRow, parseDetailDto } from './protocol/response-parser'
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'
import EditModal from './EditModal.vue'

const props = defineProps<{
  schema: FormDef
}>()

const message = useMessage()

// Business key field name — form-generator auto-injects ${lowerCamelFormName}Code
// e.g., StudentProfile → studentProfileCode
const bizKey = `${props.schema.name.charAt(0).toLowerCase()}${props.schema.name.slice(1)}Code`
// Delete endpoint uses plural: { ${bizKey}s: List<String> } (Gap6)
const bizKeyPlural = `${bizKey}s`

const searchParams = ref<Record<string, unknown>>({})
const tableData = ref<Record<string, unknown>[]>([])
const tableLoading = ref(false)
const detailLoading = ref(false)
const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const modalVisible = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const formData = ref<Record<string, unknown>>({})
const submitLoading = ref(false)

async function fetchData() {
  tableLoading.value = true
  try {
    const reqBody = buildListRequest(
      props.schema.items,
      searchParams.value,
      { pageNum: pagination.page!, pageSize: pagination.pageSize! },
    )
    const res = await request.post(endpointOf(props.schema.name, 'list'), reqBody)
    const pageResult = res.data.data as { total: number; list: Record<string, unknown>[] }
    tableData.value = pageResult.list.map(dto => parseListRow(props.schema.items, dto))
    pagination.itemCount = pageResult.total
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '查询失败')
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchParams.value = {}
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

async function handleEdit(row: Record<string, unknown>) {
  detailLoading.value = true
  try {
    const res = await request.post(
      endpointOf(props.schema.name, 'getDetail'),
      { [bizKey]: row[bizKey] },
    )
    const detail = res.data.data as Record<string, unknown>
    formData.value = parseDetailDto(props.schema.items, detail)
    modalMode.value = 'edit'
    modalVisible.value = true
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '加载详情失败')
    // Modal does not open on getDetail failure
  } finally {
    detailLoading.value = false
  }
}

async function handleDelete(row: Record<string, unknown>) {
  try {
    // Gap6: delete uses List format — { ${bizKey}s: [code] }
    await request.post(
      endpointOf(props.schema.name, 'delete'),
      { [bizKeyPlural]: [row[bizKey]] },
    )
    message.success('删除成功')
    fetchData()
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '删除失败')
  }
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    const reqBody = buildSaveRequest(props.schema.items, formData.value, modalMode.value)
    // Edit mode: append business key so backend can identify which record to update
    if (modalMode.value === 'edit') {
      reqBody[bizKey] = formData.value[bizKey]
    }
    await request.post(endpointOf(props.schema.name, 'save'), reqBody)
    message.success(modalMode.value === 'create' ? '创建成功' : '更新成功')
    modalVisible.value = false
    fetchData()
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '保存失败')
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
        :detail-loading="detailLoading"
        :pagination="pagination"
        @edit="handleEdit"
        @delete="handleDelete"
        @update:pagination="handlePaginationUpdate"
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

.crud-table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
</style>
