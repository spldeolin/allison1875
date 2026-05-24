<script setup lang="ts">
import { NInput } from 'naive-ui'
import type { SecretItemDef } from '@/schema/types'

defineProps<{
  item: SecretItemDef
  mode: 'search' | 'edit' | 'display'
  value: string | null
}>()

const emit = defineEmits<{
  'update:value': [val: string | null]
}>()
</script>

<template>
  <template v-if="mode === 'display'">
    <span>***</span>
  </template>
  <template v-else-if="mode === 'edit'">
    <NInput
      type="password"
      :value="value"
      show-password-on="click"
      clearable
      placeholder="请输入"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
