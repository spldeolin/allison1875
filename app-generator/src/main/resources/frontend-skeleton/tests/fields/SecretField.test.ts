// @vitest-environment happy-dom
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SecretField from '@/core/fields/SecretField.vue'
import type { SecretItemDef } from '@/schema/types'

function secretItem(isNonVoid = true): SecretItemDef {
  return {
    name: 'password',
    title: '密码',
    type: 'secret',
    isNonVoid,
  } as unknown as SecretItemDef
}

/**
 * edit-update 下 SecretField 的三态提交协议：
 *   - 默认态不 emit（值保持父级传入的 null）
 *   - 点击 edit icon 进入编辑 → emit ''（清空意图）
 *   - 输入新值 → emit 该值
 *   - 点击 close icon 放弃编辑 → emit null（不修改）
 */
describe('SecretField edit-update emit semantics', () => {
  it('renders readonly dots in default state without emitting', () => {
    const wrapper = mount(SecretField, {
      props: {
        item: secretItem(),
        mode: 'edit',
        editMode: 'edit-update',
        value: null,
      },
    })
    expect(wrapper.emitted('update:value')).toBeUndefined()
  })

  it('emits "" (clear) when edit trigger is clicked, even with empty input', async () => {
    const wrapper = mount(SecretField, {
      props: {
        item: secretItem(),
        mode: 'edit',
        editMode: 'edit-update',
        value: null,
      },
    })
    await wrapper.find('.secret-edit-trigger').trigger('click')
    const emitted = wrapper.emitted('update:value')
    expect(emitted).toBeTruthy()
    expect(emitted![emitted!.length - 1]).toEqual([''])
  })

  it('emits the typed value when user inputs', async () => {
    const wrapper = mount(SecretField, {
      props: {
        item: secretItem(),
        mode: 'edit',
        editMode: 'edit-update',
        value: null,
      },
    })
    await wrapper.find('.secret-edit-trigger').trigger('click')
    // 进入编辑态后，向 NInput 内部 input 派发 input 事件触发 update:value
    const input = wrapper.find('input')
    await input.setValue('new-secret')
    const emitted = wrapper.emitted('update:value')
    expect(emitted).toBeTruthy()
    expect(emitted![emitted!.length - 1]).toEqual(['new-secret'])
  })

  it('emits null (unchanged) when close icon is clicked', async () => {
    const wrapper = mount(SecretField, {
      props: {
        item: secretItem(),
        mode: 'edit',
        editMode: 'edit-update',
        value: null,
      },
    })
    await wrapper.find('.secret-edit-trigger').trigger('click')
    // 进入编辑后切换为 close icon
    await wrapper.find('.secret-edit-cancel').trigger('click')
    const emitted = wrapper.emitted('update:value')
    expect(emitted).toBeTruthy()
    expect(emitted![emitted!.length - 1]).toEqual([null])
  })
})
