<script setup lang="ts">
import { computed, ref } from 'vue'
import { NModal, NCard, NForm, NFormItem, NButton, NSpace, type FormInst, type FormRules } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'
import { isVisible } from './protocol/field-policy'

const props = defineProps<{
  visible: boolean
  mode: 'create' | 'edit'
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
    if (item.isNonVoid) {
      r[item.name] = [{ required: true, message: `请输入${item.title}`, trigger: 'blur' }]
    }
  }
  return r
})

const title = computed(() => props.mode === 'create' ? '新建' : '编辑')

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
        <NFormItem v-for="item in visibleItems" :key="item.name" :label="item.title" :path="item.name">
          <FieldRenderer
            :item="item"
            mode="edit"
            :value="modelValue[item.name] ?? null"
            @update:value="updateField(item.name, $event)"
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
