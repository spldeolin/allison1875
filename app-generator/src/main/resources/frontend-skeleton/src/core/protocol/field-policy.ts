// protocol/field-policy.ts
// Centralized rules for field visibility, editability, and filter pattern lookup.
// FilterPattern mapping mirrors backend form-generator's ItemService#getFilterPatterns.

import type { ItemDef, FilterPattern } from '@/schema/types'

// Mirror of backend ItemService#getFilterPatterns fixed mapping.
// When backend adds a new ItemType, add a row here.
const FILTER_PATTERNS_BY_TYPE: Record<ItemDef['type'], FilterPattern[]> = {
  text:        ['in', 'like'],
  number:      ['in', 'ge', 'gt', 'le', 'lt'],
  time:        ['in', 'dateRange', 'dateTimeRange'],
  select:      ['in'],
  multiSelect: ['in'],
  onOff:       ['in'],
  secret:      [],  // secret fields are not searchable
  file:        [],  // file fields are not searchable
}

export function getFilterPatternsByItemType(type: ItemDef['type']): FilterPattern[] {
  return FILTER_PATTERNS_BY_TYPE[type]
}

export type FieldMode =
  | 'search'
  | 'table'
  | 'edit-create'
  | 'edit-update'
  | 'detail'

/**
 * Returns true when the field should appear in the given mode.
 * canInputOnInit/canInputOnEdit default to true when absent (backwards-compatible).
 */
export function isVisible(item: ItemDef, mode: FieldMode): boolean {
  const canInputOnInit = item.canInputOnInit !== false  // default true
  const canInputOnEdit = item.canInputOnEdit !== false  // default true

  switch (mode) {
    case 'search':
      // secret has empty FilterPatterns → hidden from search automatically
      return getFilterPatternsByItemType(item.type).length > 0

    case 'table':
      // Gap3: secret fields are absent from backend list response; don't show column
      return item.type !== 'secret'

    case 'edit-create':
      // canInputOnInit=false: backend handles this field; hide from create form
      return canInputOnInit

    case 'edit-update':
      // secret 字段编辑不可修改时直接隐藏（不做只读展示，且详情接口本就不返回明文）
      if (item.type === 'secret' && !canInputOnEdit) return false
      // canInputOnEdit=false: field is read-only in edit mode — still show as display-only
      // (frontend shows it but doesn't submit it; backend ignores the field if sent)
      // canInputOnInit=false AND canInputOnEdit=false: completely backend-managed, hide
      if (!canInputOnInit && !canInputOnEdit) return false
      return true

    case 'detail':
      return true
  }
}

/**
 * Returns true when the field should be rendered as editable input (vs. read-only display)
 * in a modal that is in create or edit mode.
 * canInputOnInit/canInputOnEdit default to true when absent.
 */
export function isEditable(item: ItemDef, mode: 'edit-create' | 'edit-update'): boolean {
  const canInputOnInit = item.canInputOnInit !== false
  const canInputOnEdit = item.canInputOnEdit !== false
  if (mode === 'edit-create') return canInputOnInit
  return canInputOnEdit
}

/**
 * 编辑已有记录时，secret 字段允许"不修改"（提交 null 保留原值），因此不参与必填校验。
 * 作为 rules 生成与 :required 红星的单一判定来源，避免两处内联表达式漂移。
 */
export function isSecretExemptFromRequired(item: ItemDef, mode: 'edit-create' | 'edit-update'): boolean {
  return item.type === 'secret' && mode === 'edit-update'
}
