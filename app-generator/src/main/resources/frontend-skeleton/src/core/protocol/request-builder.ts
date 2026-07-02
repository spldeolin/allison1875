// protocol/request-builder.ts
// Translates frontend form state into backend request DTOs.
// All field name and suffix rules sourced from contract.md.

import type { ItemDef, FileValue } from '@/schema/types'

/**
 * Serialize a field's form-state value into the backend request representation.
 * file fields hold a FileValue object in form state; the backend business column
 * is the merged single string "fileKey/originFileName" (first '/' splits).
 */
function serializeFieldValue(item: ItemDef, v: unknown): unknown {
  if (item.type === 'file') {
    if (!v) return null
    if (typeof v === 'string') return v
    const fv = v as FileValue
    return fv.fileKey + '/' + fv.originFileName
  }
  return v ?? null
}

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
    out.sortBy = sort.field
    out.isAsc = sort.direction === 'asc'
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


export function buildCreateRequest(
  items: ItemDef[],
  formState: Record<string, unknown>,
): Record<string, unknown> {
  const out: Record<string, unknown> = {}
  for (const item of items) {
    if (item.canInputOnInit === false) continue
    out[item.name] = serializeFieldValue(item, formState[item.name])
  }
  return out
}

export function buildUpdateRequest(
  items: ItemDef[],
  formState: Record<string, unknown>,
  bizKey: string,
): Record<string, unknown> {
  const out: Record<string, unknown> = { [bizKey]: formState[bizKey] }
  for (const item of items) {
    if (item.canInputOnEdit === false) continue
    out[item.name] = serializeFieldValue(item, formState[item.name])
  }
  return out
}
