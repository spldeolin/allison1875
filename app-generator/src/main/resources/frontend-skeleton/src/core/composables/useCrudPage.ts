import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import type { PaginationProps } from 'naive-ui'
import type { FormDef } from '@/schema/types'
import request from '@/utils/request'
import { endpointOf } from '../protocol/endpoints'
import { buildListRequest, buildSaveRequest } from '../protocol/request-builder'
import { parseListRow, parseDetailDto } from '../protocol/response-parser'

export function useCrudPage(schema: FormDef) {
  const message = useMessage()
  const route = useRoute()

  const bizKey = `${schema.name.charAt(0).toLowerCase()}${schema.name.slice(1)}Code`
  const bizKeyPlural = `${bizKey}s`

  const searchParams = ref<Record<string, unknown>>({})
  const tableData = ref<Record<string, unknown>[]>([])
  const tableLoading = ref(false)
  const editingRowKey = ref<unknown>(null)
  const pagination = reactive<PaginationProps>({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    showSizePicker: true,
    pageSizes: [10, 20, 50],
  })

  const checkedRowKeys = ref<(string | number)[]>([])

  const modalVisible = ref(false)
  const modalMode = ref<'create' | 'edit'>('create')
  const formData = ref<Record<string, unknown>>({})
  const submitLoading = ref(false)

  async function fetchData() {
    tableLoading.value = true
    checkedRowKeys.value = []
    try {
      const reqBody = buildListRequest(
        schema.items,
        searchParams.value,
        { pageNum: pagination.page!, pageSize: pagination.pageSize! },
      )
      const res = await request.post(endpointOf(schema.name, 'list'), reqBody)
      const pageResult = res.data.data as { total: number; list: Record<string, unknown>[] }
      tableData.value = pageResult.list.map(dto => parseListRow(schema.items, dto))
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
    const defaults: Record<string, unknown> = {}
    for (const item of schema.items) {
      if (item.type === 'onOff') defaults[item.name] = false
    }
    formData.value = defaults
    modalVisible.value = true
  }

  async function handleEdit(row: Record<string, unknown>) {
    editingRowKey.value = row[bizKey]
    try {
      const res = await request.post(
        endpointOf(schema.name, 'getDetail'),
        { [bizKey]: row[bizKey] },
      )
      const detail = res.data.data as Record<string, unknown>
      formData.value = parseDetailDto(schema.items, detail)
      modalMode.value = 'edit'
      modalVisible.value = true
    } catch (e: unknown) {
      message.error((e instanceof Error ? e.message : String(e)) || '加载详情失败')
    } finally {
      editingRowKey.value = null
    }
  }

  async function handleDelete(row: Record<string, unknown>) {
    try {
      await request.post(
        endpointOf(schema.name, 'delete'),
        { [bizKeyPlural]: [row[bizKey]] },
      )
      message.success('删除成功')
      fetchData()
    } catch (e: unknown) {
      message.error((e instanceof Error ? e.message : String(e)) || '删除失败')
    }
  }

  async function handleBatchDelete() {
    if (checkedRowKeys.value.length === 0) return
    try {
      await request.post(
        endpointOf(schema.name, 'delete'),
        { [bizKeyPlural]: checkedRowKeys.value },
      )
      message.success(`已删除 ${checkedRowKeys.value.length} 条记录`)
      checkedRowKeys.value = []
      fetchData()
    } catch (e: unknown) {
      message.error((e instanceof Error ? e.message : String(e)) || '批量删除失败')
    }
  }

  async function handleSubmit() {
    submitLoading.value = true
    try {
      const reqBody = buildSaveRequest(schema.items, formData.value, modalMode.value)
      if (modalMode.value === 'edit') {
        reqBody[bizKey] = formData.value[bizKey]
      }
      await request.post(endpointOf(schema.name, 'save'), reqBody)
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
  watch(() => route.path, () => {
    pagination.page = 1
    searchParams.value = {}
    fetchData()
  })

  return {
    bizKey,
    bizKeyPlural,
    searchParams,
    tableData,
    tableLoading,
    editingRowKey,
    pagination,
    checkedRowKeys,
    modalVisible,
    modalMode,
    formData,
    submitLoading,
    fetchData,
    handleSearch,
    handleReset,
    handlePaginationUpdate,
    handleCreate,
    handleEdit,
    handleDelete,
    handleBatchDelete,
    handleSubmit,
  }
}
