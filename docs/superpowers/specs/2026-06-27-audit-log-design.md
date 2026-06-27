# 审计日志功能设计

## 概述

为 app-generator 生成的应用增加审计日志能力。所有表单（含内置表单，排除审计日志本身）的 create/update/delete 接口在操作成功或失败时均记录审计日志。

## 目标

1. 定义审计日志表单 DSL，加入 `builtin-form.yml`
2. 后端骨架新增 `AuditLogFacade`，提供统一的审计日志创建能力
3. 后端骨架新增 `AuditOperationTypeEnum`，包含内置操作类型
4. app-generator 在生成时追加非内置操作类型枚举项
5. app-generator 通过 `MutationExpansionService` 向生成的 ServiceImpl 注入审计日志调用代码
6. 前端骨架新增 `AuditLogPage.vue`，只读列表页面

## 审计日志字段

| 字段 | 类型 | 说明 |
|------|------|------|
| operationType | select (枚举) | 操作类型，由 AuditOperationTypeEnum 定义 |
| success | onOff (Boolean) | 操作是否成功 |
| content | text (TEXT) | 操作内容，JSON 格式字符串 |
| failReason | text (VARCHAR 512) | 失败原因，成功时为 null |
| createdAt | 内置 | 操作时间 |
| createdBy | 内置 | 操作人 |

注：无 updatedAt/updatedBy（只读不可修改）。

## 审计日志 DSL（builtin-form.yml 新增）

```yaml
- group: 系统
  icon: 1
  order: 3
  form:
    name: AuditLog
    title: 审计日志
    items:
      - type: select
        name: operationType
        title: 操作类型
        isNonVoid: true
        canInputOnInit: false
        canInputOnEdit: false
        options:
          - code: dummy1
            title: 占位1
          - code: dummy2
            title: 占位2

      - type: onOff
        name: success
        title: 是否成功
        isNonVoid: true
        canInputOnInit: false
        canInputOnEdit: false

      - type: text
        name: content
        title: 操作内容
        isNonVoid: false
        canInputOnInit: false
        canInputOnEdit: false
        isMultilineOrRich: true

      - type: text
        name: failReason
        title: 失败原因
        isNonVoid: false
        canInputOnInit: false
        canInputOnEdit: false
        maxLength: 512
```

DSL 用于初次生成代码。生成后用户手动复制 list 相关后端代码到骨架，删除 create/update/delete 相关代码及 updatedAt/updatedBy 字段。`operationType` 的 dummy options 后续手动替换。

## AuditOperationTypeEnum

### 骨架内置项

```java
@Getter
@AllArgsConstructor
public enum AuditOperationTypeEnum {

    // 认证相关
    LOGIN("login", "登录"),
    CHANGE_PASSWORD("changePassword", "修改密码"),
    LOGOUT("logout", "退出登录"),

    // 用户管理
    GRANT_ROLE("grantRole", "授予角色"),
    GRANT_PERMISSION("grantPermission", "授予权限"),
    CREATE_USER("createUser", "创建用户"),
    UPDATE_USER("updateUser", "编辑用户"),
    DELETE_USER("deleteUser", "删除用户"),

    // 角色管理
    CREATE_ROLE("createRole", "创建角色"),
    UPDATE_ROLE("updateRole", "编辑角色"),
    DELETE_ROLE("deleteRole", "删除角色"),

    // === 以下由 app-generator 生成 ===
    ;

    @JsonValue
    private final String code;

    private final String title;

    public static boolean valid(String code) {
        return Arrays.stream(values()).anyMatch(anEnum -> anEnum.getCode().equals(code));
    }

    @JsonCreator
    public static AuditOperationTypeEnum of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return code;
    }
}
```

### app-generator 追加的非内置项

每个用户定义的表单（排除 AuditLog 本身）生成 3 项：

```java
CREATE_ORDER("createOrder", "创建订单"),
UPDATE_ORDER("updateOrder", "编辑订单"),
DELETE_ORDER("deleteOrder", "删除订单"),
```

命名规则：`CREATE_` / `UPDATE_` / `DELETE_` + UPPER_SNAKE_CASE(formName)，code 为 `create` / `update` / `delete` + formName，title 为 `创建` / `编辑` / `删除` + form.title。

### 生成服务

新增 `AuditOperationTypeEnumGenerateService`（与 `PermissionEnumGenerateService` 平行），在 `AppGenerator.generateBackend()` 中骨架拷贝 + 占位符替换后、form-generator 委托前调用。

## AuditLogFacade

### 接口

