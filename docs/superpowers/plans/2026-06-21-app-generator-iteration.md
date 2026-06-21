# App-Generator Iteration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:
> executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix three app-generator issues: force system menus to sort last, fix group checkbox display bug, and add
baseOn cascade logic for permission checkboxes.

**Architecture:** Two files changed. `AppGenerator.java` gets a post-merge order override for builtin menus.
`RolePage.vue` gets a restructured checkbox template (removing `NCheckboxGroup` in favor of individual `NCheckbox` with
manual state) and a new `handlePermissionCheck()` function implementing baseOn cascade.

**Tech Stack:** Java 21, Vue 3 + Naive UI, TypeScript

---

### Task 1: Force builtin menu order to sort last

**Files:**

- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java:160-163`

- [ ] **Step 1: Add builtin menu order override after merge**

In `AppGenerator.java`, inside `generateFrontend()`, add a loop after line 163 (`mergedMenus.addAll(builtinMenus)`) that
overrides each builtin menu's order:

```java
        // Force builtin menus to sort after all user-defined menus
        for (MenuDef builtinMenu : builtinMenus) {
            builtinMenu.setOrder(100000 + (builtinMenu.getOrder() != null ? builtinMenu.getOrder() : 0));
        }
```

This sets User's order to `100001` and Role's order to `100002`, ensuring they sort after any user-defined menu group
while preserving their relative ordering.

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl app-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java
git commit -m "fix: force builtin menus to sort after user-defined menus in sidebar"
```

---

### Task 2: Fix group checkbox bug and add baseOn cascade in RolePage.vue

**Files:**

- Modify: `app-generator/src/main/resources/frontend-skeleton/src/pages/RolePage.vue`

- [ ] **Step 1: Remove NCheckboxGroup import and add helper function for permission lookup**

In `RolePage.vue`, update the import on line 9 — remove `NCheckboxGroup` from the import list:

```typescript
import {
  NButton, NSpace, NPopconfirm, NModal, NCheckbox, NDivider,
  useMessage
} from 'naive-ui'
```

Then, after the `grantLoading` ref (line 46), add a helper to look up a permission item by code:

```typescript
function findPermission(code: string): PermissionItem | undefined {
  for (const group of allPermissionGroups.value) {
    const found = group.permissions.find(p => p.code === code)
    if (found) return found
  }
  return undefined
}
```

- [ ] **Step 2: Add handlePermissionCheck with baseOn cascade logic**

Replace the existing `toggleGroupAll` function (lines 78-86) with both `toggleGroupAll` and `handlePermissionCheck`:

```typescript
function toggleGroupAll(group: PermissionGroup, checked: boolean) {
  if (checked) {
    const set = new Set([...selectedPermissionCodes.value, ...group.permissions.map(p => p.code)])
    selectedPermissionCodes.value = [...set]
  } else {
    const codes = new Set(group.permissions.map(p => p.code))
    selectedPermissionCodes.value = selectedPermissionCodes.value.filter(c => !codes.has(c))
  }
}

function handlePermissionCheck(code: string, checked: boolean) {
  const set = new Set(selectedPermissionCodes.value)
  if (checked) {
    set.add(code)
    const perm = findPermission(code)
    if (perm?.baseOn) {
      set.add(perm.baseOn)
    }
  } else {
    set.delete(code)
    for (const group of allPermissionGroups.value) {
      for (const p of group.permissions) {
        if (p.baseOn === code) {
          set.delete(p.code)
        }
      }
    }
  }
  selectedPermissionCodes.value = [...set]
}
```

- [ ] **Step 3: Restructure the template — remove NCheckboxGroup, use individual NCheckbox**

Replace the entire modal body (lines 177-200) with:

```html
      <div v-else>
        <div v-for="group in allPermissionGroups" :key="group.groupCode" style="margin-bottom: 16px">
          <div style="display: flex; align-items: center; margin-bottom: 8px">
            <NCheckbox
              :checked="isGroupAllSelected(group)"
              :indeterminate="isGroupPartialSelected(group)"
              @update:checked="(v: boolean) => toggleGroupAll(group, v)"
            >
              <span style="font-weight: 600; font-size: 14px; color: #1e293b">{{ group.groupTitle }}</span>
            </NCheckbox>
          </div>
          <div style="padding-left: 24px; display: flex; flex-wrap: wrap; gap: 8px 16px">
            <NCheckbox
              v-for="perm in group.permissions"
              :key="perm.code"
              :checked="selectedPermissionCodes.includes(perm.code)"
              :label="perm.title"
              @update:checked="(v: boolean) => handlePermissionCheck(perm.code, v)"
            />
          </div>
          <NDivider style="margin: 12px 0" />
        </div>
      </div>
```

Key changes from the original template:

- Removed the outer `<NCheckboxGroup v-model:value="selectedPermissionCodes">` wrapper
- Each permission `NCheckbox` now uses `:checked` + `@update:checked` instead of `:value`
- The `@update:checked` calls `handlePermissionCheck` which handles baseOn cascade
- Group header checkbox remains standalone with `:checked` and `:indeterminate` — now works correctly since it's no
  longer inside an `NCheckboxGroup`

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/pages/RolePage.vue
git commit -m "fix: group checkbox display bug and add baseOn cascade for permission grant"
```
