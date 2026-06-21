# App-Generator Iteration: Menu Order, Checkbox Bug, baseOn Cascade

## Fix 1: System menus always sort last

### Problem

`builtin-form.yml` defines User (order=1) and Role (order=2) menus in group "系统". These low order values can cause the system group to appear before user-defined menu groups in the sidebar.

### Solution

In `AppGenerator.java` `generateFrontend()`, after merging builtin menus into the menu list, override their `order` values to a high base (100000 + original order). This ensures `DashboardLayout.vue`'s group sorting always places the system group last while preserving User-before-Role ordering within the group.

### File changed

- `AppGenerator.java`: Add order override loop after `mergedMenus.addAll(builtinMenus)`

---

## Fix 2: Group checkbox not fully checked when all children selected

### Problem

In `RolePage.vue`, the grant permissions modal has a group header `NCheckbox` with `:checked` and `:indeterminate` bindings. This checkbox sits **inside** `NCheckboxGroup`. Naive UI's `NCheckboxGroup` manages all descendant `NCheckbox` components, which interferes with the standalone `:checked` prop on the group header checkbox — preventing it from showing as fully checked even when `isGroupAllSelected()` returns true.

### Solution

Restructure the template: move `NCheckboxGroup` to wrap only the individual permission checkboxes per group, not the group header checkbox. Each group renders as:
1. Standalone `NCheckbox` for group header (outside `NCheckboxGroup`)
2. `NCheckboxGroup` wrapping only the permission item checkboxes

Since `selectedPermissionCodes` is a flat array, we replace the single outer `NCheckboxGroup` with per-group `NCheckboxGroup` instances that share the same `v-model:value`. However, since multiple `NCheckboxGroup` instances can't share a single v-model cleanly, the better approach is to handle updates manually: use `@update:value` on per-group `NCheckboxGroup` to merge changes into the flat `selectedPermissionCodes` array.

Actually, the simplest correct fix: keep the single `NCheckboxGroup` but move each group's header `NCheckbox` **before/outside** the `NCheckboxGroup`. Since the template iterates groups inside the `NCheckboxGroup`, we restructure to:
- Keep one `NCheckboxGroup v-model:value` for all permission checkboxes
- Render each group as a wrapper `div` containing:
  - The header `NCheckbox` (standalone, NOT inside `NCheckboxGroup`)  
  - The children permission checkboxes (inside `NCheckboxGroup`)

Template structure:
```html
<div v-for="group in allPermissionGroups">
  <!-- Group header: standalone, outside NCheckboxGroup -->
  <NCheckbox :checked="..." :indeterminate="..." @update:checked="...">
    {{ group.groupTitle }}
  </NCheckbox>
  <!-- Permission items: inside NCheckboxGroup -->
  <NCheckboxGroup v-model:value="selectedPermissionCodes">
    <NCheckbox v-for="perm in group.permissions" :value="perm.code" :label="perm.title" />
  </NCheckboxGroup>
</div>
```

Wait — multiple `NCheckboxGroup` instances all bound to the same `selectedPermissionCodes` ref will each only manage their own children's values. When group A's `NCheckboxGroup` updates, it will set `selectedPermissionCodes` to only group A's selected codes, wiping out other groups' selections.

**Final approach**: Remove `NCheckboxGroup` entirely. Use individual `NCheckbox` components with manual `:checked` and `@update:checked` for each permission item. This gives full control over the flat `selectedPermissionCodes` array and also enables baseOn cascade logic (Fix 3) naturally.

### File changed

- `RolePage.vue`: Replace `NCheckboxGroup` + child `NCheckbox :value` with individual `NCheckbox :checked` + `@update:checked` per permission

---

## Fix 3: baseOn cascade on permission check/uncheck

### Problem

Permission points have a `baseOn` field (e.g., CREATE_USER baseOn LIST_USER). Currently the grant modal ignores this relationship — checking/unchecking permissions doesn't cascade.

### Required behavior

1. **Uncheck cascade (down)**: When unchecking permission A, also uncheck all permissions where `baseOn === A.code` (recursively if needed, but current data model is single-level)
2. **Check cascade (up)**: When checking permission A that has `baseOn = B`, also check B

### Solution

Add a handler function `handlePermissionCheck(code, checked)` that:
- If `checked = true`: add the code to `selectedPermissionCodes`, then find the permission's `baseOn` and add that too (if not already present)
- If `checked = false`: remove the code from `selectedPermissionCodes`, then find all permissions across all groups where `baseOn === code` and remove those too

This handler is called from each individual `NCheckbox @update:checked` (which aligns with Fix 2's approach of removing `NCheckboxGroup`).

### File changed

- `RolePage.vue`: Add `handlePermissionCheck()` function, wire to individual checkbox `@update:checked`

---

## Summary of changes

| File | Change |
|------|--------|
| `AppGenerator.java` | Override builtin menu order to 100000+ after merge |
| `RolePage.vue` | Remove `NCheckboxGroup`, use individual `NCheckbox` with manual state; add baseOn cascade logic |
