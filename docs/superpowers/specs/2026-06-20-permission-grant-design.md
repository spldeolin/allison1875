# 功能权限 — 授权模块设计

## 概述

为 app-generator 生成的应用实现 RBAC 授权能力。本文档覆盖第二阶段"授权"模块，包括：
1. 角色表单（Role CRUD）
2. 角色-权限授予
3. 用户-角色授予
4. 授权管理前端交互

前置依赖：权限定义模块（已完成，见 `2026-06-20-permission-definition-design.md`）。

## 设计决策

| 决策项 | 结论 |
|--------|------|
| 角色表单字段 | 极简：roleName + description，仅两个业务字段 |
| 关联表设计 | 独立 role_permission、user_role 表，不走 form DSL multiSelect |
| 授权 API 风格 | 全量覆盖（grant = 删旧 + 批量插新） |
| 权限分配入口 | 角色列表行操作按钮"授予权限" → 居中弹框 |
| 角色分配入口 | 用户列表行操作按钮"授予角色" → 居中弹框 |
| 命名风格 | grant（非 assign） |
| 角色表单位置 | builtin-form.yml 内置，与 User 同属"系统"分组 |

## 一、Role 表单 DSL

### builtin-form.yml 新增项

```yaml
  - group: 系统
    icon: 1
    order: 2
    form:
      name: Role
      title: 角色
      desc: 角色
      items:
        - type: text
          name: roleName
          title: 角色名称
          isNonVoid: true
          maxLength: 32
          canInputOnInit: true
          canInputOnEdit: true

        - type: text
          name: description
          title: 角色描述
          isNonVoid: false
          maxLength: 128
          canInputOnInit: true
          canInputOnEdit: true
      indices:
        - itemNames: [ roleName ]
          isUnique: true
```

### 自动生成的权限点

- `LIST_ROLE`, `CREATE_ROLE`, `UPDATE_ROLE`, `DELETE_ROLE` — 标准 CRUD 权限
- `GRANT_PERMISSION` — 授予权限操作，`baseOn: LIST_ROLE`
- `GRANT_ROLE` — 授予角色操作，`baseOn: LIST_ROLE`
- 均归属 `Group.ROLE("ROLE", "角色管理")`

### 验收流程

1. 将 Role DSL 写入 app.yml 的 menus 中
2. 通过 app-generator 生成应用
3. 验证 Role CRUD 功能正常（创建、列表、编辑、删除）
4. 验收通过后，将 Role DSL 移入 `builtin-form.yml`
5. 后端 Role 相关代码手动移入后端骨架

## 二、数据库表（后端骨架）

### role_permission

```sql
CREATE TABLE role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_code VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_role_permission (role_id, permission_code)
);
```

### user_role

```sql
CREATE TABLE user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_user_role (user_id, role_id)
);
```

### 设计要点

- 使用数据库自增 id 作为主键
- 关联字段存储对应实体的数据库 id（非 bizId），外键约束通过唯一索引保证
- `permission_code` 存储枚举 code 字符串，允许枚举扩展而不影响关联表结构
- DDL 文件位于后端骨架 `src/main/resources/db/` 目录

## 三、后端 API

### 角色-权限授予

```
POST /api/v1/role/grantPermissions
Request:  { roleBizId: String, permissionCodes: List<String> }
Response: RequestResult<Void>
```

实现逻辑：
1. 根据 roleBizId 查找 role 记录，不存在则抛 BizException
2. 校验 permissionCodes 中每个 code 在 PermissionEnum 中存在
3. 事务内：删除该 role_id 的所有 role_permission 记录 → 批量插入新记录
4. 权限点：`GRANT_PERMISSION`

### 查询角色已有权限

```
POST /api/v1/role/listRolePermissions
Request:  { roleBizId: String }
Response: RequestResult<List<String>>  // permissionCode 列表
```

### 用户-角色授予

```
POST /api/v1/user/grantRoles
Request:  { userBizId: String, roleBizIds: List<String> }
Response: RequestResult<Void>
```

实现逻辑：
1. 根据 userBizId 查找 user 记录，不存在则抛 BizException
2. 根据 roleBizIds 查找 role 记录，任一不存在则抛 BizException
3. 事务内：删除该 user_id 的所有 user_role 记录 → 批量插入新记录
4. 权限点：`GRANT_ROLE`

### 查询用户已有角色

```
POST /api/v1/user/listUserRoles
Request:  { userBizId: String }
Response: RequestResult<List<RoleBriefResp>>  // [{bizId, roleName}]
```

### DTO

