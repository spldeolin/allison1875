import { describe, it, expect } from 'vitest'
import type { FormDef, TextItemDef, TimeItemDef } from '@/schema/types'

describe('FormDef types', () => {
  it('should allow a valid FormDef with all item types', () => {
    const form: FormDef = {
      name: 'StudentBasicInfo',
      title: '学生基本信息',
      group: '学生管理',
      icon: 'PersonOutline',
      order: 1,
      items: [
        { type: 'text', name: 'studentName', title: '学生姓名', isNonVoid: true, initPattern: 'userInput', editPattern: 'userInput', maxLength: 50 },
        { type: 'number', name: 'score', title: '分数', isNonVoid: false, initPattern: 'userInput', editPattern: 'userInput', canBeDecimal: true },
        { type: 'select', name: 'grade', title: '年级', isNonVoid: true, initPattern: 'userInput', editPattern: 'userInput', options: [{ code: 'g1', title: '一年级' }] },
        { type: 'multiSelect', name: 'tags', title: '标签', isNonVoid: false, initPattern: 'userInput', editPattern: 'userInput', options: [{ code: 't1', title: '标签1' }] },
        { type: 'time', name: 'enrollDate', title: '入学日期', isNonVoid: true, initPattern: 'userInput', editPattern: 'userInput', format: 'date' },
        { type: 'onOff', name: 'isActive', title: '是否激活', isNonVoid: true, initPattern: 'userInput', editPattern: 'userInput' },
        { type: 'secret', name: 'idCard', title: '身份证号', isNonVoid: true, initPattern: 'userInput', editPattern: 'doNot' },
      ],
      indices: [{ itemNames: ['studentName'], isUnique: true }]
    }
    expect(form.name).toBe('StudentBasicInfo')
    expect(form.items).toHaveLength(7)
    expect((form.items[0] as TextItemDef).maxLength).toBe(50)
    expect((form.items[4] as TimeItemDef).format).toBe('date')
  })

  it('should allow FormDef without optional fields', () => {
    const form: FormDef = {
      name: 'Minimal',
      title: '最小表单',
      items: [
        { type: 'text', name: 'field1', title: '字段1', isNonVoid: true, initPattern: 'userInput', editPattern: 'userInput' }
      ]
    }
    expect(form.desc).toBeUndefined()
    expect(form.group).toBeUndefined()
    expect(form.indices).toBeUndefined()
  })
})
