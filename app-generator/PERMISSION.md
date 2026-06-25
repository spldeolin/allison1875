# 功能权限体系

app-generator 为生成的应用内置完整的 RBAC 权限能力，分三层：权限定义、授权、鉴权。

## 权限枚举生成（PermissionEnumGenerateService）

- 接口：`PermissionEnumGenerateService.generatePermissionEnum(List<FormDef>, Path, String)`
- 实现：字符串拼接构造完整 Java 源文件（非 AST），覆盖骨架中的空壳 `PermissionEnum.java`
- 每个表单生成 4 个权限点：`LIST_X`, `CREATE_X`, `UPDATE_X`, `DELETE_X`
- Role 表单额外生成 `GRANT_PERMISSION` 和 `GRANT_ROLE`
- 内部枚举 `Group`：每个表单一个 group，title 格式 `"{form.title}管理"`
- `baseOn` 关系：`LIST_X` 为 null，其余指向 `LIST_X`
- 命名规则：表单名 UpperCamel → UPPER_SNAKE（`UserProfile` → `USER_PROFILE`）

## app.json permissions 注入

`generateFrontend()` 中为每个 menu 注入 `permissions` 对象：

```json
{
    "list": "LIST_ORDER",
    "create": "CREATE_ORDER",
    "update": "UPDATE_ORDER",
    "delete": "DELETE_ORDER"
}
```

通过 `MenuDef.Permissions` 内部类承载，Jackson 序列化到 `app.json`。

## form-generator 生成的 Controller 权限注解

form-generator 生成的 Controller 方法自动添加 `@WebApiAuth` 注解：

- `save{Form}` → `@WebApiAuth({CREATE_X, UPDATE_X})`（OR 语义：拥有任一即通过）
- `list{Form}s` / `get{Form}Detail` → `@WebApiAuth(LIST_X)`
- `delete{Form}` → `@WebApiAuth(DELETE_X)`

## 后端骨架权限基础设施

| 组件                            | 位置            | 职责                            |
|-------------------------------|---------------|-------------------------------|
| `PermissionEnum`              | `enums/`      | 权限点枚举（由 app-generator 填充）     |
| `PermissionController`        | `controller/` | 查询全部权限点（按 group 分组）           |
| `@WebApiAuth`                 | `annotation/` | 方法级注解，声明所需权限                  |
| `UserPermissionInitializer`   | `task/`       | 启动引导 admin 用户和初始角色 + 扫描 Controller 建立 path→权限映射 |
| `ApiAuthFilter`               | `filter/`     | 认证 + 鉴权                       |
| `role_permission` 表           | `db/ddl.sql`  | 角色-权限多对多                      |
| `user_role` 表                 | `db/ddl.sql`  | 用户-角色多对多                      |

### 系统初始化

`UserPermissionInitializer` 启动时确保：

- admin 用户存在（密码从配置读取）
- 3 个初始角色：系统管理员（全部权限）、业务员（非系统分组全部权限）、观察员（非系统分组 LIST 权限）
- admin 绑定系统管理员角色；新用户自动绑定观察员角色
- 系统分组通过硬编码 `SYSTEM_GROUPS = Set.of("USER", "ROLE")` 识别

### 授权 API（后端骨架内置）

| 接口                                        | 用途           |
|-------------------------------------------|--------------|
| `POST /api/v1/role/grantPermissions`      | 全量覆盖式为角色授予权限 |
| `POST /api/v1/role/listRolePermissions`   | 查询角色已有权限     |
| `POST /api/v1/user/grantRoles`            | 全量覆盖式为用户授予角色 |
| `POST /api/v1/user/listUserRoles`         | 查询用户已有角色     |
| `POST /api/v1/permission/listPermissions` | 查询全部权限点      |

## 前端骨架权限基础设施

| 组件                  | 位置                            | 职责                           |
|---------------------|-------------------------------|------------------------------|
| `v-permission`      | `directives/permission.ts`    | 指令，控制元素显隐                    |
| `checkPermission()` | `directives/usePermission.ts` | 函数，用于 render 函数中             |
| 路由守卫                | `router/index.ts`             | 检查 `permissions.list` 控制页面访问 |
| 菜单过滤                | `DashboardLayout.vue`         | 基于 LIST 权限过滤侧边栏              |
| `RolePage.vue`      | `pages/`                      | 角色 CRUD + 授予权限弹框（baseOn 级联）  |
| `UserPage.vue`      | `pages/`                      | 用户 CRUD + 授予角色弹框 + 已授予权限列    |

### RolePage 授予权限弹框

- 使用独立 `NCheckbox`（非 `NCheckboxGroup`）避免组头 checkbox 显示 bug
- `handlePermissionCheck()` 实现 baseOn 级联：勾选子权限自动勾选父，取消父自动取消子

### UserPage "已授予权限"列

- 直接使用 `NDataTable` 替代 core DataTable，以支持完全自定义列
- 角色以 NTag 展示（最多 3 个 + overflow），hover 弹出 NPopover 按 group 展示权限详情
- 后端 listUsers 接口内联返回 `grantedRoles` + `grantedPermissions`（批量 JOIN，无 N+1）

## 关键约束

1. **生成时机**：权限枚举生成必须在骨架拷贝之后、form-generator 委托之前（Controller 引用 PermissionEnum）
2. **字符串拼接而非 AST**：因为输出目录不是已有 Maven 项目
3. **builtin 菜单 order 覆盖**：合并后设为 100000+，否则系统菜单可能出现在用户菜单之前
4. **OR 语义**：`@WebApiAuth` 多值为 OR（拥有任一即通过），用于 save 接口
5. **不要在 PermissionEnum.Group 上加字段区分系统分组**：用 UserPermissionInitializer 硬编码常量
