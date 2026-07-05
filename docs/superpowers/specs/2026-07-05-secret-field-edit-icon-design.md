# Secret Field Edit-Icon Interaction

**Date:** 2026-07-05
**Scope:** `app-generator` frontend skeleton — `SecretField.vue` only (the
`edit-update` branch).

## Background

In the edit modal, a secret field with `canInputOnEdit=true` renders on
`edit-update` an `NInput type="password"` plus an external "清空" `NButton`
(wrapped in `NSpace`). The backend detail API does **not** return the secret's
plaintext, so the field's `value` is `null` on `edit-update`; submitting `null`
means "keep the original value".

The current default state is an always-editable password input showing a gray
`••••••` placeholder, which reads as "empty / type here" rather than "a value
exists and is retained". Users have no clear signal that the field is
intentionally unmodified, and the external clear button is visually inconsistent
with other fields (which use in-input `clearable`).

## Goals

1. On `edit-update`, render the field **non-editable by default** with a clear
   "edit" affordance — an edit icon — signaling the user must opt in to modify
   the secret.
2. The default (unmodified) state shows masked dots representing the retained
   original value.
3. Clicking the edit icon turns the field into an editable password input.
4. Submit semantics are preserved:
   - Never entering edit mode ⇒ submit `null` (keep original).
   - Entering edit mode then clearing ⇒ submit `""` (explicit clear,
     non-required only).
   - Entering edit mode then typing ⇒ submit the new value (overwrite).
5. After entering edit mode, the user can cancel (revert to "unmodified") via a
   close icon that replaces the edit icon in the suffix.

## Non-goals

- No change to the `display` branch (`<span>***</span>`).
- No change to the `edit-create` branch (already `clearable`).
- No change to submit semantics / protocol layer (`null` / `""` / string).
- No change to validation rules / `isSecretExemptFromRequired`.

## Single file

`app-generator/src/main/resources/frontend-skeleton/src/core/fields/SecretField.vue`

## Design

### State

```ts
import { ref, watch } from 'vue'

// edit-update 下是否已进入可输入态。
// false: readonly + 黑点 + edit icon（默认，= 未修改）。
// true:  可输入 password input + close icon。
const editing = ref(false)
// 进入 editing 后用户输入的本地值。未进入时无意义。
const draft = ref<string>('')
// 标记当前 emit 由本组件自身触发。props.value 与父级 v-model 双向绑定，
// 自身 emit 会经父级回流为新的 props.value 触发 watch；此时不应重置 editing。
let internal = false
```

`value` prop reference change re-seeds state — but only for **external** changes
(modal open / true external reset). Self-emitted values (`startEdit`/`cancelEdit`/
`onInput`) round-trip back through the parent `v-model` as a new `props.value`
and would otherwise flip `editing` back to false on the first keystroke. The
`internal` flag distinguishes the two: each handler sets `internal = true`
before emitting; the watch consumes it and skips the reset.

```ts
watch(() => props.value, () => {
  if (internal) {
    internal = false
    return
  }
  editing.value = false
  draft.value = ''
})
```

No sentinel / invisible characters. The default-state dots are a literal
`'••••••'` string rendered by a readonly `type="text"` NInput — visible,
stable, and independent of password-mask rendering quirks.

### Handlers

```ts
function startEdit() {
  editing.value = true
  draft.value = ''
  internal = true
  emit('update:value', null) // 进入态但尚未输入，语义仍为「不修改」
}

function cancelEdit() {
  editing.value = false
  draft.value = ''
  internal = true
  emit('update:value', null) // 回到未修改
}

function onInput(v: string | null) {
  draft.value = v ?? ''
  internal = true
  emit('update:value', v ?? '') // 真实新值，或 ''（显式清空，仅非必填）
}
```

### `edit-update` template

```vue
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
```

Icons: `CreateOutline` and `CloseOutline` from `@vicons/ionicons5`
(already a dependency, `0.12.0`, used by `FileField.vue`, `Login.vue`,
`DashboardLayout.vue`).

### Behavior matrix

