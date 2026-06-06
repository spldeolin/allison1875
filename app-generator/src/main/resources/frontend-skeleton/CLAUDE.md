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

## 文件组织

```
src/pages/
├── {FormName}Page.vue         ← 页面覆盖
├── TEMPLATE.vue.example       ← 起始模板（复制后重命名）
└── components/                ← pages 内部复用的组件（可选）
```
