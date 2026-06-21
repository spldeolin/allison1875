# 用户表格"已授予权限"列 & 角色菜单改名

## 概述

为用户列表页新增一列展示该用户已授予的权限信息（角色 Tag + hover 权限 Popover），并将侧边栏"角色"菜单改名为"角色权限"。

## 设计决策

| 决策项 | 结论 |
|--------|------|
| 数据加载方式 | 列表接口内联返回（`/api/v1/user/list` 响应中附带角色和权限） |
| 表格列展示 | NTag 标签展示角色名，最多 3 个，超出 +N |
| Hover Popover | 按 PermissionGroup 分组展示权限清单（只读） |
| 角色菜单改名 | builtin-form.yml title: "角色" → "角色权限" |

## 一、后端改动

### 1.1 ListUsersResp 新增字段

在后端骨架 `ListUsersResp.java`（或对应的 user list 响应 DTO）中新增：

```java
List<GrantedRoleBrief> grantedRoles;    // [{bizId, roleName}]
List<String> grantedPermissions;         // 取并集后的权限 code 列表
```

其中 `GrantedRoleBrief` 复用已有的 `RoleBriefResp` 结构（`bizId` + `roleName`）。

### 1.2 UserServiceImpl 查询逻辑

在 user list 查询后，批量加载所有返回用户的角色和权限：

```
1. 取当前页 user_ids
2. 批量查 user_role 表 → Map<userId, List<roleId>>
3. 批量查 role 表 → Map<roleId, roleName + bizId>
4. 批量查 role_permission 表 → Map<roleId, List<permissionCode>>
5. 对每个 user 聚合：
   - grantedRoles = 该用户的 roleId → [{bizId, roleName}]
   - grantedPermissions = 该用户所有 roleId 的 permissionCodes 取并集（去重）
```

使用批量查询（IN 子句），避免 N+1。

### 1.3 无需新 API

不新增接口，仅扩展现有 list 接口的响应字段。

## 二、前端改动

### 2.1 UserPage.vue — 新增自定义列

在 DataTable 后新增一个手动渲染的列（或通过 DataTable 的 slot/自定义列机制），位于操作列之前：

**列配置**：
- 列标题："已授予权限"
- 宽度：约 200px，不可排序

**单元格渲染**：
```
┌──────────────────────────────────────┐
│ [管理员] [编辑者] [+2]               │  ← NTag × 最多3个 + overflow tag
└──────────────────────────────────────┘
```

- 每个角色名渲染为 `NTag`（size=small, type=info）
- 最多展示 3 个 Tag，超出部分显示 `+N` 的灰色 Tag
- 无角色时显示灰色文字"暂无"

### 2.2 Hover Popover — 权限分组展示

鼠标悬停权限列单元格时，弹出 `NPopover`（trigger=hover），内容：

**数据准备**：
- 页面加载时调用 `POST /api/v1/permission/listPermissions` 获取权限树（含 group 信息和每个权限的 title）
- 缓存在组件 ref 中，所有行共用

**Popover 内容结构**：
```
标题：已授予权限（共 N 项）

[Group 1 名称]
  · 权限title1  · 权限title2  · 权限title3

[Group 2 名称]
  · 权限title4  · 权限title5
```

- 按 PermissionGroup 分组
- 每个权限显示其 title（中文名），不显示 code
- 只展示该用户实际拥有的权限（用 grantedPermissions 过滤权限树）
- 权限为空时 Popover 显示"暂无权限"

**样式要点**：
- Popover 最大宽度 360px，最大高度 400px（超出滚动）
- 组标题加粗，权限项用 `·` 分隔或 flex wrap 排列
- 整体紧凑，适合快速浏览

### 2.3 builtin-form.yml — 角色菜单改名

```yaml
# 改动前
title: 角色
desc: 角色

# 改动后
title: 角色权限
desc: 角色权限
```

影响范围：
- 侧边栏菜单项显示"角色权限"
- 表格页标题显示"角色权限"
- 创建/编辑弹框标题："创建角色权限" / "编辑角色权限"

## 三、DataTable 自定义列方案

当前 `DataTable.vue` 根据 `schema.items` 自动生成列。UserPage 需要在自动列之外额外插入一列。

方案：UserPage.vue 不使用 DataTable 的自动列，而是自行传入 `columns` prop（如果 DataTable 支持），或者在 DataTable 外自行构建 `NDataTable`。

查看现有 DataTable 组件是否支持自定义列 — 如果不支持，UserPage 可以直接使用 Naive UI 的 `NDataTable`（在覆盖页面中自由组合 UI，不受 core/ 约束）。

## 四、验收标准

1. 用户列表页显示"已授予权限"列，角色以 Tag 形式展示
2. 鼠标悬停该列时，Popover 按分组展示该用户的所有权限（中文名）
3. 侧边栏菜单"角色"已改为"角色权限"
4. 无角色用户显示"暂无"，Popover 显示"暂无权限"
5. 角色/权限数据随用户列表同步加载，无额外请求延迟
