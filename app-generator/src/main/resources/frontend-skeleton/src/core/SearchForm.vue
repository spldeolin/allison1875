<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
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

const expanded = ref(false)
const needsCollapse = ref(false)
const formItemsRef = ref<HTMLElement | null>(null)
const collapsedHeight = ref<number>(0)

function measureRows() {
  const el = formItemsRef.value
  if (!el) return
  const prevMaxHeight = el.style.maxHeight
  const prevOverflow = el.style.overflow
  el.style.maxHeight = 'none'
  el.style.overflow = 'visible'

  const children = el.querySelectorAll(':scope > .n-form-item')
  if (children.length < 2) {
    el.style.maxHeight = prevMaxHeight
    el.style.overflow = prevOverflow
    needsCollapse.value = false
    return
  }

  const firstTop = (children[0] as HTMLElement).offsetTop
  const rowHeight = (children[0] as HTMLElement).offsetHeight
  let rowCount = 1
  let secondRowTop = firstTop

  for (let i = 1; i < children.length; i++) {
    const top = (children[i] as HTMLElement).offsetTop
    if (top > firstTop && secondRowTop === firstTop) {
      secondRowTop = top
      rowCount = 2
    } else if (top > secondRowTop && secondRowTop > firstTop) {
      rowCount = 3
      break
    }
  }

  needsCollapse.value = rowCount >= 3
  collapsedHeight.value = secondRowTop - firstTop + rowHeight + 8

  el.style.maxHeight = prevMaxHeight
  el.style.overflow = prevOverflow
}

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  nextTick(() => {
    measureRows()
    if (formItemsRef.value) {
      resizeObserver = new ResizeObserver(() => measureRows())
      resizeObserver.observe(formItemsRef.value)
    }
  })
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
})

watch(() => props.items, () => nextTick(measureRows), { deep: true })

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
    <div
      ref="formItemsRef"
      class="search-form-content"
      :class="{ collapsed: needsCollapse && !expanded }"
      :style="needsCollapse && !expanded ? { maxHeight: collapsedHeight + 'px' } : {}"
    >
      <NForm inline label-placement="left" style="flex-wrap: wrap; gap: 0 16px;">
        <NFormItem v-for="item in searchableItems" :key="item.name" :label="item.title">
          <FieldRenderer
            :item="item"
            mode="search"
            :value="modelValue[item.name] ?? null"
            @update:value="updateField(item.name, $event)"
          />
        </NFormItem>
        <NFormItem label="创建时间">
          <NDatePicker
            type="datetimerange"
            :value="modelValue._createdAtRange ?? null"
            clearable
            @update:value="updateCreatedAtRange($event as [number, number] | null)"
          />
        </NFormItem>
        <NFormItem>
          <NSpace>
            <NButton type="primary" @click="emit('search')">查询</NButton>
            <NButton @click="handleReset">重置</NButton>
          </NSpace>
        </NFormItem>
      </NForm>
    </div>
    <div v-if="needsCollapse" class="search-expand-bar">
      <NButton size="small" @click="expanded = !expanded">
        {{ expanded ? '收起' : '展开' }}
      </NButton>
    </div>
  </div>
</template>

<style scoped>
.search-form-wrapper {
  display: flex;
  flex-direction: column;
}

.search-form-content {
  transition: max-height 0.25s ease;
}

.search-form-content.collapsed {
  overflow: hidden;
}

.search-expand-bar {
  display: flex;
  justify-content: center;
  margin-top: 4px;
}
</style>
