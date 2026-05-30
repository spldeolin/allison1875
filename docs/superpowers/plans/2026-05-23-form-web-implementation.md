# form-web Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Vue 3 frontend that reads YML Form DSL at build time and renders complete CRUD pages (search + table + create/edit modal + delete) at runtime.

**Architecture:** Vite plugin converts YML files to a virtual JSON module. Vue Router dynamically generates routes from the JSON. A `CrudPage` component receives a schema prop and orchestrates sub-components (SearchForm, DataTable, EditModal) that use field-type renderers to pick Naive UI controls.

**Tech Stack:** Vue 3.5, Naive UI 2.41, Vite 5.4, TypeScript 5.6, Vue Router 4.4, Pinia 2.2, Axios 1.7, js-yaml 4.1, Vitest 2.1

---

## File Map

```
form-web/
├── package.json
├── pnpm-lock.yaml
├── tsconfig.json
├── tsconfig.node.json
├── vite.config.ts
├── index.html
├── plugins/
│   └── vite-plugin-form-dsl.ts       # Vite plugin: scan YML → virtual module
├── src/
│   ├── main.ts                        # App entry
│   ├── App.vue                        # Root component (router-view)
│   ├── env.d.ts                       # Type declarations for virtual modules
│   ├── dsl/
│   │   └── student.yml                # Sample DSL for development & testing
│   ├── schema/
│   │   └── types.ts                   # TypeScript interfaces for FormDef/ItemDef
│   ├── router/
│   │   └── index.ts                   # Dynamic route generation from DSL
│   ├── stores/
│   │   └── auth.ts                    # Auth Pinia store
│   ├── utils/
│   │   ├── request.ts                 # Axios instance with auth interceptor
│   │   └── naming.ts                  # upperCamelToKebab, API path derivation
│   ├── layouts/
│   │   └── DashboardLayout.vue        # Sidebar menu + content area
│   ├── views/
│   │   └── Login.vue                  # Login page
│   └── core/
│       ├── CrudPage.vue               # CRUD page orchestrator
│       ├── SearchForm.vue             # Search bar
│       ├── DataTable.vue              # Data table with pagination
│       ├── EditModal.vue              # Create/edit modal
│       └── fields/
│           ├── FieldRenderer.vue      # Type-based field dispatcher
│           ├── TextField.vue
│           ├── NumberField.vue
│           ├── SelectField.vue
│           ├── MultiSelectField.vue
│           ├── TimeField.vue
│           ├── OnOffField.vue
│           └── SecretField.vue
└── tests/
    ├── plugins/
    │   └── vite-plugin-form-dsl.test.ts
    ├── schema/
    │   └── types.test.ts
    └── utils/
        └── naming.test.ts
```

---

### Task 1: Project Scaffolding

**Files:**
- Create: `package.json`
- Create: `tsconfig.json`
- Create: `tsconfig.node.json`
- Create: `vite.config.ts`
- Create: `index.html`
- Create: `src/main.ts`
- Create: `src/App.vue`
- Create: `src/env.d.ts`

- [ ] **Step 1: Initialize package.json**

```json
{
  "name": "form-web",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc -b && vite build",
    "preview": "vite preview",
    "test": "vitest"
  },
  "dependencies": {
    "vue": "3.5.12",
    "vue-router": "4.4.5",
    "pinia": "2.2.6",
    "naive-ui": "2.41.0",
    "axios": "1.7.9",
    "@vicons/ionicons5": "0.12.0",
    "vfonts": "0.0.3"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "5.1.4",
    "typescript": "5.6.3",
    "vite": "5.4.10",
    "vue-tsc": "2.1.10",
    "js-yaml": "4.1.0",
    "@types/js-yaml": "4.0.9",
    "vitest": "2.1.4",
    "@vue/test-utils": "2.4.6",
    "happy-dom": "15.7.4"
  }
}
```

- [ ] **Step 2: Create tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue", "src/env.d.ts"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

- [ ] **Step 3: Create tsconfig.node.json**

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "noEmit": true,
    "strict": true,
    "skipLibCheck": true
  },
  "include": ["vite.config.ts", "plugins/**/*.ts"]
}
```

- [ ] **Step 4: Create vite.config.ts (minimal, plugin added in Task 3)**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5180
  }
})
```

- [ ] **Step 5: Create index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Form Web</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
```

- [ ] **Step 6: Create src/env.d.ts**

```typescript
/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'virtual:form-dsl' {
  import type { FormDef } from '@/schema/types'
  const formDefs: FormDef[]
  export default formDefs
}
```

- [ ] **Step 7: Create src/App.vue**

```vue
<script setup lang="ts">
</script>

<template>
  <router-view />
</template>
```

- [ ] **Step 8: Create src/main.ts (minimal, will be expanded in later tasks)**

```typescript
import { createApp } from 'vue'
import App from './App.vue'

const app = createApp(App)
app.mount('#app')
```

- [ ] **Step 9: Install dependencies and verify**

Run: `pnpm install`
Expected: Lock file generated, no errors.

Run: `pnpm run dev`
Expected: Vite dev server starts on port 5180, blank page renders.

- [ ] **Step 10: Commit**

```bash
git init
git add .
git commit -m "feat: scaffold form-web project with Vue 3 + Naive UI + Vite + TypeScript"
```

---

### Task 2: Schema Type Definitions

**Files:**
- Create: `src/schema/types.ts`
- Create: `tests/schema/types.test.ts`

- [ ] **Step 1: Write the test for schema types**

```typescript
// tests/schema/types.test.ts
import { describe, it, expect } from 'vitest'
import type { FormDef, TextItemDef, NumberItemDef, SelectItemDef, MultiSelectItemDef, TimeItemDef, OnOffItemDef, SecretItemDef, ItemDef } from '@/schema/types'

