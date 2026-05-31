<script setup lang="ts">
import { computed, ref } from 'vue'
import { NModal, NCard, NForm, NFormItem, NButton, NSpace, type FormInst, type FormRules } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'
import { isVisible, isEditable } from './protocol/field-policy'

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

const editMode = computed<'edit-create' | 'edit-update'>(() =>
  props.mode === 'create' ? 'edit-create' : 'edit-update'
)

const visibleItems = computed(() =>
  props.items.filter(item => isVisible(item, editMode.value))
)

const rules = computed<FormRules>(() => {
  const r: FormRules = {}
  for (const item of visibleItems.value) {
    // Only add validation rules for editable fields
    if (item.isNonVoid && isEditable(item, editMode.value)) {
      const isNumber = item.type === 'number'
      r[item.name] = [{
        required: true,
        type: isNumber ? 'number' : 'string',
        message: `请输入${item.title}`,
        trigger: isNumber ? ['blur', 'change'] : 'blur'
      }]
    }
  }
  return r
})

const title = computed(() => props.mode === 'create' ? `新建${props.formTitle}` : `编辑${props.formTitle}`)

function updateField(name: string, value: any) {
  emit('update:modelValue', { ...props.modelValue, [name]: value })
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  emit('submit')
}

function handleClose() {
  emit('update:visible', false)
}
</script>

<template>
  <NModal :show="visible" @update:show="emit('update:visible', $event)">
    <NCard :title="title" style="width: 600px; border-radius: 16px;" :bordered="false" closable @close="handleClose">
      <NForm ref="formRef" :model="modelValue" :rules="rules" label-placement="left" label-width="100px">
        <NFormItem v-for="item in visibleItems" :key="item.name" :label="item.title" :path="isEditable(item, editMode) ? item.name : undefined">
          <FieldRenderer
            :item="item"
            :mode="isEditable(item, editMode) ? 'edit' : 'display'"
            :value="modelValue[item.name] ?? null"
            @update:value="isEditable(item, editMode) ? updateField(item.name, $event) : undefined"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="handleClose">取消</NButton>
          <NButton type="primary" :loading="loading" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NCard>
  </NModal>
</template>