| User action                                  | `editing` | emit            | Submit meaning     |
|----------------------------------------------|-----------|-----------------|--------------------|
| (default, no interaction)                    | false     | — (never set)   | `null` ⇒ keep original |
| Click edit icon                              | true      | `null`          | keep original      |
| Click edit icon, then type "x"               | true      | `"x"`           | overwrite          |
| Click edit icon, then clear (clearable)      | true      | `""`            | explicit clear (non-required only) |
| Click edit icon, then click close icon       | false     | `null`          | keep original      |
| Click edit icon, focus, type nothing, save   | true      | `null` (from startEdit) | keep original — see note |

- `clearable` is gated by `item.isNonVoid === false` — required secrets must
  not be clearable to empty (the backend rejects an explicit empty string for a
  required field on update). Preserves the previous `showClearButton` gate.
- `show-password-on` (eye) appears only while `editing && draft` is truthy —
  no reveal affordance on the default dots or on an empty editing field.
- The default-state `readonly` + `type="text"` prevents the browser quirks of
  readonly password inputs (some browsers allow selecting/copying the mask);
  the literal `••••••` is plain visible text the user can see but not edit.

### Preserved

- `display` branch, `editMode` prop, `isEditUpdate` computed.
- `edit-create` branch (unchanged: `clearable` + conditional
  `show-password-on`).
- Submit semantics: `null` ⇒ keep original; `""` ⇒ explicit clear
  (non-required only); real string ⇒ overwrite.
- `isSecretExemptFromRequired` validation.

### Removed

- `NSpace`, `NButton` imports.
- `showClearButton` computed and `onClear` function.
- External "清空" button and the wrapping `NSpace`.

## State interactions

- Default state renders `'••••••'` (6 visible bullet chars) via a readonly
  `type="text"` NInput. The suffix shows the edit icon. The field cannot be
  typed into; `clearable` is off; no eye icon.
- `startEdit` flips `editing` to true. NInput becomes `type="password"`,
  `readonly` off, `value` bound to `draft` (empty ⇒ gray "请输入" placeholder),
  `clearable` on for non-required, suffix swaps to close icon.
- While editing, `onInput` propagates every keystroke as the real value (or
  `""` when cleared). `cancelEdit` reverts to the default state and re-emits
  `null`.
- Modal reopen / external `value` prop change: `watch` resets `editing=false`,
  `draft=''`, so the field always opens in the unmodified default state. The
  `internal` flag guards against self-emitted values (from `startEdit`/
  `cancelEdit`/`onInput`) that round-trip back through the parent `v-model` as
  a new `props.value` — without the guard, the first keystroke would flip
  `editing` back to false and revert the field to dots.

### Edge case — entered edit mode but typed nothing

If the user clicks the edit icon (`startEdit` emits `null`, `draft=''`) and
then saves **without** typing or clearing, no further `update:value` fires, so
the form model stays `null` ⇒ "keep original". The empty input the user sees
does **not** mean "clear". This is intentional: only an explicit clear
(clearable ✕, which fires `onInput('')`) or typing a real value changes the
emitted value. To abandon an accidental edit, the user clicks the close icon.

## Verification

- `node_modules` is absent in the skeleton template directory, so `vue-tsc`
  cannot run here. Verify by manual review of template + TS, and recommend
  running `npm install && npm run typecheck` in a generated project.
- No existing `SecretField` unit tests; none added (skeleton has no component
  test harness for fields).
- Manual checklist for a generated project:
  1. Open edit modal on a record with a secret field → field shows `••••••` +
     edit icon, not editable.
  2. Save without touching → request body secret is `null` (or omitted); value
     unchanged.
  3. Click edit icon → field becomes empty password input with "请输入" placeholder
     + close icon.
  4. Type a value → eye icon appears; save → request body carries the new value.
  5. For a non-required secret: click edit icon, then clear (✕) → save → request
     body carries `""`.
  6. Click edit icon, then close icon → field reverts to `••••••` + edit icon;
     save → `null`.
  7. Required secret in edit mode: no clearable ✕ appears.
