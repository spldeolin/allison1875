# 前端骨架二次开发规则

本项目由 app-generator 从 DSL 生成。所有 CRUD 页面默认由 `src/core/CrudPage.vue` 驱动。

## 核心原则

在 `src/pages/` 下创建覆盖文件定制页面，用 `useCrudPage` composable 复用标准逻辑，**不修改 core/ 下的文件**。

## 不可修改（会被重新生成覆盖）

- `src/core/**` — CrudPage、DataTable、EditModal、SearchForm、fields/、protocol/、composables/
- `src/app.json` — DSL 配置
- `src/router/index.ts` — 路由（自动发现 pages/）

## 二次开发方式

1. 创建 `src/pages/{FormName}Page.vue`（FormName = app.json 中 form.name，UpperCamel）
2. 路由自动生效，无需手动注册
3. 复制 `src/pages/TEMPLATE.vue.example` 作为起点
4. 用 `useCrudPage(props.schema)` 获取全部 CRUD 状态和方法
5. 在模板中组合 SearchForm / DataTable / EditModal，添加自定义内容

## 禁止事项

- 不要修改 `src/core/` 下的任何文件
- 不要修改 `src/app.json`
- 不要手动改 router 添加路由
- 不要跨页面 import 其他页面的逻辑
- 不要在 `src/pages/` 以外创建覆盖文件

## 是否需要创建覆盖文件？

| 需求                 |             覆盖？             |
|--------------------|:---------------------------:|
| 改 DSL 字段（名称、类型、选项） |        否，改 DSL 重新生成         |
| 工具栏/行操作加按钮         |              是              |
| 自定义弹框、抽屉           |              是              |
| 修改提交逻辑             |              是              |
| 页面级样式              |              是              |
| 调用非标准接口            |              是              |
| 全局样式               | 否，改 `src/styles/global.css` |
| 侧边栏/布局             |     否，改 `src/layouts/`      |

## 后端接口约定

- 全部 POST，路径 `/api/v1/{lowerCamelFormName}/{actionWithFormName}`
- 响应格式 `{ errorCode, data, errorMsg, traceId }`
- 使用 `src/utils/request.ts` 发请求

## Secret 字段编辑交互

`secret` 字段（密码/密钥）出于安全，detail 接口**不返回明文**——编辑弹框打开时该字段绑定值为 `null`。前后端约定三态提交协议（`null`/`""`/非空串），前端在 edit-update 下用「edit icon 进入编辑」的交互承载这套语义。涉及 `core/` 下多个文件，二次开发时若覆盖 SecretField 需保留以下协议。

### 提交语义（edit-update，`canInputOnEdit=true`）

| formData 值 | 含义 | 后端行为 |
|---|---|---|
| `null` | 未修改 | 跳过 setter（保留数据库原值） |
| `""` | 清空 | `isNonVoid=false` 存空串；`isNonVoid=true` 抛业务异常 |
| 非空串 | 覆盖 | setter 存新值 |

`canInputOnEdit=false` 的 secret 在 edit-update 下被 `protocol/field-policy.ts` 隐藏，不进入 SecretField 编辑分支。create 下 secret 仍必填、仍带 `@NotEmpty`。

### 涉及的 core/ 文件

- `protocol/field-policy.ts` — edit-update 下隐藏 `canInputOnEdit=false` 的 secret。
- `EditModal.vue` — edit-update 下 secret 不生成 required 规则（避免拦截 `null` 提交）；向 FieldRenderer 透传 `editMode`。
- `fields/FieldRenderer.vue` — 仅对 `item.type === 'secret'` 透传 `editMode`（其他字段不接收，避免 Vue 属性警告）。
- `fields/SecretField.vue` — 根据 `editMode` 区分 create / edit-update 渲染。

### SecretField edit-update 交互模型