```java
public interface AuditLogFacade {

    void logSuccess(AuditOperationTypeEnum operationType, Map<String, Object> content);

    void logFailure(AuditOperationTypeEnum operationType, Map<String, Object> content, String failReason);

    void logUpdateSuccess(AuditOperationTypeEnum operationType,
                          Map<String, Object> oldValues, Map<String, Object> newValues);

    void logUpdateFailure(AuditOperationTypeEnum operationType,
                          Map<String, Object> oldValues, Map<String, Object> newValues, String failReason);
}
```

### 实现要点

- `@Service` + `@Slf4j`
- 所有方法标注 `@Transactional(propagation = Propagation.REQUIRES_NEW)` — 独立事务，业务回滚不影响审计日志写入
- `logSuccess` / `logFailure`：将 content Map 序列化为 JSON 字符串（key 为字段 title，value 为字段值）
- `logUpdateSuccess` / `logUpdateFailure`：逐 key 对比 oldValues 与 newValues，仅保留有变更的字段。输出格式：`{"字段title": {"old": oldValue, "new": newValue}, ...}`。若无任何变更，content 为 `"无变更内容"`
- 构建 `AuditLogEntity`：`createdAt = LocalDateTime.now()`，`createdBy = CurrentUser.getUsername()`，`success`/`operationType`/`content`/`failReason` 按参数设置
- 调用 `auditLogMapper.insert(entity)` 持久化

### 操作内容格式

| 场景 | JSON 格式 |
|------|-----------|
| create / delete | `{"title1": value1, "title2": value2, ...}` |
| update（有变更） | `{"title1": {"old": oldVal, "new": newVal}, ...}` |
| update（无变更） | `"无变更内容"` |

### 内置表单调用点

| 调用位置 | 操作类型 | content Map 内容 |
|---------|---------|-----------------|
| AuthcServiceImpl.login 成功 | LOGIN | {"登录类型": "用户名密码登录"} |
| AuthcServiceImpl.login 失败 | LOGIN | {"登录类型": "用户名密码登录"}, failReason=异常消息 |
| AuthcServiceImpl.logout | LOGOUT | {"登录类型": "用户名密码登录"} |
| AuthcServiceImpl.updateSelfPassword | CHANGE_PASSWORD | null |
| UserServiceImpl.createUser | CREATE_USER | {"用户名": ..., "用户昵称": ...} |
| UserServiceImpl.updateUser | UPDATE_USER | oldValues/newValues 含用户名、用户昵称 |
| UserServiceImpl.deleteUser | DELETE_USER | {"用户名": ...} |
| RoleServiceImpl.createRole | CREATE_ROLE | {"角色ID": ..., "角色名称": ..., "角色描述": ...} |
| RoleServiceImpl.updateRole | UPDATE_ROLE | oldValues/newValues 含角色ID、角色名称、角色描述 |
| RoleServiceImpl.deleteRole | DELETE_ROLE | {"角色名称": ..., "角色ID": ...} |
| UserGrantServiceImpl.grantRoles | GRANT_ROLE | {"用户名": ..., "原先角色列表": [...], "新的角色列表": [...]} |
| RoleGrantServiceImpl.grantPermissions | GRANT_PERMISSION | {"用户名": ..., "原先权限列表": [...], "新的权限列表": [...]} |

## MutationExpansionService 扩展

### 接口变更

新增方法：

```java
void expandDeleteMethodBody(FormDef form, BlockStmt body);
```

默认实现（`FormGeneratorMutationExpansionServiceImpl`）为空方法体。

### form-generator 配合变更

`DeleteApiService` 的生成逻辑需暴露方法体的 `BlockStmt`，以便 `expandDeleteMethodBody` 注入代码（类似 Create/Update 的现有模式）。

### AppGeneratorMutationExpansionServiceImpl 扩展

构造函数新增 `List<FormDef> allForms` 参数，用于获取每个表单的字段信息。

#### Create 注入代码模式

```java
Map<String, Object> auditContent = new LinkedHashMap<>();
auditContent.put("订单Code", order.getOrderCode());
auditContent.put("订单号", req.getOrderNo());
auditContent.put("金额", req.getAmount());
// ... 所有非 secret 字段
try {
    // 原有业务逻辑
    orderMapper.insert(order);
    auditLogFacade.logSuccess(AuditOperationTypeEnum.CREATE_ORDER, auditContent);
} catch (BizException e) {
    auditLogFacade.logFailure(AuditOperationTypeEnum.CREATE_ORDER, auditContent, e.getMessage());
    throw e;
}
```

#### Update 注入代码模式

```java
Map<String, Object> oldValues = new LinkedHashMap<>();
oldValues.put("订单号", order.getOrderNo());
oldValues.put("金额", order.getAmount());
// ... 所有非 secret 字段

Map<String, Object> newValues = new LinkedHashMap<>();
newValues.put("订单号", req.getOrderNo());
newValues.put("金额", req.getAmount());
// ...

try {
    // 原有 update 逻辑
    orderMapper.updateById(order);
    auditLogFacade.logUpdateSuccess(AuditOperationTypeEnum.UPDATE_ORDER, oldValues, newValues);
} catch (BizException e) {
    auditLogFacade.logUpdateFailure(AuditOperationTypeEnum.UPDATE_ORDER, oldValues, newValues, e.getMessage());
    throw e;
}
```