describe('FormDef types', () => {
  it('should allow a valid FormDef with all item types', () => {
    const form: FormDef = {
      name: 'StudentBasicInfo',
      title: '学生基本信息',
      group: '学生管理',
      icon: 'PersonOutline',
      order: 1,
      items: [
        { type: 'text', name: 'studentName', title: '学生姓名', isNonVoid: true, canInputOnInit: 'true', canInputOnEdit: 'true', maxLength: 50 },
        { type: 'number', name: 'score', title: '分数', isNonVoid: false, canInputOnInit: 'true', canInputOnEdit: 'true', canBeDecimal: true },
        { type: 'select', name: 'grade', title: '年级', isNonVoid: true, canInputOnInit: 'true', canInputOnEdit: 'true', options: [{ code: 'g1', title: '一年级' }] },
        { type: 'multiSelect', name: 'tags', title: '标签', isNonVoid: false, canInputOnInit: 'true', canInputOnEdit: 'true', options: [{ code: 't1', title: '标签1' }] },
        { type: 'time', name: 'enrollDate', title: '入学日期', isNonVoid: true, canInputOnInit: 'true', canInputOnEdit: 'true', format: 'date' },
        { type: 'onOff', name: 'isActive', title: '是否激活', isNonVoid: true, canInputOnInit: 'true', canInputOnEdit: 'true' },
        { type: 'secret', name: 'idCard', title: '身份证号', isNonVoid: true, canInputOnInit: 'true', canInputOnEdit: 'false' },
      ],
      indices: [{ itemNames: ['studentName'], isUnique: true }]
    }
    expect(form.name).toBe('StudentBasicInfo')
    expect(form.items).toHaveLength(7)
    expect((form.items[0] as TextItemDef).maxLength).toBe(50)
    expect((form.items[4] as TimeItemDef).format).toBe('date')
  })

  it('should allow FormDef without optional fields', () => {
    const form: FormDef = {
      name: 'Minimal',
      title: '最小表单',
      items: [
        { type: 'text', name: 'field1', title: '字段1', isNonVoid: true, canInputOnInit: 'true', canInputOnEdit: 'true' }
      ]
    }
    expect(form.desc).toBeUndefined()
    expect(form.group).toBeUndefined()
    expect(form.indices).toBeUndefined()
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `pnpm test -- tests/schema/types.test.ts`
Expected: FAIL — cannot resolve `@/schema/types`

- [ ] **Step 3: Implement schema types**

```typescript
// src/schema/types.ts

export type Boolean = 'doNot' | 'userInput' | 'todo'
export type TimeFormat = 'date' | 'time' | 'dateTime'

export interface OptionDef {
  code: string
  title: string
}

export interface IndexDef {
  itemNames: string[]
  isUnique: boolean
}

interface ItemDefBase {
  name: string
  title: string
  isNonVoid: boolean
  canInputOnInit: Boolean
  canInputOnEdit: Boolean
}

export interface TextItemDef extends ItemDefBase {
  type: 'text'
  maxLength?: number
  isMultilineOrRich?: boolean
  regex?: string
}

export interface NumberItemDef extends ItemDefBase {
  type: 'number'
  canBeDecimal?: boolean
}

export interface SelectItemDef extends ItemDefBase {
  type: 'select'
  options: OptionDef[]
}

export interface MultiSelectItemDef extends ItemDefBase {
  type: 'multiSelect'
  options: OptionDef[]
}

export interface TimeItemDef extends ItemDefBase {
  type: 'time'
  format: TimeFormat
}

export interface OnOffItemDef extends ItemDefBase {
  type: 'onOff'
}

export interface SecretItemDef extends ItemDefBase {
  type: 'secret'
}

export type ItemDef =
  | TextItemDef
  | NumberItemDef
  | SelectItemDef
  | MultiSelectItemDef
  | TimeItemDef
  | OnOffItemDef
  | SecretItemDef

export interface FormDef {
  name: string
  title: string
  desc?: string
  group?: string
  icon?: string
  order?: number
  items: ItemDef[]
  indices?: IndexDef[]
}
```

- [ ] **Step 4: Configure Vitest**

Add to `vite.config.ts`:

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5180
  },
  test: {
    environment: 'happy-dom'
  }
})
```

- [ ] **Step 5: Run test to verify it passes**

Run: `pnpm test -- tests/schema/types.test.ts`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/schema/types.ts tests/schema/types.test.ts vite.config.ts
git commit -m "feat: define TypeScript interfaces for Form DSL schema"
```

---

### Task 3: Naming Utilities

**Files:**
- Create: `src/utils/naming.ts`
- Create: `tests/utils/naming.test.ts`

- [ ] **Step 1: Write the tests**

```typescript
// tests/utils/naming.test.ts
import { describe, it, expect } from 'vitest'
import { upperCamelToKebab, deriveApiBasePath } from '@/utils/naming'

describe('upperCamelToKebab', () => {
  it('converts UpperCamel to kebab-case', () => {
    expect(upperCamelToKebab('StudentBasicInfo')).toBe('student-basic-info')
    expect(upperCamelToKebab('User')).toBe('user')
    expect(upperCamelToKebab('APIKey')).toBe('api-key')
    expect(upperCamelToKebab('DormitoryApplication')).toBe('dormitory-application')
  })
})

describe('deriveApiBasePath', () => {
  it('derives API base path from FormDef name', () => {
    // Placeholder rule: /api/v1/{lowerCamelName}
    // Actual rule to be confirmed with Allison1875
    expect(deriveApiBasePath('StudentBasicInfo')).toBe('/api/v1/studentBasicInfo')
    expect(deriveApiBasePath('User')).toBe('/api/v1/user')
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `pnpm test -- tests/utils/naming.test.ts`
Expected: FAIL — cannot resolve `@/utils/naming`

- [ ] **Step 3: Implement naming utilities**

```typescript
// src/utils/naming.ts

export function upperCamelToKebab(str: string): string {
  return str
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1-$2')
    .replace(/([a-z\d])([A-Z])/g, '$1-$2')
    .toLowerCase()
}

export function upperCamelToLowerCamel(str: string): string {
  return str.charAt(0).toLowerCase() + str.slice(1)
}

// TODO: Actual rule to be confirmed with Allison1875 backend code generation logic
export function deriveApiBasePath(formName: string): string {
  return `/api/v1/${upperCamelToLowerCamel(formName)}`
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `pnpm test -- tests/utils/naming.test.ts`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/utils/naming.ts tests/utils/naming.test.ts
git commit -m "feat: add naming utilities for route path and API path derivation"
```

---

### Task 4: Vite DSL Plugin

**Files:**
- Create: `plugins/vite-plugin-form-dsl.ts`
- Create: `src/dsl/student.yml`
- Create: `tests/plugins/vite-plugin-form-dsl.test.ts`
- Modify: `vite.config.ts`

- [ ] **Step 1: Create the sample DSL file**

```yaml
# src/dsl/student.yml
forms:
  - name: StudentBasicInfo
    title: 学生基本信息
    desc: 学生身份信息管理
    group: 学生管理
    icon: PersonOutline
    order: 1
    items:
      - type: text
        name: studentName
        title: 学生姓名
        isNonVoid: true
        canInputOnInit: true
        canInputOnEdit: true
        maxLength: 50
      - type: number
        name: studentId
        title: 学号
        isNonVoid: true
        canInputOnInit: true
        canInputOnEdit: false
        canBeDecimal: false
      - type: select
        name: grade
        title: 年级
        isNonVoid: true
        canInputOnInit: true
        canInputOnEdit: true
        options:
          - code: grade1
            title: 一年级
          - code: grade2
            title: 二年级
          - code: grade3
            title: 三年级
      - type: time
        name: enrollmentDate
        title: 入学日期
        isNonVoid: true
        canInputOnInit: true
        canInputOnEdit: false
        format: date
      - type: onOff
        name: isActive
        title: 是否在读
        isNonVoid: true
        canInputOnInit: true
        canInputOnEdit: true
      - type: secret
        name: idCardNumber
        title: 身份证号
        isNonVoid: true
        canInputOnInit: true
        canInputOnEdit: false
    indices:
      - itemNames: [studentId]
        isUnique: true
```

- [ ] **Step 2: Write the plugin test**

```typescript
// tests/plugins/vite-plugin-form-dsl.test.ts
import { describe, it, expect } from 'vitest'
import { parseDslFiles } from '../../plugins/vite-plugin-form-dsl'
import path from 'path'

describe('parseDslFiles', () => {
  it('parses YML files from a directory into FormDef array', () => {
    const dslDir = path.resolve(__dirname, '../../src/dsl')
    const result = parseDslFiles(dslDir)

    expect(result).toBeInstanceOf(Array)
    expect(result.length).toBeGreaterThanOrEqual(1)

    const student = result.find(f => f.name === 'StudentBasicInfo')
    expect(student).toBeDefined()
    expect(student!.title).toBe('学生基本信息')
    expect(student!.group).toBe('学生管理')
    expect(student!.items).toHaveLength(6)
    expect(student!.items[0].type).toBe('text')
    expect(student!.items[0].name).toBe('studentName')
  })

  it('handles multiple forms in a single YML file', () => {
    const dslDir = path.resolve(__dirname, '../../src/dsl')
    const result = parseDslFiles(dslDir)
    // student.yml has one form, so at least 1
    expect(result.length).toBeGreaterThanOrEqual(1)
  })
})
```

- [ ] **Step 3: Run test to verify it fails**

Run: `pnpm test -- tests/plugins/vite-plugin-form-dsl.test.ts`
Expected: FAIL — cannot resolve `parseDslFiles`

- [ ] **Step 4: Implement the Vite plugin**

```typescript
// plugins/vite-plugin-form-dsl.ts
import fs from 'fs'
import path from 'path'
import yaml from 'js-yaml'
import type { Plugin } from 'vite'
import type { FormDef } from '../src/schema/types'

const VIRTUAL_MODULE_ID = 'virtual:form-dsl'
const RESOLVED_VIRTUAL_MODULE_ID = '\0' + VIRTUAL_MODULE_ID

export function parseDslFiles(dslDir: string): FormDef[] {
  if (!fs.existsSync(dslDir)) {
    return []
  }
  const files = fs.readdirSync(dslDir).filter(f => f.endsWith('.yml') || f.endsWith('.yaml'))
  const allForms: FormDef[] = []

  for (const file of files) {
    const content = fs.readFileSync(path.join(dslDir, file), 'utf-8')
    const parsed = yaml.load(content) as { forms?: FormDef[] }
    if (parsed && parsed.forms) {
      allForms.push(...parsed.forms)
    }
  }
  return allForms
}

export default function formDslPlugin(dslDir: string): Plugin {
  return {
    name: 'vite-plugin-form-dsl',
    resolveId(id) {
      if (id === VIRTUAL_MODULE_ID) {
        return RESOLVED_VIRTUAL_MODULE_ID
      }
    },
    load(id) {
      if (id === RESOLVED_VIRTUAL_MODULE_ID) {
        const forms = parseDslFiles(dslDir)
        return `export default ${JSON.stringify(forms)}`
      }
    },
    handleHotUpdate({ file, server }) {
      if (file.startsWith(dslDir) && (file.endsWith('.yml') || file.endsWith('.yaml'))) {
        const module = server.moduleGraph.getModuleById(RESOLVED_VIRTUAL_MODULE_ID)
        if (module) {
          server.moduleGraph.invalidateModule(module)
          server.ws.send({ type: 'full-reload' })
        }
      }
    }
  }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `pnpm test -- tests/plugins/vite-plugin-form-dsl.test.ts`
Expected: PASS

- [ ] **Step 6: Register plugin in vite.config.ts**

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import formDslPlugin from './plugins/vite-plugin-form-dsl'

export default defineConfig({
  plugins: [
    vue(),
    formDslPlugin(path.resolve(__dirname, 'src/dsl'))
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5180
  },
  test: {
    environment: 'happy-dom'
  }
})
```

- [ ] **Step 7: Verify dev server starts without errors**

Run: `pnpm run dev`
Expected: Vite starts, no plugin errors.

- [ ] **Step 8: Commit**

```bash
git add plugins/ src/dsl/student.yml vite.config.ts tests/plugins/
git commit -m "feat: implement Vite plugin for YML DSL to virtual JSON module"
```

---

### Task 5: Auth Store & Request Utility

**Files:**
- Create: `src/stores/auth.ts`
- Create: `src/utils/request.ts`

- [ ] **Step 1: Create auth store**

```typescript
// src/stores/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY_TOKEN = 'form_web_token'
const STORAGE_KEY_USER = 'form_web_user'
const STORAGE_KEY_PERMISSIONS = 'form_web_permissions'

export interface UserInfo {
  id: string
  username: string
  displayName: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(STORAGE_KEY_TOKEN) || '')
  const userInfo = ref<UserInfo | null>(
    JSON.parse(localStorage.getItem(STORAGE_KEY_USER) || 'null')
  )
  const permissions = ref<string[]>(
    JSON.parse(localStorage.getItem(STORAGE_KEY_PERMISSIONS) || '[]')
  )

  const isAuthenticated = computed(() => !!token.value)

  function setAuth(t: string, user: UserInfo, perms: string[]) {
    token.value = t
    userInfo.value = user
    permissions.value = perms
    localStorage.setItem(STORAGE_KEY_TOKEN, t)
    localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(user))
    localStorage.setItem(STORAGE_KEY_PERMISSIONS, JSON.stringify(perms))
  }

  function getToken(): string {
    return token.value
  }

  function getUserInfo(): UserInfo | null {
    return userInfo.value
  }

  function hasPermission(perm: string): boolean {
    return permissions.value.includes(perm)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem(STORAGE_KEY_TOKEN)
    localStorage.removeItem(STORAGE_KEY_USER)
    localStorage.removeItem(STORAGE_KEY_PERMISSIONS)
  }

  return { token, userInfo, permissions, isAuthenticated, setAuth, getToken, getUserInfo, hasPermission, logout }
})
```

- [ ] **Step 2: Create request utility**

```typescript
// src/utils/request.ts
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

