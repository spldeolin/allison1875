// protocol/query-sync.ts
// Bidirectional serialization between search form state and URL query params.
// Field name and time-range suffix rules sourced from request-builder.ts and SearchForm.vue.

import type { ItemDef } from '@/schema/types'

type Query = Record<string, string>

/**
 * Serialize search form state into a flat URL query object.
 * - text/select → string (omitted when null/empty/blank)
 * - number → string (omitted when null/empty/blank/NaN)
 * - multiSelect → comma-joined (omitted when empty)
 * - onOff → 'true'/'false' (omitted when null)
 * - time → split into {name}Start / {name}End (omitted when missing)
 * - createdAtStart/createdAtEnd range → kept as-is when both present
 */
export function serializeSearchToQuery(
  items: ItemDef[],
  formState: Record<string, unknown>
): Query {
  const out: Query = {}
  for (const item of items) {
    const v = formState[item.name]
    switch (item.type) {
      case 'text':
      case 'select':
        if (v != null && v !== '') out[item.name] = String(v)
        break
      case 'number':
        // NaN guard: don't write the literal "NaN" into the URL
        if (v != null && v !== '' && !Number.isNaN(v as number)) out[item.name] = String(v)
        break
      case 'multiSelect':
        if (Array.isArray(v) && v.length > 0) out[item.name] = v.join(',')
        break
      case 'onOff':
        if (v != null) out[item.name] = String(v)
        break
      case 'time': {
        if (Array.isArray(v) && v.length === 2) {
          const [start, end] = v as [unknown, unknown]
          if (start != null && end != null && start !== '' && end !== '') {
            out[`${item.name}Start`] = String(start)
            out[`${item.name}End`] = String(end)
          }
        }
        break
      }
      default:
        break
    }
  }
  // Created-at range (SearchForm writes createdAtStart/createdAtEnd strings + _createdAtRange timestamps)
  const cs = formState.createdAtStart
  const ce = formState.createdAtEnd
  if (cs != null && cs !== '' && ce != null && ce !== '') {
    out.createdAtStart = String(cs)
    out.createdAtEnd = String(ce)
  }
  return out
}

/**
 * Parse a URL query object back into search form state.
 * Inverse of serializeSearchToQuery. createdAt range also rebuilds _createdAtRange
 * timestamps (NDatePicker uses timestamps for display).
 */
export function parseQueryToSearch(
  items: ItemDef[],
  query: Record<string, unknown>
): Record<string, unknown> {
  const out: Record<string, unknown> = {}
  for (const item of items) {
    switch (item.type) {
      case 'text':
      case 'select': {
        const raw = query[item.name]
        out[item.name] = raw != null && raw !== '' ? String(raw) : null
        break
      }
      case 'number': {
        const raw = query[item.name]
        if (raw != null && raw !== '') {
          const n = Number(raw)
          out[item.name] = Number.isNaN(n) ? null : n
        } else {
          out[item.name] = null
        }
        break
      }
      case 'multiSelect': {
        const raw = query[item.name]
        out[item.name] = raw != null && raw !== '' ? String(raw).split(',') : null
        break
      }
      case 'onOff': {
        const raw = query[item.name]
        out[item.name] = raw === 'true' ? true : raw === 'false' ? false : null
        break
      }
      case 'time': {
        const start = query[`${item.name}Start`]
        const end = query[`${item.name}End`]
        if ((start != null && start !== '') || (end != null && end !== '')) {
          out[item.name] = [start != null && start !== '' ? String(start) : '', end != null && end !== '' ? String(end) : '']
        } else {
          out[item.name] = null
        }
        break
      }
      default:
        break
    }
  }
  // Created-at range
  const cs = query.createdAtStart
  const ce = query.createdAtEnd
  if (cs != null && cs !== '' && ce != null && ce !== '') {
    const startStr = String(cs)
    const endStr = String(ce)
    out.createdAtStart = startStr
    out.createdAtEnd = endStr
    const startTs = Date.parse(startStr)
    const endTs = Date.parse(endStr)
    out._createdAtRange = (!Number.isNaN(startTs) || !Number.isNaN(endTs))
      ? [Number.isNaN(startTs) ? null : startTs, Number.isNaN(endTs) ? null : endTs]
      : null
  }
  return out
}
