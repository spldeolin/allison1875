// protocol/response-parser.ts
// Normalizes backend response DTOs into frontend row objects.
// Sourced from contract.md §4-6.

import type { ItemDef } from '@/schema/types'

type Mode = 'list' | 'detail'

type ParseRule = (item: ItemDef, raw: unknown, mode: Mode) => unknown

// Gap2 decision: multiSelect backend returns List<Enum> (JSON array of code strings).
// Direct passthrough — no string splitting needed.
const itemTypeParseRules: Record<ItemDef['type'], ParseRule> = {
  text:        (_item, raw) => raw,
  number:      (_item, raw) => raw,
  onOff:       (_item, raw) => raw,
  // time fields: backend returns ISO strings ("yyyy-MM-dd", "HH:mm:ss", "yyyy-MM-dd HH:mm:ss")
  // Naive UI date/time pickers can consume these strings directly.
  time:        (_item, raw) => raw,
  // select: single code string, passthrough
  select:      (_item, raw) => raw,
  // multiSelect: backend returns List<Enum> (JSON array). Gap2: direct passthrough.
  multiSelect: (_item, raw) => {
    if (Array.isArray(raw)) return raw
    // Defensive fallback: should not happen per contract, but handle gracefully
    return []
  },
  // secret: completely absent from backend responses (Gap3). This rule is a no-op placeholder.
  secret:      (_item, raw) => raw,
  // file: backend business column is the merged "fileKey/originFileName" string;
  // split on the first '/' to restore the FileValue object held in form state.
  file:        (_item, raw) => {
    if (typeof raw !== 'string' || raw === '') return null
    const bar = raw.indexOf('/')
    if (bar < 0) return null
    return { fileKey: raw.substring(0, bar), originFileName: raw.substring(bar + 1) }
  },
}

function parseRow(
  items: ItemDef[],
  dto: Record<string, unknown>,
  mode: Mode
): Record<string, unknown> {
  const out: Record<string, unknown> = {}
  for (const item of items) {
    const rule = itemTypeParseRules[item.type]
    out[item.name] = rule(item, dto[item.name], mode)
  }
  // Spread dto first to preserve auto-injected fields:
  //   ${formName}Code (business key), createdAt, updatedAt
  // These are not in items[] but CrudPage needs ${formName}Code for delete/getDetail.
  // out (parsed fields) takes precedence over raw dto values.
  return { ...dto, ...out }
}

export function parseListRow(
  items: ItemDef[],
  dto: Record<string, unknown>
): Record<string, unknown> {
  return parseRow(items, dto, 'list')
}

export function parseDetailDto(
  items: ItemDef[],
  dto: Record<string, unknown>
): Record<string, unknown> {
  return parseRow(items, dto, 'detail')
}
