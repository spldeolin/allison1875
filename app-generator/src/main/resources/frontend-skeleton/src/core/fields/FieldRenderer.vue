<script setup lang="ts">
import { computed } from 'vue'
import type { Component } from 'vue'
import type { ItemDef } from '@/schema/types'
import TextField from './TextField.vue'
import NumberField from './NumberField.vue'
import SelectField from './SelectField.vue'
import MultiSelectField from './MultiSelectField.vue'
import TimeField from './TimeField.vue'
import OnOffField from './OnOffField.vue'
import SecretField from './SecretField.vue'
import FileField from './FileField.vue'

const props = defineProps<{
  item: ItemDef
  mode: 'search' | 'edit' | 'display'
  value: any
  /** In an edit modal, a visible-but-not-editable field is read-only (file field renders a distinct style). */
  readonly?: boolean
  /** 弹框场景（create/update），仅 secret 字段消费以区分渲染。 */
  editMode?: 'edit-create' | 'edit-update'
}>()

const emit = defineEmits<{
  'update:value': [val: any]
}>()

const componentMap: Record<string, Component> = {
  text: TextField,
  number: NumberField,
  select: SelectField,
  multiSelect: MultiSelectField,
  time: TimeField,
  onOff: OnOffField,
  secret: SecretField,
  file: FileField
}

const currentComponent = computed(() => componentMap[props.item.type])

// Only FileField consumes `readonly`; binding it to other (fragment-root) field
// components would trigger Vue's extraneous-attribute warning.
const extraProps = computed(() => {
  if (props.item.type === 'file') return { readonly: props.readonly }
  if (props.item.type === 'secret') return { editMode: props.editMode }
  return {}
})
</script>

<template>
  <component
    :is="currentComponent"
    :item="(item as any)"
    :mode="mode"
    :value="value"
    v-bind="extraProps"
    @update:value="emit('update:value', $event)"
  />
</template>
