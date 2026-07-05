import { describe, it, expect } from 'vitest'
import { serializeSearchToQuery, parseQueryToSearch } from '@/core/protocol/query-sync'
import type { ItemDef } from '@/schema/types'

function textItem(name: string): ItemDef {
  return { name, title: name, type: 'text', isNonVoid: true } as unknown as ItemDef
}
function numberItem(name: string): ItemDef {
  return { name, title: name, type: 'number', isNonVoid: true } as unknown as ItemDef
}
function selectItem(name: string): ItemDef {
  return { name, title: name, type: 'select', isNonVoid: true, options: [] } as unknown as ItemDef
}
function multiSelectItem(name: string): ItemDef {
  return { name, title: name, type: 'multiSelect', isNonVoid: true, options: [] } as unknown as ItemDef
}
function timeItem(name: string): ItemDef {
  return { name, title: name, type: 'time', isNonVoid: true, format: 'dateTime' } as unknown as ItemDef
}
function onOffItem(name: string): ItemDef {
  return { name, title: name, type: 'onOff', isNonVoid: true } as unknown as ItemDef
}

const items: ItemDef[] = [
  textItem('name'),
  numberItem('age'),
  selectItem('status'),
  multiSelectItem('tags'),
  onOffItem('enabled'),
  timeItem('createdAt'),
]

describe('serializeSearchToQuery', () => {
  it('serializes text/number/select/multiSelect/onOff/time', () => {
    const formState = {
      name: 'foo',
      age: 30,
      status: 'ACTIVE',
      tags: ['a', 'b'],
      enabled: true,
      createdAt: ['2026-07-05 00:00:00', '2026-07-05 23:59:59'],
    }
    expect(serializeSearchToQuery(items, formState)).toEqual({
      name: 'foo',
      age: '30',
      status: 'ACTIVE',
      tags: 'a,b',
      enabled: 'true',
      createdAtStart: '2026-07-05 00:00:00',
      createdAtEnd: '2026-07-05 23:59:59',
    })
  })

  it('omits null/empty/blank values', () => {
    const formState = { name: '', age: null, status: undefined, tags: [], enabled: null, createdAt: null }
    expect(serializeSearchToQuery(items, formState)).toEqual({})
  })

  it('serializes onOff false as "false" and parses it back', () => {
    expect(serializeSearchToQuery(items, { enabled: false })).toEqual({ enabled: 'false' })
    expect(parseQueryToSearch(items, { enabled: 'false' }).enabled).toBe(false)
  })

  it('serializes createdAtStart/createdAtEnd range', () => {
    const formState = {
      createdAtStart: '2026-07-05 00:00:00',
      createdAtEnd: '2026-07-05 23:59:59',
      _createdAtRange: [1751673600000, 1751762399000],
    }
    expect(serializeSearchToQuery(items, formState)).toEqual({
      createdAtStart: '2026-07-05 00:00:00',
      createdAtEnd: '2026-07-05 23:59:59',
    })
  })

  it('omits createdAt range when partial', () => {
    expect(serializeSearchToQuery(items, { createdAtStart: '2026-07-05 00:00:00' })).toEqual({})
  })
})

describe('parseQueryToSearch', () => {
  it('parses text/number/select/multiSelect/onOff/time', () => {
    const query = {
      name: 'foo',
      age: '30',
      status: 'ACTIVE',
      tags: 'a,b',
      enabled: 'true',
      createdAtStart: '2026-07-05 00:00:00',
      createdAtEnd: '2026-07-05 23:59:59',
    }
    const result = parseQueryToSearch(items, query)
    expect(result).toMatchObject({
      name: 'foo',
      age: 30,
      status: 'ACTIVE',
      tags: ['a', 'b'],
      enabled: true,
      createdAt: ['2026-07-05 00:00:00', '2026-07-05 23:59:59'],
      createdAtStart: '2026-07-05 00:00:00',
      createdAtEnd: '2026-07-05 23:59:59',
    })
    // _createdAtRange rebuilt as [number, number] for NDatePicker (TZ-robust: only check shape + numeric)
    expect(Array.isArray(result._createdAtRange)).toBe(true)
    expect(result._createdAtRange).toHaveLength(2)
    expect(typeof result._createdAtRange[0]).toBe('number')
    expect(typeof result._createdAtRange[1]).toBe('number')
  })

  it('returns null for each searchable field on empty query', () => {
    expect(parseQueryToSearch(items, {})).toEqual({
      name: null,
      age: null,
      status: null,
      tags: null,
      enabled: null,
      createdAt: null,
    })
  })

  it('treats NaN number as null', () => {
    const result = parseQueryToSearch(items, { age: 'abc' })
    expect(result.age).toBe(null)
  })

  it('parses partial time range as tuple with empty slot', () => {
    const result = parseQueryToSearch(items, { createdAtStart: '2026-07-05 00:00:00' })
    expect(result.createdAt).toEqual(['2026-07-05 00:00:00', ''])
  })

  it('ignores query keys not in schema', () => {
    const result = parseQueryToSearch(items, { unknownField: 'x' })
    expect(result.unknownField).toBeUndefined()
    expect(result.name).toBe(null)
  })

  it('serialize and parse are inverses for representative state', () => {
    const formState = {
      name: 'foo',
      age: 30,
      status: 'ACTIVE',
      tags: ['a', 'b'],
      enabled: true,
      createdAt: ['2026-07-05 00:00:00', '2026-07-05 23:59:59'],
    }
    const query = serializeSearchToQuery(items, formState)
    const parsed = parseQueryToSearch(items, query)
    expect(parsed).toMatchObject(formState)
  })
})
