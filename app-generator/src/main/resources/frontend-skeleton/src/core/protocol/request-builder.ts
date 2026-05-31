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
  if (start != null) out[`${item.name}${startSuffix}`] = formatDateTime(start as number, (item as import('@/schema/types').TimeItemDef).format)
  if (end != null) out[`${item.name}${endSuffix}`] = formatDateTime(end as number, (item as import('@/schema/types').TimeItemDef).format)
  return out
}

function formatDateTime(timestamp: number, format: 'date' | 'time' | 'dateTime'): string {
  const d = new Date(timestamp)
  const pad = (n: number) => String(n).padStart(2, '0')
  const ymd = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  const hms = `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  switch (format) {
    case 'date':     return ymd
    case 'time':     return hms
    case 'dateTime': return `${ymd} ${hms}`
  }
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
