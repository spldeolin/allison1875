# form-web 设计规格

## 概述

form-web 是一个基于 Vue 3 + Naive UI 的前端项目，能够读取 YML 格式的 Form DSL，自动渲染完整的 CRUD 页面（搜索栏 + 数据表格 + 新建/编辑弹窗 + 删除操作）。

DSL 格式兼容 Allison1875 form-generator 的定义，并在前端层面做克制的扩展。

## 核心原则

- **MVP satisficing**：完全实现表单渲染，架构不提前实现扩展点，保留可扩展性即可
- **Schema-Driven Renderer**：YML 构建时转 JSON，运行时由渲染引擎动态渲染
- **约定优于配置**：字段类型自动推断控件，API 路径自动推导
- **固定依赖版本**：package.json 中所有依赖使用精确版本号

## 技术栈

| 项目 | 选型 |
|------|------|
| 框架 | Vue 3 (Composition API + `<script setup>`) |
| UI 库 | Naive UI |
| 构建工具 | Vite |
| 语言 | TypeScript |
| 路由 | Vue Router (Hash mode) |
| 状态管理 | Pinia（仅 auth） |
| HTTP | Axios |
| 包管理 | pnpm |
| DSL 解析 | js-yaml（Vite 插件中使用） |

## 项目结构

```
form-web/
├── src/
│   ├── dsl/                 # *.yml 文件存放目录（Vite 构建时扫描）
│   ├── core/                # 核心渲染组件
│   │   ├── CrudPage.vue     # 完整 CRUD 页面编排组件
│   │   ├── SearchForm.vue   # 搜索栏
│   │   ├── DataTable.vue    # 数据表格
│   │   ├── EditModal.vue    # 新建/编辑弹窗
│   │   └── fields/          # 字段渲染器（按类型）
│   │       ├── FieldRenderer.vue
│   │       ├── TextField.vue
│   │       ├── NumberField.vue
│   │       ├── SelectField.vue
│   │       ├── MultiSelectField.vue
│   │       ├── TimeField.vue
│   │       ├── OnOffField.vue
│   │       └── SecretField.vue
│   ├── schema/              # DSL 类型定义 & 默认值推断
│   ├── layouts/             # DashboardLayout
│   ├── views/               # Login 等非 DSL 页面
│   ├── stores/              # auth store（不透明 token）
│   ├── router/              # 动态路由（基于 DSL 生成）
│   ├── utils/               # request.ts
│   └── main.ts
├── plugins/                 # Vite 插件：YML→JSON 转换
├── vite.config.ts
├── package.json
└── tsconfig.json
```

## 数据流

```
YML 文件 (src/dsl/*.yml)
    ↓ Vite 插件（构建时）
JSON Schema（virtual:form-dsl ES module）
    ↓ router/index.ts
动态路由生成（Hash mode）
    ↓
CrudPage.vue（接收 schema prop）
    ↓ 分发给子组件
SearchForm / DataTable / EditModal
    ↓ FieldRenderer 根据 type 分发
Naive UI 控件
```

## DSL Schema

基于 Allison1875 form-generator DSL，前端仅扩展 3 个字段：

```yaml
forms:
  - name: StudentBasicInfo       # UpperCamel，必填
    title: 学生基本信息            # 必填，页面标题
    desc: 学生身份信息管理          # 可选
    group: 学生管理               # [前端扩展] 菜单分组
    icon: PersonOutline           # [前端扩展] 菜单图标（vicons/ionicons5）
    order: 1                     # [前端扩展] 菜单排序权重
    items:
      - type: text               # text|number|select|multiSelect|time|onOff|secret
        name: studentName        # lowerCamel，必填
        title: 学生姓名           # 必填，字段标签
        isNonVoid: true          # 是否必填
        initPattern: userInput   # 新建时：doNot|userInput|todo
        editPattern: userInput   # 编辑时：doNot|userInput|todo
        maxLength: 50            # text 专属
        # isMultilineOrRich: false  # text 专属
        # regex: ''                 # text 专属
      - type: number
        name: studentId
        title: 学号
        isNonVoid: true
        canBeDecimal: false
      - type: select
        name: grade
        title: 年级
        isNonVoid: true
        options:
          - code: grade1
            title: 一年级
          - code: grade2
            title: 二年级
      - type: time
        name: enrollmentDate
        title: 入学日期
        isNonVoid: true
        format: date             # date|time|dateTime
    indices:
      - itemNames: [studentId]
        isUnique: true
```

## 字段类型 → 控件映射

