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