export interface ApiBaseResult<T = unknown> {
  code: number
  msg: string
  result: T
}

export interface PageResult<T> {
  count: number
  list: T[]
}

const request = axios.create({
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

request.interceptors.request.use(config => {
  const authStore = useAuthStore()
  const token = authStore.getToken()
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
})

request.interceptors.response.use(
  response => {
    const data = response.data as ApiBaseResult
    if (data.code === 200) {
      return response
    }
    if (data.code === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.hash = '#/login'
      return Promise.reject(new Error('认证已过期'))
    }
    return Promise.reject(new Error(data.msg || '请求失败'))
  },
  error => Promise.reject(error)
)

export default request
```

- [ ] **Step 3: Commit**

```bash
git add src/stores/auth.ts src/utils/request.ts
git commit -m "feat: implement auth store and axios request utility with token interceptor"
```

---

### Task 6: Router Setup

**Files:**
- Create: `src/router/index.ts`
- Modify: `src/main.ts`

- [ ] **Step 1: Create router with dynamic DSL routes**

```typescript
// src/router/index.ts
import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import formDefs from 'virtual:form-dsl'
import { upperCamelToKebab } from '@/utils/naming'
import { useAuthStore } from '@/stores/auth'

const CrudPage = () => import('@/core/CrudPage.vue')
const DashboardLayout = () => import('@/layouts/DashboardLayout.vue')
const Login = () => import('@/views/Login.vue')

const dslRoutes: RouteRecordRaw[] = formDefs.map(def => ({
  path: `/${upperCamelToKebab(def.name)}`,
  name: def.name,
  component: CrudPage,
  props: { schema: def },
  meta: { title: def.title, group: def.group, icon: def.icon, order: def.order }
}))

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: DashboardLayout,
    meta: { requiresAuth: true },
    children: [
      ...dslRoutes,
      { path: '', redirect: dslRoutes.length > 0 ? dslRoutes[0].path : '/login' }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const requiresAuth = to.matched.some(r => r.meta.requiresAuth !== false)
  if (requiresAuth) {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }
})

export default router
```

- [ ] **Step 2: Update main.ts to register router and pinia**

```typescript
// src/main.ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
```

- [ ] **Step 3: Verify dev server starts (will show blank page since CrudPage/Layout not yet created)**

Run: `pnpm run dev`
Expected: No compilation errors. Browser shows blank page (components not yet created).

- [ ] **Step 4: Commit**

```bash
git add src/router/index.ts src/main.ts
git commit -m "feat: set up Vue Router with dynamic DSL-based route generation"
```

---

### Task 7: Login Page

**Files:**
- Create: `src/views/Login.vue`

- [ ] **Step 1: Implement Login page**

```vue
<!-- src/views/Login.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, useMessage, type FormInst } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'
import type { ApiBaseResult } from '@/utils/request'

