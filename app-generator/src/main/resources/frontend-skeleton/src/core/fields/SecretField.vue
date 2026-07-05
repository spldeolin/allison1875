<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { NInput, NIcon } from 'naive-ui'
import { CreateOutline, CloseOutline } from '@vicons/ionicons5'
import type { SecretItemDef } from '@/schema/types'

const props = defineProps<{
  item: SecretItemDef
  mode: 'search' | 'edit' | 'display'
  value: string | null
  /** 弹框场景：'edit-create'（创建）或 'edit-update'（编辑已有记录）。 */
  editMode?: 'edit-create' | 'edit-update'
}>()

const emit = defineEmits<{
  'update:value': [val: string | null]
}>()

// 编辑已有记录时的特殊态：详情接口不返回明文，允许"不修改即保留"。
const isEditUpdate = computed(() => props.mode === 'edit' && props.editMode === 'edit-update')

// edit-update 下是否已进入可输入态。
// false: readonly + 黑点 + edit icon（默认，= 未修改）。
// true:  可输入 password input + close icon。
const editing = ref(false)
// 进入 editing 后用户输入的本地值。未进入时无意义。
const draft = ref<string>('')

// 弹框打开 / 外部重置时回到默认未修改态。
watch(() => props.value, () => {
  editing.value = false
  draft.value = ''
})

// 点击 edit icon：进入可输入态。draft 起始为空（露出 placeholder），
// 语义仍为「不修改」（emit null）。
function startEdit() {
  editing.value = true
  draft.value = ''
  emit('update:value', null)
}

// 点击 close icon：退出可输入态，回到未修改（emit null）。
function cancelEdit() {
  editing.value = false
  draft.value = ''
  emit('update:value', null)
}

// 输入态下用户键入或清空。真实新值 → emit 新值；clearable 清空 → emit ''。
function onInput(v: string | null) {
  draft.value = v ?? ''
  emit('update:value', v ?? '')
}
</script>

<template>
  <template v-if="mode === 'display'">
    <span>***</span>
  </template>
  <template v-else-if="mode === 'edit' && isEditUpdate">
    <NInput
      :type="editing ? 'password' : 'text'"
      :value="editing ? draft : '••••••'"
      :readonly="!editing"
      :clearable="editing && item.isNonVoid === false"
      :show-password-on="editing && draft ? 'click' : undefined"
      :placeholder="editing ? '请输入' : ''"
      @update:value="onInput"
    >
      <template #suffix>
        <NIcon
          v-if="!editing"
          class="secret-edit-trigger"
          :component="CreateOutline"
          @click="startEdit"
        />
        <NIcon
          v-else
          class="secret-edit-cancel"
          :component="CloseOutline"
          @click="cancelEdit"
        />
      </template>
    </NInput>
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