默认**不可输入**：readonly NInput 显示 `••••••`（6 个可见圆点字符，非 password 掩码）+ suffix 的 edit icon（`@vicons/ionicons5` 的 `CreateOutline`）。点击 edit icon 进入可输入 password input，suffix 换成 close icon（`CloseOutline`）；输入新值 → 覆盖，clearable ✕（仅 `isNonVoid=false`）→ 清空为 `""`，点 close icon → 回到未修改态。

关键实现约束：

- **`internal` flag 守卫 `watch(() => props.value)`**。`props.value` 经父级 `v-model` 双向绑定，本组件 `emit` 会回流为新的 `props.value` 触发 watch。若不区分，首次输入即被 watch 重置 `editing=false`、字段退回黑点态。每个 handler（`startEdit`/`cancelEdit`/`onInput`）emit 前置 `internal=true`，watch 消费后跳过重置；仅外部重置（弹框重开等）才真正重置。
- **已知局限**：连续编辑两条 secret 值恰好相同的记录时 `props.value` 引用未变，watch 不触发、状态不重置。这是 watch-on-value 方案的固有限制，非缺陷；若需更强保证可改为按 modal-open 信号重置。
- 默认态黑点是字面量 `'••••••'`，不用 password 掩码或不可见 sentinel——避免 readonly password input 的浏览器怪异行为。

### 不改动的协议层

`protocol/request-builder.ts` 的 `buildUpdateRequest` 用 `v ?? null` 原样传三态，无需为 secret 特判。`response-parser.ts` 对 secret 是 no-op passthrough，detail 无该键时 `formData[name]` 为 `undefined`，模板 `?? null` 归一为 `null`。

## 功能权限体系

### 权限数据来源

`app.json` 中每个 menu 包含 `permissions` 对象（由 app-generator 注入）：

```json
{
    "list": "LIST_ORDER",
    "create": "CREATE_ORDER",
    "update": "UPDATE_ORDER",
    "delete": "DELETE_ORDER"
}
```

通过 router props 传入页面组件。

### v-permission 指令

模板中控制元素显隐：`<NButton v-permission="permissions?.create">`。值为空时元素始终可见，非空时检查 authStore 权限列表。

### checkPermission 函数

render 函数中使用（v-permission 无法在 `h()` 中使用）：

```typescript
import { checkPermission } from '@/directives/usePermission'
...(checkPermission(props.permissions?.update) ? [h(NButton, ...)] : [])
```

### DataTable extraActions

通过 `extraActions` prop 注入自定义行操作按钮，每个 action 可声明 `permission` 字段控制显隐。

### 路由守卫与菜单过滤

- `router/index.ts` beforeEach 检查 `permissions.list`，无权限时重定向
- `DashboardLayout.vue` 基于 LIST 权限过滤侧边栏菜单项
- `utils/request.ts` 拦截 403 响应返回 "没有操作权限"

## 已有的页面覆盖文件

| 文件             | 功能                        | 实现要点                                      |
|----------------|---------------------------|-------------------------------------------|
| `RolePage.vue` | 角色 CRUD + 授予权限弹框          | 独立 NCheckbox（非 NCheckboxGroup）+ baseOn 级联 |
| `UserPage.vue` | 用户 CRUD + 授予角色弹框 + 已授予权限列 | 直接用 NDataTable + NTag/NPopover 自定义列       |
| `AuditLogPage.vue` | 审计日志只读列表 | 自定义排序列 + operationType 映射 + content tooltip，不使用 DataTable 组件 |

## 文件组织

```
src/
├── directives/
│   ├── permission.ts          ← v-permission 指令
│   └── usePermission.ts       ← checkPermission 函数
├── pages/
│   ├── {FormName}Page.vue     ← 页面覆盖
│   ├── RolePage.vue           ← 内置：角色权限页
│   ├── UserPage.vue           ← 内置：用户页
│   ├── TEMPLATE.vue.example   ← 起始模板
│   └── components/            ← pages 内部复用的组件（可选）
├── stores/
│   └── auth.ts                ← 认证状态 + hasPermission()
└── ...
```
