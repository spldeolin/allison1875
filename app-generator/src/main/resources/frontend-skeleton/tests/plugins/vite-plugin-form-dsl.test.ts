import { describe, it, expect } from 'vitest'
import { parseDslFiles } from '../../plugins/vite-plugin-form-dsl'
import path from 'path'

describe('parseDslFiles', () => {
  it('parses YML files from a directory into FormDef array', () => {
    const dslDir = path.resolve(__dirname, '../../src/dsl')
    const result = parseDslFiles(dslDir)

    expect(result).toBeInstanceOf(Array)
    expect(result.length).toBeGreaterThanOrEqual(1)

    const student = result.find(f => f.name === 'StudentBasicInfo')
    expect(student).toBeDefined()
    expect(student!.title).toBe('学生基本信息')
    expect(student!.group).toBe('学生管理')
    expect(student!.items).toHaveLength(6)
    expect(student!.items[0].type).toBe('text')
    expect(student!.items[0].name).toBe('studentName')
  })

  it('returns empty array for non-existent directory', () => {
    const result = parseDslFiles('/non/existent/path')
    expect(result).toEqual([])
  })
})
