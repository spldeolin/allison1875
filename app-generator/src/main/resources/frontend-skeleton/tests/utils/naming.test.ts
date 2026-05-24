import { describe, it, expect } from 'vitest'
import { upperCamelToKebab, deriveApiBasePath } from '@/utils/naming'

describe('upperCamelToKebab', () => {
  it('converts UpperCamel to kebab-case', () => {
    expect(upperCamelToKebab('StudentBasicInfo')).toBe('student-basic-info')
    expect(upperCamelToKebab('User')).toBe('user')
    expect(upperCamelToKebab('APIKey')).toBe('api-key')
    expect(upperCamelToKebab('DormitoryApplication')).toBe('dormitory-application')
  })
})

describe('deriveApiBasePath', () => {
  it('derives API base path from FormDef name', () => {
    expect(deriveApiBasePath('StudentBasicInfo')).toBe('/api/v1/studentBasicInfo')
    expect(deriveApiBasePath('User')).toBe('/api/v1/user')
  })
})