```java
// dto/req/GrantPermissionsReq.java
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GrantPermissionsReq {
    @NotBlank
    String roleBizId;
    @NotNull
    List<String> permissionCodes;
}

// dto/req/ListRolePermissionsReq.java
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListRolePermissionsReq {
    @NotBlank
    String roleBizId;
}

// dto/req/GrantRolesReq.java
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GrantRolesReq {
    @NotBlank
    String userBizId;
    @NotNull
    List<String> roleBizIds;
}

// dto/req/ListUserRolesReq.java
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListUserRolesReq {
    @NotBlank
    String userBizId;
}

// dto/resp/RoleBriefResp.java
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleBriefResp {
    String bizId;
    String roleName;
}
```

## 四、前端交互

### 4.1 角色列表页 — 授予权限弹框

**入口**：Role 列表行操作栏新增"授予权限"按钮，携带 `func-permission="GRANT_PERMISSION"`

**弹框内容**：
- 标题："授予权限 — {roleName}"
- 按 PermissionGroup 分组展示
- 每组：组标题 + 全选 checkbox + 各权限项 checkbox
- 底部：取消 / 确定按钮

**交互流程**：
1. 点击"授予权限" → 调用 `listRolePermissions` 获取已有权限 + `listPermissions` 获取权限树
2. 回填勾选状态
3. 用户勾选/取消 → 点击确定 → 调用 `grantPermissions`
4. 成功后关闭弹框，提示成功

### 4.2 用户列表页 — 授予角色弹框

**入口**：User 列表行操作栏新增"授予角色"按钮，携带 `func-permission="GRANT_ROLE"`

**弹框内容**：
- 标题："授予角色 — {username}"
- 平铺展示全部角色 checkbox 列表（roleName）
- 底部：取消 / 确定按钮

**交互流程**：
1. 点击"授予角色" → 调用 `listUserRoles` 获取已有角色 + Role list 接口获取全部角色
2. 回填勾选状态
3. 用户勾选/取消 → 点击确定 → 调用 `grantRoles`
4. 成功后关闭弹框，提示成功

### 4.3 实现方式

两个页面覆盖文件放入前端骨架：
- `src/pages/RolePage.vue` — 复用 useCrudPage + 授予权限弹框
- `src/pages/UserPage.vue` — 复用 useCrudPage + 授予角色弹框

### 4.4 getCurrentUser permissions 来源变更

`AuthcServiceImpl.getCurrentUser()` 中 permissions 字段改为从关联表查询：
```
当前用户 user_id → user_role → role_ids → role_permission → permission_codes（聚合去重）
```

无角色时返回空列表（无任何权限）。

## 五、权限枚举生成调整

`PermissionEnumGenerateService` 需额外生成 `GRANT_PERMISSION` 和 `GRANT_ROLE` 两个权限点：

```java
GRANT_PERMISSION("GRANT_PERMISSION", "授予权限", Group.ROLE, LIST_ROLE),
GRANT_ROLE("GRANT_ROLE", "授予角色", Group.ROLE, LIST_ROLE),
```

这两个权限点固定存在（不依赖具体表单），在枚举生成时追加到 ROLE group 末尾。

## 六、实施步骤概览

1. **验收 Role 表单**：写入 app.yml → 生成 → 验证 CRUD → 通过后移入 builtin-form.yml + 骨架
2. **后端骨架扩展**：新增 DDL、Entity、Mapper、Controller、Service、DTO
3. **前端骨架扩展**：新增 RolePage.vue、UserPage.vue 覆盖文件
4. **权限枚举生成调整**：追加 GRANT_PERMISSION、GRANT_ROLE
5. **getCurrentUser 改造**：从关联表聚合权限列表
6. **端到端验证**：生成应用 → 完整 RBAC 授权流程可用

## 七、里程碑提示词

授权模块完成后，为后续"鉴权模块"会话提供上下文：

---

## 已完成：授权模块

### 后端
- Role 表单内置于 builtin-form.yml，与 User 同属"系统"分组
- `role_permission` 表：角色-权限多对多关联
- `user_role` 表：用户-角色多对多关联
- `POST /api/v1/role/grantPermissions` — 全量覆盖式授予权限
- `POST /api/v1/role/listRolePermissions` — 查询角色已有权限
- `POST /api/v1/user/grantRoles` — 全量覆盖式授予角色
- `POST /api/v1/user/listUserRoles` — 查询用户已有角色
- 权限点：GRANT_PERMISSION、GRANT_ROLE，归属 Group.ROLE
- getCurrentUser 从 user_role → role_permission 聚合返回 permissions

### 前端
- RolePage.vue 覆盖：标准 CRUD + "授予权限"弹框（分组 checkbox）
- UserPage.vue 覆盖：标准 CRUD + "授予角色"弹框（平铺 checkbox）
- 按钮通过 func-permission 声明权限点

### 下一步：鉴权模块
需要实现：
1. 后端接口鉴权拦截器（基于当前用户角色→权限判断）
2. 前端 v-permission 指令（基于 func-permission + 当前用户权限列表控制按钮显隐）
3. 前端路由守卫（基于 permissions.list 控制菜单可见性）— 已部分实现于 DashboardLayout
