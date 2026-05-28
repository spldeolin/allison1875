// protocol/request-builder.ts
// Translates frontend form state into backend request DTOs.
// All field name and suffix rules sourced from contract.md.

import type { ItemDef, FilterPattern } from '@/schema/types'
import { getFilterPatternsByItemType } from './field-policy'

export interface PaginationInput {
  pageNum: number    // contract.md §3: backend uses pageNum
  pageSize: number
}

export interface SortInput {
  field: string
  direction: 'asc' | 'desc'
}

// Each rule: given item and the current value in formState, returns key-value pairs for the request body.
type ListRule = (item: ItemDef, value: unknown) => Record<string, unknown>

// Gap1 decision: field names have NO suffix — use item.name directly.
// dateRange/dateTimeRange: ${name}Start / ${name}End (contract.md §4.1)
const filterPatternListRules: Record<FilterPattern, ListRule> = {
  like:          (item, v) => (v != null && v !== '') ? { [item.name]: v } : {},
  in:            (item, v) => (Array.isArray(v) && v.length > 0) ? { [item.name]: v } : {},
  ge:            (item, v) => v != null ? { [item.name]: v } : {},
  gt:            (item, v) => v != null ? { [item.name]: v } : {},
  le:            (item, v) => v != null ? { [item.name]: v } : {},
  lt:            (item, v) => v != null ? { [item.name]: v } : {},
  dateRange:     (item, v) => buildDateRange(item, v, 'Start', 'End'),
  dateTimeRange: (item, v) => buildDateRange(item, v, 'Start', 'End'),
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
    const patterns = getFilterPatternsByItemType(item.type)
    for (const p of patterns) {
      const rule = filterPatternListRules[p]
      Object.assign(out, rule(item, formState[item.name]))
    }
  }
  if (sort) {
    // Backend does not yet support sort; preserved as placeholder for future use.
    out.sortField = sort.field
    out.sortDirection = sort.direction
  }
  return out
}

export function buildSaveRequest(
  items: ItemDef[],
  formState: Record<string, unknown>,
  mode: 'create' | 'edit'
): Record<string, unknown> {
  const out: Record<string, unknown> = {}
  for (const item of items) {
    const pattern = mode === 'create' ? item.initPattern : item.editPattern
    // doNot: field must not be sent (not allowed in this mode)
    if (pattern === 'doNot') continue
    // todo: backend initializes this field; frontend hides and does not transmit (Gap4)
    if (pattern === 'todo') continue
    // userInput: normal case, transmit the value
    out[item.name] = formState[item.name] ?? null
  }
  return out
}