interface LoginResp {
  token: string
  userInfo: {
    id: string
    username: string
    displayName: string
  }
  permissions: string[]
}

const router = useRouter()
const route = useRoute()
const message = useMessage()
const authStore = useAuthStore()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formData = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const { data } = await request.post<ApiBaseResult<LoginResp>>('/api/v1/login', formData.value)
    const { token, userInfo, permissions } = data.result
    authStore.setAuth(token, userInfo, permissions)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    message.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="display: flex; align-items: center; justify-content: center; min-height: 100vh; background: #f0f2f5;">
    <NCard title="Form Web" style="width: 380px;">
      <NForm ref="formRef" :model="formData" :rules="rules">
        <NFormItem label="用户名" path="username">
          <NInput v-model:value="formData.username" placeholder="请输入用户名" @keyup.enter="handleLogin" />
        </NFormItem>
        <NFormItem label="密码" path="password">
          <NInput v-model:value="formData.password" type="password" placeholder="请输入密码" show-password-on="click" @keyup.enter="handleLogin" />
        </NFormItem>
        <NButton type="primary" block :loading="loading" @click="handleLogin">
          登录
        </NButton>
      </NForm>
    </NCard>
  </div>
</template>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/Login.vue
git commit -m "feat: implement login page with username/password form"
```

---

### Task 8: Dashboard Layout with Sidebar Menu

**Files:**
- Create: `src/layouts/DashboardLayout.vue`

- [ ] **Step 1: Implement DashboardLayout**

```vue
<!-- src/layouts/DashboardLayout.vue -->
<script setup lang="ts">
import { computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NLayout, NLayoutSider, NLayoutContent, NMenu, NButton, type MenuOption } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import * as icons from '@vicons/ionicons5'
import { NIcon } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

