// protocol/request-builder.ts
// Translates frontend form state into backend request DTOs.
// All field name and suffix rules sourced from contract.md.

import type { ItemDef } from '@/schema/types'

export interface PaginationInput {
  pageNum: number    // contract.md §3: backend uses pageNum
  pageSize: number
}

export interface SortInput {
  field: string
  direction: 'asc' | 'desc'
}

export function buildListRequest(
  items: ItemDef[],
  formState: Record<string, unknown>,
  pagination: PaginationInput,
  sort?: SortInput
): Record<string, unknown> {
  const out: Record<string, unknown> = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
  }
  for (const item of items) {
    const v = formState[item.name]
    Object.assign(out, buildItemFilter(item, v))
  }
  // 创建时间范围查询：SearchForm 已将 datetimerange 转换为格式化字符串写入 formState
  if (formState.createdAtStart != null) out.createdAtStart = formState.createdAtStart
  if (formState.createdAtEnd != null) out.createdAtEnd = formState.createdAtEnd
  if (sort) {
    out.sortField = sort.field
    out.sortDirection = sort.direction
  }
  return out
}

function buildItemFilter(item: ItemDef, v: unknown): Record<string, unknown> {
  switch (item.type) {
    case 'text':
      // like filter: single string value
      return (v != null && v !== '') ? { [item.name]: v } : {}
    case 'number':
    case 'select':
    case 'onOff':
      // in filter: backend expects List<T>, wrap scalar in array
      return v != null ? { [item.name]: [v] } : {}
    case 'multiSelect':
      // in filter: component already returns array
      return (Array.isArray(v) && v.length > 0) ? { [item.name]: v } : {}
    case 'time':
      // date/time range: see buildDateRange
      return buildDateRange(item, v, 'Start', 'End')
    case 'secret':
      // secret fields are not searchable
      return {}
    default:
      return {}
  }
}

function buildDateRange(
  item: ItemDef,
  v: unknown,
  startSuffix: string,
  endSuffix: string
): Record<string, unknown> {
  if (!Array.isArray(v) || v.length !== 2) return {}
  const [start, end] = v
  const out: Record<string, unknown> = {}
  if (start != null) out[`${item.name}${startSuffix}`] = start
  if (end != null) out[`${item.name}${endSuffix}`] = end
  return out
}


export function buildSaveRequest(
  items: ItemDef[],
  formState: Record<string, unknown>,
  mode: 'create' | 'edit'
): Record<string, unknown> {
  const out: Record<string, unknown> = {}
  for (const item of items) {
    // canInputOnInit/canInputOnEdit default to true when absent (backwards-compatible)
    const canInput = mode === 'create'
      ? (item.canInputOnInit !== false)
      : (item.canInputOnEdit !== false)
    // If user cannot input in this mode, do not transmit the field
    if (!canInput) continue
    out[item.name] = formState[item.name] ?? null
  }
  return out
}
