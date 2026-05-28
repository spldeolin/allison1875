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

export function isVisible(item: ItemDef, mode: FieldMode): boolean {
  switch (mode) {
    case 'search':
      // secret has empty FilterPatterns → hidden from search automatically
      return getFilterPatternsByItemType(item.type).length > 0

    case 'table':
      // Gap3: secret fields are absent from backend list response; don't show column
      return item.type !== 'secret'

    case 'edit-create':
      // doNot: field is not allowed on create
      if (item.initPattern === 'doNot') return false
      // todo: backend initializes this field; frontend hides it completely (Gap4)
      if (item.initPattern === 'todo') return false
      return true

    case 'edit-update':
      // doNot: field cannot be edited
      if (item.editPattern === 'doNot') return false
      // todo on editPattern is not expected by the DSL (todo only appears on initPattern),
      // but guard against it defensively
      if (item.editPattern === 'todo') return false
      return true

    case 'detail':
      return true
  }
}

// isReadonly: since todo fields are hidden (isVisible returns false for them),
// no visible field is readonly. This function is a reserved hook for future use.
export function isReadonly(_item: ItemDef, _mode: 'edit-create' | 'edit-update'): boolean {
  return false
}
