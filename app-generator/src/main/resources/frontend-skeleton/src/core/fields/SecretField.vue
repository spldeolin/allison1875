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
// 标记当前 emit 由本组件自身触发。props.value 与父级 v-model 双向绑定，
// 自身 emit 会经父级回流为新的 props.value 触发 watch；此时不应重置 editing，
// 仅弹框重新打开 / 外部真正重置时才重置。
let internal = false

// 弹框打开 / 外部重置时回到默认未修改态。自身 emit 引起的回流被 internal 跳过。
watch(() => props.value, () => {
  if (internal) {
    internal = false
    return
  }
  editing.value = false
  draft.value = ''
})

// 点击 edit icon：进入可输入态。draft 起始为空（露出 placeholder）。
// 进入编辑态即表达「要改」的意图，空 draft = 清空（emit ''），
// 区别于 close icon 的「放弃编辑、不修改」（emit null）。
function startEdit() {
  editing.value = true
  draft.value = ''
  internal = true
  emit('update:value', '')
}

// 点击 close icon：退出可输入态，回到未修改（emit null）。
function cancelEdit() {
  editing.value = false
  draft.value = ''
  internal = true
  emit('update:value', null)
}

// 输入态下用户键入或清空。真实新值 → emit 新值；clearable 清空 → emit ''。
function onInput(v: string | null) {
  draft.value = v ?? ''
  internal = true
  emit('update:value', v ?? '')
}
</script>

<template>
  <template v-if="mode === 'display'">
    <span>***</span>
  </template>
  <template v-else-if="mode === 'edit' && isEditUpdate">
    <NInput
      :class="!editing ? 'secret-readonly-input' : undefined"
      :type="editing ? 'password' : 'text'"
      :value="editing ? draft : ''"
      :readonly="!editing"
      :clearable="editing && item.isNonVoid === false"
      :show-password-on="editing && draft ? 'click' : undefined"
      :placeholder="editing ? '请输入' : '••••••'"
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

<style scoped>
/* edit-update 默认态：未进入编辑的 readonly 输入框，强化「不可编辑」视觉。
   关键区分点：未编辑态不显示圆点（易与编辑态 password 掩码混淆），
   改用 placeholder「未修改…」+ 灰底 + 虚线边框 + not-allowed 光标，
   与进入编辑后的活跃 password 输入框拉开明显差距。
   naive-ui 把传入的 class 与 n-input--readonly 落在同一根元素上，
   故用「同元素」选择器 .secret-readonly-input.n-input--readonly 命中。 */
.secret-readonly-input:deep(.n-input.n-input--readonly) {
  background: #f5f7fa;
  border: 1px dashed #cbd5e1;
  border-radius: 4px;
}
.secret-readonly-input:deep(.n-input.n-input--readonly:hover) {
  border-color: #94a3b8;
}
.secret-readonly-input:deep(.n-input.n-input--readonly .n-input__input-el),
.secret-readonly-input:deep(.n-input.n-input--readonly .n-input__textarea-el) {
  cursor: not-allowed;
}
.secret-readonly-input:deep(.n-input.n-input--readonly .n-input__placeholder) {
  color: #94a3b8;
  font-style: italic;
}
/* edit icon 保持与其他输入框图标一致的普通色，不加蓝色/放大等强调 */
.secret-readonly-input :deep(.secret-edit-trigger) {
  cursor: pointer;
}
</style>
