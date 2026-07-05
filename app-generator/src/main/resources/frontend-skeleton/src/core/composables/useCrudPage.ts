import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useMessage } from 'naive-ui'
import type { PaginationProps } from 'naive-ui'
import type { FormDef } from '@/schema/types'
import request from '@/utils/request'
import { endpointOf } from '../protocol/endpoints'
import { buildListRequest, buildCreateRequest, buildUpdateRequest } from '../protocol/request-builder'
import type { SortInput } from '../protocol/request-builder'
import { parseListRow, parseDetailDto } from '../protocol/response-parser'
import { useRoute, useRouter } from 'vue-router'
import { serializeSearchToQuery, parseQueryToSearch } from '../protocol/query-sync'

/**
 * @param getSchema - 传入 getter（如 `() => props.schema`），确保路由切换时能读取到最新的 schema
 */
export function useCrudPage(getSchema: () => FormDef) {
  const message = useMessage()

  const bizKey = computed(() => {
    const name = getSchema().name
    return `${name.charAt(0).toLowerCase()}${name.slice(1)}Code`
  })
  const bizKeyPlural = computed(() => `${bizKey.value}s`)

  const searchParams = ref<Record<string, unknown>>({})
  const tableData = ref<Record<string, unknown>[]>([])
  const tableLoading = ref(false)
  const editingRowKey = ref<unknown>(null)
  const editBizKeyValue = ref<unknown>(null)
  const pagination = reactive<PaginationProps>({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    showSizePicker: true,
    pageSizes: [10, 20, 50],
  })

  const checkedRowKeys = ref<(string | number)[]>([])

  const sortState = ref<SortInput | null>(null)
  const currentSort = computed(() => {
    if (!sortState.value) return null
    return {
      columnKey: sortState.value.field,
      order: (sortState.value.direction === 'asc' ? 'ascend' : 'descend') as 'ascend' | 'descend',
    }
  })
  const modalVisible = ref(false)
  const modalMode = ref<'create' | 'edit'>('create')
  const formData = ref<Record<string, unknown>>({})
  const submitLoading = ref(false)

  async function fetchData() {
    const schema = getSchema()
    tableLoading.value = true
    checkedRowKeys.value = []
    try {
      const reqBody = buildListRequest(
        schema.items,
        searchParams.value,
        { pageNum: pagination.page!, pageSize: pagination.pageSize! },
        sortState.value ?? undefined,
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
    const schema = getSchema()
    const defaults: Record<string, unknown> = {}
    for (const item of schema.items) {
      if (item.type === 'onOff') defaults[item.name] = false
    }
    formData.value = defaults
    modalVisible.value = true
  }

  async function handleEdit(row: Record<string, unknown>) {
    const schema = getSchema()
    editingRowKey.value = row[bizKey.value]
    editBizKeyValue.value = row[bizKey.value]
    try {
      const res = await request.post(
        endpointOf(schema.name, 'getDetail'),
        { [bizKey.value]: row[bizKey.value] },
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
    const schema = getSchema()
    try {
      await request.post(
        endpointOf(schema.name, 'delete'),
        { [bizKeyPlural.value]: [row[bizKey.value]] },
      )
      message.success('删除成功')
      fetchData()
    } catch (e: unknown) {
      message.error((e instanceof Error ? e.message : String(e)) || '删除失败')
    }
  }

  async function handleBatchDelete() {
    if (checkedRowKeys.value.length === 0) return
    const schema = getSchema()
    try {
      await request.post(
        endpointOf(schema.name, 'delete'),
        { [bizKeyPlural.value]: checkedRowKeys.value },
      )
      message.success(`已删除 ${checkedRowKeys.value.length} 条记录`)
      checkedRowKeys.value = []
      fetchData()
    } catch (e: unknown) {
      message.error((e instanceof Error ? e.message : String(e)) || '批量删除失败')
    }
  }

  async function handleSubmit() {
    const schema = getSchema()
    submitLoading.value = true
    try {
      let reqBody: Record<string, unknown>
      let endpoint: string
      if (modalMode.value === 'create') {
        reqBody = buildCreateRequest(schema.items, formData.value)
        endpoint = endpointOf(schema.name, 'create')
      } else {
        reqBody = buildUpdateRequest(schema.items, formData.value, bizKey.value)
        reqBody[bizKey.value] = editBizKeyValue.value
        endpoint = endpointOf(schema.name, 'update')
      }
      await request.post(endpoint, reqBody)
      message.success(modalMode.value === 'create' ? '创建成功' : '更新成功')
      modalVisible.value = false
      fetchData()
    } catch (e: unknown) {
      message.error((e instanceof Error ? e.message : String(e)) || '保存失败')
    } finally {
      submitLoading.value = false
    }
  }

  function handleSortChange(sorter: { columnKey: string; order: 'ascend' | 'descend' | false } | null) {
    if (!sorter || sorter.order === false) {
      sortState.value = null
    } else {
      sortState.value = {
        field: sorter.columnKey,
        direction: sorter.order === 'ascend' ? 'asc' : 'desc',
      }
    }
    pagination.page = 1
    fetchData()
  }

  const route = useRoute()
  const router = useRouter()
  let syncing = false

  function applyQueryToSearch() {
    const parsed = parseQueryToSearch(getSchema().items, route.query as Record<string, unknown>)
    if (Object.keys(parsed).length === 0) return false
    syncing = true
    searchParams.value = parsed
    pagination.page = 1
    void nextTick(() => { syncing = false })
    return true
  }

  function writeSearchToQuery() {
    if (syncing) return
    const next = serializeSearchToQuery(getSchema().items, searchParams.value)
    const cur = route.query
    const sameKeys = Object.keys(next).length === Object.keys(cur).length
      && Object.entries(next).every(([k, v]) => cur[k] === v)
    if (sameKeys) return
    syncing = true
    router.replace({ query: next }).catch(() => { /* ignore NavigationDuplicated */ })
    void nextTick(() => { syncing = false })
  }

  // 输入 → URL（debounce 300ms）
  let writeTimer: ReturnType<typeof setTimeout> | null = null
  watch(searchParams, () => {
    if (syncing) return
    if (writeTimer) clearTimeout(writeTimer)
    writeTimer = setTimeout(writeSearchToQuery, 300)
  }, { deep: true })

  // URL → 输入（前进/后退/分享链接）
  watch(() => route.query, () => {
    if (syncing) return
    if (applyQueryToSearch()) {
      fetchData()
    }
  })

  // 初始化：优先从 URL query 回填，再 fetchData
  onMounted(() => {
    applyQueryToSearch()
    fetchData()
  })
  // 当 schema 切换时（Vue Router 复用组件实例），重新加载数据 + 清 URL query
  watch(() => getSchema().name, () => {
    pagination.page = 1
    searchParams.value = {}
    sortState.value = null
    syncing = true
    router.replace({ query: {} }).catch(() => { /* ignore */ })
    void nextTick(() => { syncing = false })
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
    currentSort,
    modalVisible,
    modalMode,
    formData,
    submitLoading,
    fetchData,
    handleSearch,
    handleReset,
    handlePaginationUpdate,
    handleSortChange,
    handleCreate,
    handleEdit,
    handleDelete,
    handleBatchDelete,
    handleSubmit,
  }
}
