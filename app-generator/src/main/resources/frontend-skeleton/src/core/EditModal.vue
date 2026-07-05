<script setup lang="ts">
import { computed, ref, reactive, watch, nextTick } from 'vue'
import { NModal, NCard, NForm, NFormItem, NButton, NSpace, type FormInst, type FormRules } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'
import { isVisible, isEditable, isSecretExemptFromRequired } from './protocol/field-policy'

const props = defineProps<{
  visible: boolean
  mode: 'create' | 'edit'
  formTitle: string
  items: ItemDef[]
  modelValue: Record<string, any>
  loading: boolean
}>()

const emit = defineEmits<{
  'update:visible': [val: boolean]
  'update:modelValue': [val: Record<string, any>]
  submit: []
}>()

const formRef = ref<FormInst | null>(null)

const itemRefs = new Map<string, HTMLElement>()

function setItemRef(name: string) {
  return (el: Element | { $el?: HTMLElement } | null) => {
    if (el) {
      const dom = (el as { $el?: HTMLElement }).$el ?? (el as HTMLElement)
      itemRefs.set(name, dom as HTMLElement)
    } else {
      itemRefs.delete(name)
    }
  }
}

// Local reactive model for NForm — mutated in place so NFormItem sees updates
// immediately when it re-validates (avoids prop round-trip timing issue)
const localModel = reactive<Record<string, any>>({})

// Sync from parent when modelValue reference changes (modal open / external reset)
watch(() => props.modelValue, (incoming) => {
  for (const key of Object.keys(localModel)) {
    delete localModel[key]
  }
  Object.assign(localModel, incoming)
}, { immediate: true })

const editMode = computed<'edit-create' | 'edit-update'>(() =>
  props.mode === 'create' ? 'edit-create' : 'edit-update'
)

const visibleItems = computed(() =>
  props.items.filter(item => isVisible(item, editMode.value))
)

const rules = computed<FormRules>(() => {
  const r: FormRules = {}
  for (const item of visibleItems.value) {
    // 编辑已有记录时，secret 允许"不修改"（提交 null），因此不生成必填规则
    if (isSecretExemptFromRequired(item, editMode.value)) continue
    // Only add validation rules for editable fields
    if (item.isNonVoid && isEditable(item, editMode.value)) {
      // onOff is a boolean — false is a valid non-void value, no required rule needed
      if (item.type === 'onOff') continue
      // file holds a FileValue object, not a string — validate non-void without a string type
      if (item.type === 'file') {
        r[item.name] = [{
          required: true,
          message: `请上传${item.title}`,
          trigger: ['blur', 'change']
        }]
        continue
      }
      const isNumber = item.type === 'number'
      r[item.name] = [{
        required: true,
        type: isNumber ? 'number' : 'string',
        message: `请输入${item.title}`,
        trigger: ['blur', 'change']
      }]
    }
  }
  return r
})

const title = computed(() => props.mode === 'create' ? `创建${props.formTitle}` : `编辑${props.formTitle}`)

function updateField(name: string, value: any) {
  localModel[name] = value
  emit('update:modelValue', { ...localModel })
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch (errors) {
    await nextTick()
    focusFirstError(errors)
    return
  }
  emit('submit')
}

function focusFirstError(errors: unknown) {
  const errObj = (errors && typeof errors === 'object' ? errors : {}) as Record<string, unknown>
  for (const item of visibleItems.value) {
    if (!isEditable(item, editMode.value)) continue
    if (!errObj[item.name]) continue
    const dom = itemRefs.get(item.name)
    if (!dom) continue
    const focusable = dom.querySelector('input, [tabindex]:not([tabindex="-1"])') as HTMLElement | null
    focusable?.focus()
    return
  }
  // 兜底：未匹配到结构化 errors 时，按 DOM 错误类定位
  for (const item of visibleItems.value) {
    if (!isEditable(item, editMode.value)) continue
    const dom = itemRefs.get(item.name)
    if (!dom) continue
    if (dom.classList.contains('n-form-item--error') || dom.querySelector('.n-form-item--error')) {
      const focusable = dom.querySelector('input, [tabindex]:not([tabindex="-1"])') as HTMLElement | null
      focusable?.focus()
      return
    }
  }
}

function handleClose() {
  emit('update:visible', false)
}
</script>

<template>
  <NModal :show="visible" @update:show="emit('update:visible', $event)">
    <NCard :title="title" class="edit-modal-card" :bordered="false">
      <div class="edit-modal-body">
        <NForm ref="formRef" :model="localModel" :rules="rules" label-placement="left" label-width="130px">
          <NFormItem
            v-for="item in visibleItems"
            :key="item.name"
            :ref="setItemRef(item.name)"
            :label="item.title"
            :path="isEditable(item, editMode) ? item.name : undefined"
            :required="item.isNonVoid && isEditable(item, editMode) && !isSecretExemptFromRequired(item, editMode)"
          >
            <FieldRenderer
              :item="item"
              :mode="isEditable(item, editMode) ? 'edit' : 'display'"
              :edit-mode="editMode"
              :value="localModel[item.name] ?? null"
              :readonly="!isEditable(item, editMode)"
              @update:value="isEditable(item, editMode) ? updateField(item.name, $event) : undefined"
            />
          </NFormItem>
        </NForm>
      </div>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="handleClose">取消</NButton>
          <NButton type="primary" :loading="loading" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NCard>
  </NModal>
</template>

<style scoped>
.edit-modal-card {
  width: 600px;
  border-radius: 16px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.edit-modal-body {
  overflow-y: auto;
  max-height: calc(80vh - 160px);
  padding-right: 8px;
}
</style>
