# App-Generator — AI Coding 规约

## 概述

从 `app.yml` DSL 生成前后端一体的 fullstack jar 项目。输出包含：

- `{name}-backend/` — Spring Boot 后端（骨架 + form-generator 生成的 CRUD）
- `{name}-frontend/` — Vue 3 前端（CRUD 页面由 `app.json` 驱动）
- `README.md` — Quick Start 与 DSL 原文

## 处理流程

1. 解析 `app.yml` → `AppDef`
2. 确定输出目录（重名自动追加序号）
3. 复制 `backend-skeleton`，替换占位符（`__NAMESPACE__`、`__APP_NAME__` 等）
4. **生成权限枚举**（`permissionEnumGenerateService.generatePermissionEnum()`）— 填充骨架中的空壳 `PermissionEnum.java`
5. **生成审计操作类型枚举**（`auditOperationTypeEnumGenerateService.generate()`）— 为每个非 AuditLog 表单追加 CREATE/UPDATE/DELETE 枚举项
6. 调用 `form-generator`（`Allison1875.letsGo(ToolEnum.FORM_GENERATOR, ...)`）生成 CRUD 代码
7. 复制 `frontend-skeleton`，合并 `builtin-form.yml` 内置菜单，**注入 permissions 映射**，写入 `app.json`
8. **覆盖 builtin 菜单 order 为 100000+**（确保系统菜单排在侧边栏最后）
9. 生成 `README.md`

## Config 字段

| 字段                      | 类型     | 默认值         | 说明              |
|-------------------------|--------|-------------|-----------------|
| `appDslPath`            | File   | `./app.yml` | DSL 输入路径        |
| `appGeneratorOutputDir` | File   | `./output`  | 输出根目录           |
| `appGeneratorModule`    | String | （全限定类名）     | Guice Module 类名 |

form-generator 委托还依赖：`jdbcUrl`、`schema`、`userName`、`password`、`author` 等公共字段。

## app.yml DSL 结构

```yaml
namespace: com.example        # 小写字母+数字+点号，Java 包名
name: my-app                  # UpperCamel，应用名
title: 我的应用               # 用户可见标题

menus: # MenuDef 列表
  - group: 分组名             # 菜单分组标题
    icon: 1                   # 图标标识
    order: 1                  # 排序权重
    form: # FormDef（语法同 form-generator DSL）
      name: Order
      title: 订单
      items: [ ... ]
      indices: [ ... ]
```

### AppDef 字段

| 字段        | 类型              | 必填 | 说明                                            |
|-----------|-----------------|----|-----------------------------------------------|
| namespace | String          | 是  | Java 包名，`^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$` |
| name      | String          | 是  | UpperCamel 应用名                                |
| title     | String          | 是  | 用户可见标题                                        |
| menus     | List\<MenuDef\> | 是  | 菜单定义列表                                        |

### MenuDef 字段

| 字段    | 类型      | 必填 | 说明                                    |
|-------|---------|----|---------------------------------------|
| group | String  | 否  | 菜单分组标题                                |
| icon  | String  | 否  | 图标标识                                  |
| order | Integer | 否  | 排序权重                                  |
| form  | FormDef | 是  | 关联的表单（语法见 `form-generator/CLAUDE.md`） |

## 骨架资源

骨架模板位于 `src/main/resources/`：

- `backend-skeleton/` — Spring Boot 2.7 项目模板，含占位符 `__NAMESPACE_PATH__`、`__DATASOURCE_*__` 等
- `frontend-skeleton/` — Vue 3 + Vite + Ant Design Vue 项目模板

骨架内各自有 `CLAUDE.md`，指导生成后项目的二次开发。

## 内置表单

`frontend-skeleton/src/builtin-form.yml` 定义内置菜单，生成前端时自动合并到 `app.json`：

- **User**（系统分组, order=1）— 用户 CRUD + 授予角色弹框
- **Role**（系统分组, order=2）— 角色 CRUD + 授予权限弹框
- **AuditLog**（系统分组, order=3）— 审计日志只读列表（无 create/update/delete）

内置菜单 order 在合并时被覆盖为 `100000 + 原始 order`，确保排在用户定义菜单之后。

### 创建人/更新人字段联动

当 `builtin-form.yml` 包含 **User** 表单时，自动为每个表单启用创建人/更新人追踪：

- **后端**：`AppGenerator.invokeFormGenerator()` 检测到 User 表单后，override `CommonItemsExpansionService`（为每个表单追加 `createdBy`/`updatedBy` 字段）和 `MutationExpansionService`（在 Create/Update 方法中生成 `CurrentUser.getUsername()` 赋值）
- **前端**：`DataTable.vue` 从 `app.json` 检测是否存在 `form.name === 'User'` 的菜单，有则显示"创建人"和"最近更新人"列