> **注意：** 此映射为设计阶段的合理推断。实际实现时需对齐 Allison1875 生成的后端接口（请求参数结构、响应格式），映射规则可能调整。

| DSL 类型 | 表格列 | 搜索栏 | 编辑表单 |
|---------|--------|--------|---------|
| text | 文本显示 | NInput（模糊搜索） | NInput / NInput[textarea]（multiline） |
| number | 数字显示 | NInputNumber（范围） | NInputNumber |
| select | 显示 option title | NSelect（筛选） | NSelect |
| multiSelect | tag 列表 | NSelect[multiple] | NSelect[multiple] |
| time(date) | yyyy-MM-dd | NDatePicker[daterange] | NDatePicker[date] |
| time(dateTime) | yyyy-MM-dd HH:mm:ss | NDatePicker[datetimerange] | NDatePicker[datetime] |
| time(time) | HH:mm:ss | 不可搜索 | NTimePicker |
| onOff | NTag(是/否) | NSelect(是/否) | NSwitch |
| secret | 脱敏(***) | 不可搜索 | NInput[password] |

**initPattern / editPattern 对编辑表单的影响：**
- `userInput`：正常显示可编辑字段
- `doNot`：不显示该字段
- `todo`：MVP 阶段等同 `userInput`

## Vite 插件

**职责：**
1. 扫描 `src/dsl/*.yml`，用 js-yaml 解析为 JSON
2. 提供虚拟模块 `virtual:form-dsl`，导出所有 FormDef 的 JSON 数组
3. 开发模式监听 YML 文件变化，触发 HMR

## 路由

- 采用 Hash mode（`createWebHashHistory`）
- 从 `virtual:form-dsl` 导入所有 FormDef
- 每个 FormDef 自动生成路由：`path` 由 name 推导（UpperCamel → kebab-case）
- 路由 meta 携带 title、group、icon、order

```typescript
import formDefs from 'virtual:form-dsl'
import CrudPage from '@/core/CrudPage.vue'

const dslRoutes = formDefs.map(def => ({
  path: `/${upperCamelToKebab(def.name)}`,
  component: CrudPage,
  props: { schema: def },
  meta: { title: def.title, group: def.group, icon: def.icon, order: def.order }
}))
```

## 菜单

从路由 meta 中提取 group/icon/order，按 group 分组、order 升序排列，渲染到侧边栏。

## 核心组件

### CrudPage.vue

接收 `schema: FormDef` prop，管理页面状态：
- searchParams（搜索参数）
- tableData + pagination（表格数据与分页）
- modalVisible + modalMode（弹窗状态：create/edit）
- currentRecord（当前编辑的记录）

子组件通过 props 接收配置，通过 emit 上报事件。

### SearchForm.vue

- 从 schema.items 中筛选可搜索字段（排除 secret、time[time]）
- 使用 NForm inline 布局
- 提供查询和重置按钮

### DataTable.vue

- 从 schema.items 生成列定义
- 支持分页
- 操作列：编辑、删除按钮

### EditModal.vue

- 根据 modalMode 过滤字段（initPattern/editPattern 为 doNot 的不显示）
- 字段验证：isNonVoid 的字段设为 required
- 提交时调用约定的 save API

### FieldRenderer.vue

根据 `item.type` 和 `mode`（search/edit/display）分发到对应字段组件。

## Auth 模块

- `stores/auth.ts`：Pinia store，管理 token、用户信息、权限列表
- Token：不透明高熵随机密钥，存储在 localStorage
- `utils/request.ts`：axios 拦截器从 auth store 读取 token 附加到 header；401 自动跳转登录
- `views/Login.vue`：用户名 + 密码登录
- 权限控制：菜单项根据权限过滤显示，MVP 不做按钮级控制
- 解耦方式：其他模块只依赖 auth store 暴露的接口（getToken、getUserInfo、hasPermission、logout）

## API 对接

- 路径推导规则来源于 Allison1875 后端代码生成逻辑（实现时确认具体规则）
- 请求格式：POST，Content-Type: application/json
- 响应结构：`{ code: number, msg: string, result: T }`
- 分页响应：`result: { count: number, list: T[] }`
- 四种操作：list（分页查询）、save（新建/编辑）、getDetail（详情）、delete（删除）

## 待对齐事项

详见 `docs/allison1875-sync-items.md`：
1. DSL 新增 group/icon/order 字段
2. API 路径推导规则确认
3. 后端接口请求/响应结构确认
4. FilterPattern 与 ItemDef 的关联方式确认
