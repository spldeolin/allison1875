import { describe, it, expect } from 'vitest'
import { filledHintOf } from '@/core/fields/file-category'

describe('filledHintOf', () => {
  it('returns category title only when no size limit', () => {
    expect(filledHintOf('document', undefined)).toBe('文档')
  })

  it('appends size limit when provided', () => {
    expect(filledHintOf('document', 10)).toBe('文档 · ≤ 10MB')
  })

  it('defaults to general category', () => {
    expect(filledHintOf(undefined, 5)).toBe('普通文件 · ≤ 5MB')
  })

  it('falls back to general title for unknown category', () => {
    expect(filledHintOf('unknown', undefined)).toBe('普通文件')
  })
})