不含 User 表单时，两端均退化为标准行为（无 createdBy/updatedBy）。

### 审计日志联动

当 `builtin-form.yml` 包含 **AuditLog** 表单时，自动为所有非 AuditLog 表单的 create/update/delete 注入审计日志代码：

**后端生成：**
- `AuditOperationTypeEnumGenerateService` 为每个用户表单追加 `CREATE_X`/`UPDATE_X`/`DELETE_X` 枚举项到骨架中的 `AuditOperationTypeEnum`（内置项含 LOGIN/LOGOUT/CHANGE_PASSWORD/GRANT_* 等）
- `AppGeneratorMutationExpansionServiceImpl` 通过 MutationExpansionService 的 `postProcessCreateMethodBody`/`postProcessUpdateMethodBody`/`expandDeleteMethodBody` 注入 try-catch 审计日志调用

**注入代码模式：**
- Create/Delete：构建 `auditContent` Map（key=item.title, value=字段值，排除 secret 类型），try 块内 `auditLogFacade.logSuccess()`，catch 块 `auditLogFacade.logFailure()`
- Update：构建 `oldValues`/`newValues` Map，try 块内 `auditLogFacade.logUpdateSuccess()`，catch 块 `auditLogFacade.logUpdateFailure()`

**AuditLogFacade 骨架：**
- 独立事务（`Propagation.REQUIRES_NEW`），业务回滚不影响审计写入
- `logFailure`/`logUpdateFailure` 仅记录 failReason，不记录 content（失败时数据可能不完整）

**前端**：`AuditLogPage.vue` 为只读列表页，无创建/编辑/删除操作。

## 功能权限体系

生成的应用内置完整 RBAC：权限枚举生成 → 授权 API → 鉴权拦截。详见 `PERMISSION.md`。

### Controller 权限注解映射

`ControllerAuthAnnotateServiceImpl` 通过匹配 `@PostMapping` 参数为每个接口添加 `@WebApiAuth`：

| @PostMapping 匹配 | 注解 |
|------------------|------|
| `"create..."` | `@WebApiAuth(PermissionEnum.CREATE_X)` |
| `"update..."` | `@WebApiAuth(PermissionEnum.UPDATE_X)` |
| `"list..."` | `@WebApiAuth(PermissionEnum.LIST_X)` |
| `"get...Detail"` | `@WebApiAuth(PermissionEnum.LIST_X)` |
| `"delete..."` | `@WebApiAuth(PermissionEnum.DELETE_X)` |

### 前端骨架 CRUD 协议

`frontend-skeleton/src/core/protocol/` 中定义了前后端交互约定：

- `CrudAction`: `'list' | 'create' | 'update' | 'delete' | 'getDetail'`
- `endpointOf(formName, action)`: 生成 `/api/v1/{lower}/{action}{FormName}` URL
- `buildCreateRequest`: 仅传 `canInputOnInit !== false` 的字段（无 bizId）
- `buildUpdateRequest`: 传 bizId + `canInputOnEdit !== false` 的字段
- `handleSubmit`: 根据 `modalMode` 路由到 create 或 update 接口

## 关键约束

1. **form-generator 委托**：`invokeFormGenerator()` 构造独立的 `Config` + `DomainConfig` 再调 `Allison1875.letsGo()`。变更
   form-generator 配置结构时需同步此处
2. **AstForest 手动构建**：因输出目录不是已有 Maven 项目，`invokeFormGenerator()` 手动创建 `DefaultAstForest` 并
   `AstForestContext.set()`
3. **骨架占位符**：所有骨架文件中的 `__XXX__` 占位符在 `replaceInAllFiles()` 中统一替换。新增占位符时需同时更新此方法和骨架文件
4. **输出目录命名**：重名时追加 `-1`、`-2` 后缀，不覆盖已有输出
5. **生成失败回滚**：`play()` 中 catch 后删除整个 `outputRoot`
6. **枚举生成时序**：骨架拷贝 → 权限枚举生成 → 审计操作类型枚举生成 → form-generator 委托（详见 `PERMISSION.md`）

## IT 测试

IT 位于 `allison1875-cli` 模块（所有 tool 共用入口 `Entrypoint`），尚无专属 IT。新增 IT 时遵循标准模式：继承 `ItBaseTest`，资源放
`allison1875-cli/src/test/resources/it/app-generator/<caseName>/`。

## 完整 DSL 示例

参考 `output/super-dsl-example/README.md` 中 DSL 章节。

## 渐进式参考

- `PERMISSION.md` — 功能权限体系完整参考（枚举生成、授权 API、鉴权机制、前后端基础设施）
- `src/main/resources/backend-skeleton/CLAUDE.md` — 生成后后端项目的二次开发规则
- `src/main/resources/frontend-skeleton/CLAUDE.md` — 生成后前端项目的二次开发规则
