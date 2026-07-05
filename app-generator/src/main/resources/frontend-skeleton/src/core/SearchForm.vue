<script setup lang="ts">
import { computed, ref, onMounted, onUpdated, nextTick } from 'vue'
import { NForm, NFormItem, NButton, NSpace, NDatePicker } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'
import { isVisible } from './protocol/field-policy'

const props = defineProps<{
  items: ItemDef[]
  modelValue: Record<string, any>
}>()

const emit = defineEmits<{
  'update:modelValue': [val: Record<string, any>]
  search: []
  reset: []
}>()

const searchableItems = computed(() =>
  props.items.filter(item => isVisible(item, 'search'))
)

const scrollRef = ref<HTMLElement>()
const expanded = ref(false)
const overflowing = ref(false)

function checkOverflow() {
  if (scrollRef.value) {
    overflowing.value = scrollRef.value.scrollHeight > 180
  }
}

onMounted(() => nextTick(checkOverflow))
onUpdated(() => nextTick(checkOverflow))

function updateField(name: string, value: any) {
  emit('update:modelValue', { ...props.modelValue, [name]: value })
}

function updateCreatedAtRange(val: [number, number] | null) {
  if (val) {
    const pad = (n: number) => String(n).padStart(2, '0')
    const fmt = (ts: number) => {
      const d = new Date(ts)
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    }
    emit('update:modelValue', {
      ...props.modelValue,
      createdAtStart: fmt(val[0]),
      createdAtEnd: fmt(val[1]),
      _createdAtRange: val,
    })
  } else {
    const next = { ...props.modelValue }
    delete next.createdAtStart
    delete next.createdAtEnd
    delete next._createdAtRange
    emit('update:modelValue', next)
  }
}

function handleReset() {
  const empty: Record<string, any> = {}
  for (const item of searchableItems.value) {
    empty[item.name] = null
  }
  emit('update:modelValue', empty)
  emit('reset')
}
</script>

<template>
  <div class="search-form-wrapper">
    <div ref="scrollRef" class="search-form-scroll" :class="{ 'is-expanded': expanded }">
      <NForm inline label-placement="left" style="flex-wrap: wrap; gap: 0 16px;">
        <NFormItem v-for="item in searchableItems" :key="item.name" :label="item.title">
          <div
            :class="{ 'search-enter-wrap': ['text', 'number', 'time'].includes(item.type) }"
            @keyup.enter="['text', 'number', 'time'].includes(item.type) ? emit('search') : undefined"
          >
            <FieldRenderer
              :item="item"
              mode="search"
              :value="modelValue[item.name] ?? null"
              @update:value="updateField(item.name, $event)"
            />
          </div>
        </NFormItem>
        <NFormItem label="创建时间">
          <NDatePicker
            type="datetimerange"
            :value="modelValue._createdAtRange ?? null"
            clearable
            @update:value="updateCreatedAtRange($event as [number, number] | null)"
          />
        </NFormItem>
      </NForm>
    </div>
    <div class="search-form-actions">
      <NButton v-if="overflowing" text type="primary" size="small" @click="expanded = !expanded">
        {{ expanded ? '收起' : '展开筛选' }}
      </NButton>
      <NSpace>
        <NButton type="primary" @click="emit('search')">查询</NButton>
        <NButton @click="handleReset">重置</NButton>
      </NSpace>
    </div>
  </div>
</template>

<style scoped>
.search-form-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-form-scroll {
  max-height: 180px;
  overflow-y: auto;
  transition: max-height 0.25s ease;
}

.search-form-scroll.is-expanded {
  max-height: 420px;
}

.search-form-actions {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  border-top: 1px solid #f1f5f9;
  padding-top: 12px;
}

.search-enter-wrap {
  display: contents;
}
</style>