interface RouteMeta {
  title?: string
  group?: string
  icon?: string
  order?: number
}

const menuOptions = computed<MenuOption[]>(() => {
  const dslRoutes = router.getRoutes().filter(r => r.meta?.group)

  const groups = new Map<string, { routes: typeof dslRoutes; order: number }>()
  for (const r of dslRoutes) {
    const meta = r.meta as RouteMeta
    const group = meta.group!
    if (!authStore.hasPermission(r.name as string)) continue
    if (!groups.has(group)) {
      groups.set(group, { routes: [], order: meta.order ?? 99 })
    }
    groups.get(group)!.routes.push(r)
  }

  const sortedGroups = [...groups.entries()].sort((a, b) => a[1].order - b[1].order)

  return sortedGroups.map(([groupName, { routes }]) => {
    const sortedRoutes = routes.sort((a, b) => ((a.meta as RouteMeta).order ?? 99) - ((b.meta as RouteMeta).order ?? 99))
    return {
      label: groupName,
      key: groupName,
      children: sortedRoutes.map(r => {
        const meta = r.meta as RouteMeta
        const iconComp = meta.icon ? (icons as any)[meta.icon] : undefined
        return {
          label: meta.title,
          key: r.path,
          icon: iconComp ? () => h(NIcon, null, { default: () => h(iconComp) }) : undefined
        }
      })
    }
  })
})

const activeKey = computed(() => route.path)