#### Delete 注入代码模式

```java
Map<String, Object> auditContent = new LinkedHashMap<>();
auditContent.put("订单Code", req.getOrderCode());
try {
    orderMapper.deleteByOrderCode(req.getOrderCode());
    auditLogFacade.logSuccess(AuditOperationTypeEnum.DELETE_ORDER, auditContent);
} catch (BizException e) {
    auditLogFacade.logFailure(AuditOperationTypeEnum.DELETE_ORDER, auditContent, e.getMessage());
    throw e;
}
```

#### 关键规则

- 排除 `form.getName().equals("AuditLog")` — 审计日志表单不记录自身
- Map key 使用 item 的 `title`（中文）
- 排除 `type=secret` 的字段
- Create 时 value 来自 req（请求 DTO），额外加入 bizKey 作为第一个字段
- Update 时 oldValues 来自旧实体 getter，newValues 来自 req getter
- Delete 时 content 仅包含 bizKey

### ServiceImpl 字段注入

生成的 ServiceImpl 需要 `@Resource private AuditLogFacade auditLogFacade;` 字段。通过在 `expandCreateMethodBody`（首次被调用时）中 AST 操作追加字段声明实现。

## app-generator 集成流程

### generateBackend() 执行顺序

```
1. 复制 backend-skeleton
2. 替换占位符
3. 生成 PermissionEnum（追加非内置项）
4. 生成 AuditOperationTypeEnum（追加非内置项）   ← 新增
5. 调用 form-generator 委托（含 MutationExpansionService 覆盖）
6. 标注 @WebApiAuth
```

### invokeFormGenerator() Guice 覆盖

```java
boolean hasAuditLogForm = parseBuiltinMenus().stream()
        .anyMatch(menu -> "AuditLog".equals(menu.getForm().getName()));

// 当 hasAuditLogForm 为 true 时，MutationExpansionService 实现中注入审计日志调用代码
// AppGeneratorMutationExpansionServiceImpl 构造函数接收 namespace + allForms
```

条件检测逻辑与现有 `hasUserForm` 类似。两个条件可合并为同一个 Guice override 模块。

## 权限

### PermissionEnum 新增内置项

```java
LIST_AUDIT_LOG("LIST_AUDIT_LOG", "查看审计日志", Group.AUDIT_LOG, null),
```

`Group` 内部枚举新增 `AUDIT_LOG`。

### UserPermissionInitializer

- `SYSTEM_GROUPS` 新增 `"AUDIT_LOG"`
- `LIST_AUDIT_LOG` 默认授予"系统管理员"角色

### 后端

骨架中 AuditLog 的 list Controller 方法标注 `@WebApiAuth(PermissionEnum.LIST_AUDIT_LOG)`。

### 前端

- app.json 中 AuditLog 菜单 permissions：`{ "list": "LIST_AUDIT_LOG" }`（仅 list）
- 路由守卫和侧边栏菜单过滤基于此权限控制可见性

## 前端 AuditLogPage.vue

骨架中新增 `frontend-skeleton/src/pages/AuditLogPage.vue` 覆盖文件：

- 只展示列表（SearchForm + DataTable）
- 无"新增"按钮、无行编辑/删除操作
- 操作内容列直接展示（JSON 格式化或 tooltip 展示长文本）
- 标准筛选行为（根据字段类型自动推断）

## 变更范围总结

| 变更位置 | 变更内容 |
|---------|---------|
| form-generator | `MutationExpansionService` 新增 `expandDeleteMethodBody`；`DeleteApiService` 暴露 BlockStmt |
| app-generator 代码 | 新增 `AuditOperationTypeEnumGenerateService`；`AppGeneratorMutationExpansionServiceImpl` 扩展审计日志注入；`AppGenerator.generateBackend()` 增加枚举生成步骤 |
| backend-skeleton | 新增 `AuditOperationTypeEnum`、`AuditLogFacade` + Impl、AuditLog Entity/Mapper/Service(list)/Controller；`PermissionEnum` 新增 `LIST_AUDIT_LOG`；`UserPermissionInitializer` 更新；内置 ServiceImpl 手写审计日志调用 |
| frontend-skeleton | `builtin-form.yml` 新增 AuditLog；新增 `AuditLogPage.vue` |

## 不涉及变更

- form-generator 的 List/GetDetail API Service
- 前端 `src/core/` 目录
- 现有 `CommonItemsExpansionService`（createdBy/updatedBy 逻辑不变）
