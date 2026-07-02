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
</script>

<template>
  <component
    :is="currentComponent"
    :item="(item as any)"
    :mode="mode"
    :value="value"
    @update:value="emit('update:value', $event)"
  />
</template>