function handleMenuUpdate(key: string) {
  router.push(key)
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <NLayout has-sider style="height: 100vh;">
    <NLayoutSider bordered :width="220" :collapsed-width="64" show-trigger>
      <div style="padding: 16px; font-size: 18px; font-weight: bold; text-align: center;">
        Form Web
      </div>
      <NMenu :options="menuOptions" :value="activeKey" @update:value="handleMenuUpdate" />
      <div style="position: absolute; bottom: 16px; left: 0; right: 0; padding: 0 16px;">
        <NButton block quaternary @click="handleLogout">退出登录</NButton>
      </div>
    </NLayoutSider>
    <NLayoutContent style="padding: 24px;">
      <router-view />
    </NLayoutContent>
  </NLayout>
</template>
```

- [ ] **Step 2: Verify in browser**

Run: `pnpm run dev`
Expected: Navigate to `http://localhost:5180`. Should redirect to `/#/login`. Login page renders. (Layout visible after login — no backend yet, so login will fail, but page should render.)

- [ ] **Step 3: Commit**

```bash
git add src/layouts/DashboardLayout.vue
git commit -m "feat: implement dashboard layout with auto-generated sidebar menu from DSL"
```

---

### Task 9: Field Renderers

**Files:**
- Create: `src/core/fields/FieldRenderer.vue`
- Create: `src/core/fields/TextField.vue`
- Create: `src/core/fields/NumberField.vue`
- Create: `src/core/fields/SelectField.vue`
- Create: `src/core/fields/MultiSelectField.vue`
- Create: `src/core/fields/TimeField.vue`
- Create: `src/core/fields/OnOffField.vue`
- Create: `src/core/fields/SecretField.vue`

- [ ] **Step 1: Create TextField.vue**

```vue
<!-- src/core/fields/TextField.vue -->
<script setup lang="ts">
import { NInput, NEllipsis } from 'naive-ui'
import type { TextItemDef } from '@/schema/types'

const props = defineProps<{
  item: TextItemDef
  mode: 'search' | 'edit' | 'display'
  value: string | null
}>()

const emit = defineEmits<{
  'update:value': [val: string | null]
}>()
</script>

<template>
  <template v-if="mode === 'display'">
    <NEllipsis>{{ value ?? '' }}</NEllipsis>
  </template>
  <template v-else-if="mode === 'search'">
    <NInput :value="value" clearable placeholder="请输入" @update:value="emit('update:value', $event)" />
  </template>
  <template v-else>
    <NInput
      v-if="item.isMultilineOrRich"
      type="textarea"
      :value="value"
      :maxlength="item.maxLength"
      show-count
      placeholder="请输入"
      @update:value="emit('update:value', $event)"
    />
    <NInput
      v-else
      :value="value"
      :maxlength="item.maxLength"
      clearable
      placeholder="请输入"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
```

- [ ] **Step 2: Create NumberField.vue**

```vue
<!-- src/core/fields/NumberField.vue -->
<script setup lang="ts">
import { NInputNumber } from 'naive-ui'
import type { NumberItemDef } from '@/schema/types'

const props = defineProps<{
  item: NumberItemDef
  mode: 'search' | 'edit' | 'display'
  value: number | null
}>()

const emit = defineEmits<{
  'update:value': [val: number | null]
}>()
</script>

<template>
  <template v-if="mode === 'display'">
    <span>{{ value ?? '' }}</span>
  </template>
  <template v-else>
    <NInputNumber
      :value="value"
      clearable
      :precision="item.canBeDecimal ? undefined : 0"
      placeholder="请输入"
      style="width: 100%;"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
```

- [ ] **Step 3: Create SelectField.vue**

```vue
<!-- src/core/fields/SelectField.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { NSelect, NTag } from 'naive-ui'
import type { SelectItemDef } from '@/schema/types'

const props = defineProps<{
  item: SelectItemDef
  mode: 'search' | 'edit' | 'display'
  value: string | null
}>()

const emit = defineEmits<{
  'update:value': [val: string | null]
}>()

const selectOptions = computed(() =>
  props.item.options.map(opt => ({ label: opt.title, value: opt.code }))
)

const displayTitle = computed(() => {
  const opt = props.item.options.find(o => o.code === props.value)
  return opt?.title ?? ''
})
</script>

<template>
  <template v-if="mode === 'display'">
    <NTag v-if="value" size="small">{{ displayTitle }}</NTag>
    <span v-else></span>
  </template>
  <template v-else>
    <NSelect
      :value="value"
      :options="selectOptions"
      clearable
      filterable
      placeholder="请选择"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
```

- [ ] **Step 4: Create MultiSelectField.vue**

```vue
<!-- src/core/fields/MultiSelectField.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { NSelect, NTag, NSpace } from 'naive-ui'
import type { MultiSelectItemDef } from '@/schema/types'

const props = defineProps<{
  item: MultiSelectItemDef
  mode: 'search' | 'edit' | 'display'
  value: string[] | null
}>()

const emit = defineEmits<{
  'update:value': [val: string[] | null]
}>()

const selectOptions = computed(() =>
  props.item.options.map(opt => ({ label: opt.title, value: opt.code }))
)

const displayTitles = computed(() => {
  if (!props.value) return []
  return props.value.map(code => {
    const opt = props.item.options.find(o => o.code === code)
    return opt?.title ?? code
  })
})
</script>

<template>
  <template v-if="mode === 'display'">
    <NSpace>
      <NTag v-for="t in displayTitles" :key="t" size="small">{{ t }}</NTag>
    </NSpace>
  </template>
  <template v-else>
    <NSelect
      :value="value"
      :options="selectOptions"
      multiple
      clearable
      filterable
      placeholder="请选择"
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
```

- [ ] **Step 5: Create TimeField.vue**

```vue
<!-- src/core/fields/TimeField.vue -->
<script setup lang="ts">
import { NDatePicker, NTimePicker } from 'naive-ui'
import type { TimeItemDef } from '@/schema/types'

const props = defineProps<{
  item: TimeItemDef
  mode: 'search' | 'edit' | 'display'
  value: number | [number, number] | null
}>()

const emit = defineEmits<{
  'update:value': [val: number | [number, number] | null]
}>()

function formatDisplay(val: number | [number, number] | null): string {
  if (val === null) return ''
  const ts = typeof val === 'number' ? val : val[0]
  const d = new Date(ts)
  if (props.item.format === 'date') return d.toLocaleDateString('zh-CN')
  if (props.item.format === 'time') return d.toLocaleTimeString('zh-CN')
  return d.toLocaleString('zh-CN')
}
</script>

<template>
  <template v-if="mode === 'display'">
    <span>{{ formatDisplay(value) }}</span>
  </template>
  <template v-else-if="mode === 'search'">
    <NDatePicker
      v-if="item.format === 'date'"
      type="daterange"
      :value="value as [number, number] | null"
      clearable
      @update:value="emit('update:value', $event)"
    />
    <NDatePicker
      v-else-if="item.format === 'dateTime'"
      type="datetimerange"
      :value="value as [number, number] | null"
      clearable
      @update:value="emit('update:value', $event)"
    />
    <!-- time format is not searchable per spec -->
  </template>
  <template v-else>
    <NDatePicker
      v-if="item.format === 'date'"
      type="date"
      :value="value as number | null"
      clearable
      @update:value="emit('update:value', $event)"
    />
    <NDatePicker
      v-else-if="item.format === 'dateTime'"
      type="datetime"
      :value="value as number | null"
      clearable
      @update:value="emit('update:value', $event)"
    />
    <NTimePicker
      v-else
      :value="value as number | null"
      clearable
      @update:value="emit('update:value', $event)"
    />
  </template>
</template>
```

- [ ] **Step 6: Create OnOffField.vue**

```vue
<!-- src/core/fields/OnOffField.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { NSwitch, NSelect, NTag } from 'naive-ui'
import type { OnOffItemDef } from '@/schema/types'

const props = defineProps<{
  item: OnOffItemDef
  mode: 'search' | 'edit' | 'display'
  value: boolean | null
}>()

const emit = defineEmits<{
  'update:value': [val: boolean | null]
}>()

const searchOptions = [
  { label: '是', value: true },
  { label: '否', value: false }
]
</script>

<template>
  <template v-if="mode === 'display'">
    <NTag :type="value ? 'success' : 'default'" size="small">{{ value ? '是' : '否' }}</NTag>
  </template>
  <template v-else-if="mode === 'search'">
    <NSelect :value="value" :options="searchOptions" clearable placeholder="请选择" @update:value="emit('update:value', $event)" />
  </template>
  <template v-else>
    <NSwitch :value="value ?? false" @update:value="emit('update:value', $event)" />
  </template>
</template>
```

- [ ] **Step 7: Create SecretField.vue**

```vue
<!-- src/core/fields/SecretField.vue -->
<script setup lang="ts">
import { NInput } from 'naive-ui'
import type { SecretItemDef } from '@/schema/types'

const props = defineProps<{
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
  <!-- secret is not searchable -->
</template>
```

- [ ] **Step 8: Create FieldRenderer.vue**

```vue
<!-- src/core/fields/FieldRenderer.vue -->
<script setup lang="ts">
import type { ItemDef } from '@/schema/types'
import TextField from './TextField.vue'
import NumberField from './NumberField.vue'
import SelectField from './SelectField.vue'
import MultiSelectField from './MultiSelectField.vue'
import TimeField from './TimeField.vue'
import OnOffField from './OnOffField.vue'
import SecretField from './SecretField.vue'

const props = defineProps<{
  item: ItemDef
  mode: 'search' | 'edit' | 'display'
  value: any
}>()

const emit = defineEmits<{
  'update:value': [val: any]
}>()

const componentMap = {
  text: TextField,
  number: NumberField,
  select: SelectField,
  multiSelect: MultiSelectField,
  time: TimeField,
  onOff: OnOffField,
  secret: SecretField
} as const
</script>

<template>
  <component
    :is="componentMap[item.type]"
    :item="item"
    :mode="mode"
    :value="value"
    @update:value="emit('update:value', $event)"
  />
</template>
```

- [ ] **Step 9: Commit**

```bash
git add src/core/fields/
git commit -m "feat: implement field renderer components for all 7 DSL item types"
```

---

### Task 10: SearchForm Component

**Files:**
- Create: `src/core/SearchForm.vue`

- [ ] **Step 1: Implement SearchForm**

```vue
<!-- src/core/SearchForm.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { NForm, NFormItem, NButton, NSpace } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'

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
  props.items.filter(item => {
    if (item.type === 'secret') return false
    if (item.type === 'time' && item.format === 'time') return false
    return true
  })
)

function updateField(name: string, value: any) {
  emit('update:modelValue', { ...props.modelValue, [name]: value })
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
  <NForm inline label-placement="left" style="flex-wrap: wrap; gap: 12px 16px;">
    <NFormItem v-for="item in searchableItems" :key="item.name" :label="item.title">
      <FieldRenderer
        :item="item"
        mode="search"
        :value="modelValue[item.name] ?? null"
        @update:value="updateField(item.name, $event)"
      />
    </NFormItem>
    <NFormItem>
      <NSpace>
        <NButton type="primary" @click="emit('search')">查询</NButton>
        <NButton @click="handleReset">重置</NButton>
      </NSpace>
    </NFormItem>
  </NForm>
</template>
```

- [ ] **Step 2: Commit**

```bash
git add src/core/SearchForm.vue
git commit -m "feat: implement SearchForm component with auto field filtering"
```

---

### Task 11: DataTable Component

**Files:**
- Create: `src/core/DataTable.vue`

- [ ] **Step 1: Implement DataTable**

```vue
<!-- src/core/DataTable.vue -->
<script setup lang="ts">
import { computed, h } from 'vue'
import { NDataTable, NButton, NSpace, NPopconfirm } from 'naive-ui'
import type { DataTableColumn, PaginationProps } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'

const props = defineProps<{
  items: ItemDef[]
  data: Record<string, any>[]
  loading: boolean
  pagination: PaginationProps
}>()

const emit = defineEmits<{
  edit: [row: Record<string, any>]
  delete: [row: Record<string, any>]
  'update:pagination': [pagination: PaginationProps]
}>()

const columns = computed<DataTableColumn[]>(() => {
  const cols: DataTableColumn[] = props.items.map(item => ({
    title: item.title,
    key: item.name,
    render(row: Record<string, any>) {
      return h(FieldRenderer, {
        item,
        mode: 'display',
        value: row[item.name] ?? null
      })
    }
  }))

  cols.push({
    title: '操作',
    key: '_actions',
    width: 150,
    render(row: Record<string, any>) {
      return h(NSpace, null, () => [
        h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => emit('edit', row) }, () => '编辑'),
        h(NPopconfirm, { onPositiveClick: () => emit('delete', row) }, {
          trigger: () => h(NButton, { size: 'small', quaternary: true, type: 'error' }, () => '删除'),
          default: () => '确定要删除吗？'
        })
      ])
    }
  })

  return cols
})

function handlePageChange(page: number) {
  emit('update:pagination', { ...props.pagination, page })
}

function handlePageSizeChange(pageSize: number) {
  emit('update:pagination', { ...props.pagination, pageSize, page: 1 })
}
</script>

<template>
  <NDataTable
    :columns="columns"
    :data="data"
    :loading="loading"
    :pagination="pagination"
    remote
    @update:page="handlePageChange"
    @update:page-size="handlePageSizeChange"
  />
</template>
```

- [ ] **Step 2: Commit**

```bash
git add src/core/DataTable.vue
git commit -m "feat: implement DataTable component with dynamic columns from DSL"
```

---

### Task 12: EditModal Component

**Files:**
- Create: `src/core/EditModal.vue`

- [ ] **Step 1: Implement EditModal**

```vue
<!-- src/core/EditModal.vue -->
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { NModal, NCard, NForm, NFormItem, NButton, NSpace, type FormInst, type FormRules } from 'naive-ui'
import type { ItemDef } from '@/schema/types'
import FieldRenderer from './fields/FieldRenderer.vue'

const props = defineProps<{
  visible: boolean
  mode: 'create' | 'edit'
  items: ItemDef[]
  modelValue: Record<string, any>
  loading: boolean
}>()

const emit = defineEmits<{
  'update:visible': [val: boolean]
  'update:modelValue': [val: Record<string, any>]
  submit: []
}>()

const formRef = ref<FormInst | null>(null)

const visibleItems = computed(() =>
  props.items.filter(item => {
    const pattern = props.mode === 'create' ? item.canInputOnInit : item.canInputOnEdit
    return pattern !== 'doNot'
  })
)

const rules = computed<FormRules>(() => {
  const r: FormRules = {}
  for (const item of visibleItems.value) {
    if (item.isNonVoid) {
      r[item.name] = [{ required: true, message: `请输入${item.title}`, trigger: 'blur' }]
    }
  }
  return r
})

const title = computed(() => props.mode === 'create' ? '新建' : '编辑')

function updateField(name: string, value: any) {
  emit('update:modelValue', { ...props.modelValue, [name]: value })
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  emit('submit')
}

function handleClose() {
  emit('update:visible', false)
}
</script>

<template>
  <NModal :show="visible" @update:show="emit('update:visible', $event)">
    <NCard :title="title" style="width: 600px;" :bordered="false" closable @close="handleClose">
      <NForm ref="formRef" :model="modelValue" :rules="rules" label-placement="left" label-width="100px">
        <NFormItem v-for="item in visibleItems" :key="item.name" :label="item.title" :path="item.name">
          <FieldRenderer
            :item="item"
            mode="edit"
            :value="modelValue[item.name] ?? null"
            @update:value="updateField(item.name, $event)"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="handleClose">取消</NButton>
          <NButton type="primary" :loading="loading" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NCard>
  </NModal>
</template>
```

- [ ] **Step 2: Commit**

```bash
git add src/core/EditModal.vue
git commit -m "feat: implement EditModal with dynamic fields and validation from DSL"
```

---

### Task 13: CrudPage Orchestrator

**Files:**
- Create: `src/core/CrudPage.vue`

- [ ] **Step 1: Implement CrudPage**

```vue
<!-- src/core/CrudPage.vue -->
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { NCard, NButton, NSpace, useMessage } from 'naive-ui'
import type { PaginationProps } from 'naive-ui'
import type { FormDef } from '@/schema/types'
import { deriveApiBasePath } from '@/utils/naming'
import request from '@/utils/request'
import type { ApiBaseResult, PageResult } from '@/utils/request'
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'
import EditModal from './EditModal.vue'

const props = defineProps<{
  schema: FormDef
}>()

const message = useMessage()
const apiBase = deriveApiBasePath(props.schema.name)

const searchParams = ref<Record<string, any>>({})
const tableData = ref<Record<string, any>[]>([])
const tableLoading = ref(false)
const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const modalVisible = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const formData = ref<Record<string, any>>({})
const submitLoading = ref(false)

async function fetchData() {
  tableLoading.value = true
  try {
    const { data } = await request.post<ApiBaseResult<PageResult<Record<string, any>>>>(`${apiBase}/list`, {
      ...searchParams.value,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    })
    tableData.value = data.result.list
    pagination.itemCount = data.result.count
  } catch (e: any) {
    message.error(e.message || '查询失败')
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  pagination.page = 1
  fetchData()
}

function handlePaginationUpdate(p: PaginationProps) {
  pagination.page = p.page
  pagination.pageSize = p.pageSize
  fetchData()
}

function handleCreate() {
  modalMode.value = 'create'
  formData.value = {}
  modalVisible.value = true
}

function handleEdit(row: Record<string, any>) {
  modalMode.value = 'edit'
  formData.value = { ...row }
  modalVisible.value = true
}

async function handleDelete(row: Record<string, any>) {
  try {
    await request.post<ApiBaseResult>(`${apiBase}/delete`, { id: row.id })
    message.success('删除成功')
    fetchData()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await request.post<ApiBaseResult>(`${apiBase}/save`, formData.value)
    message.success(modalMode.value === 'create' ? '创建成功' : '更新成功')
    modalVisible.value = false
    fetchData()
  } catch (e: any) {
    message.error(e.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <NSpace vertical :size="16">
    <NCard>
      <SearchForm
        :items="schema.items"
        v-model="searchParams"
        @search="handleSearch"
        @reset="handleReset"
      />
    </NCard>
    <NCard>
      <template #header>
        <NSpace justify="space-between" align="center">
          <span>{{ schema.title }}</span>
          <NButton type="primary" @click="handleCreate">新建</NButton>
        </NSpace>
      </template>
      <DataTable
        :items="schema.items"
        :data="tableData"
        :loading="tableLoading"
        :pagination="pagination"
        @edit="handleEdit"
        @delete="handleDelete"
        @update:pagination="handlePaginationUpdate"
      />
    </NCard>
    <EditModal
      :visible="modalVisible"
      :mode="modalMode"
      :items="schema.items"
      v-model="formData"
      :loading="submitLoading"
      @update:visible="modalVisible = $event"
      @submit="handleSubmit"
    />
  </NSpace>
</template>
```

- [ ] **Step 2: Commit**

```bash
git add src/core/CrudPage.vue
git commit -m "feat: implement CrudPage orchestrator with search, table, modal, and API calls"
```

---

### Task 14: End-to-End Verification

**Files:**
- Modify: `vite.config.ts` (add API proxy for development)

- [ ] **Step 1: Add dev proxy to vite.config.ts**

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import formDslPlugin from './plugins/vite-plugin-form-dsl'

export default defineConfig({
  plugins: [
    vue(),
    formDslPlugin(path.resolve(__dirname, 'src/dsl'))
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5180,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  test: {
    environment: 'happy-dom'
  }
})
```

- [ ] **Step 2: Run all tests**

Run: `pnpm test`
Expected: All tests pass.

- [ ] **Step 3: Run type check**

Run: `pnpm run build`
Expected: `vue-tsc` passes. Vite builds successfully.

- [ ] **Step 4: Start dev server and verify UI in browser**

Run: `pnpm run dev`
Expected:
- `http://localhost:5180` → redirects to `/#/login`
- Login page renders with username/password form
- (Without backend: login will fail with network error — this is expected)
- If you temporarily set a token in localStorage, the dashboard renders with sidebar menu showing "学生管理 > 学生基本信息"
- The CRUD page shows search form with fields, empty table, and "新建" button

- [ ] **Step 5: Commit**

```bash
git add vite.config.ts
git commit -m "feat: add dev proxy and complete end-to-end wiring"
```

- [ ] **Step 6: Run final full test suite**

Run: `pnpm test -- --run`
Expected: All tests pass. Zero failures.

---

## Summary

| Task | Deliverable |
|------|------------|
| 1 | Project scaffolding (package.json, tsconfig, vite, entry files) |
| 2 | TypeScript type definitions for DSL schema |
| 3 | Naming utilities (route path, API path derivation) |
| 4 | Vite plugin (YML → virtual JSON module with HMR) |
| 5 | Auth store + request utility |
| 6 | Router with dynamic DSL route generation |
| 7 | Login page |
| 8 | Dashboard layout with auto-generated sidebar menu |
| 9 | All 7 field renderer components + dispatcher |
| 10 | SearchForm component |
| 11 | DataTable component |
| 12 | EditModal component |
| 13 | CrudPage orchestrator |
| 14 | End-to-end verification (proxy, type check, browser test) |
