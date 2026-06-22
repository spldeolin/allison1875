# 重构：SaveApiService 拆分为 CreateApiService + UpdateApiService

## 背景

form-generator 生成表单 CRUD 时，创建和更新合并为单一 save 接口。这导致：
1. 生成逻辑复杂——需同时处理 create/update 分支（if/else 条件散落各处）
2. 权限管理困难——创建和更新是两个独立功能权限点，合并后无法用注解分别声明

## 目标

- 将 `SaveApiService` 完全拆分为 `CreateApiService` + `UpdateApiService`
- 生成应用产出 `createFormName` 和 `updateFormName` 两个独立接口
- 每个接口使用各自的功能权限注解
- 前端 EditModal 保持单一组件，根据 mode 调用不同接口

## 方案：完全拆分 + 公共逻辑抽取

### 1. form-generator 模块

#### 删除

- `SaveApiService` 接口
- `SaveApiServiceImpl` 实现类

#### 新建 — 公共支撑

`MutationApiSupport` 接口 + `MutationApiSupportImpl` 实现（Guice 单例），封装 Create/Update 共用逻辑：

| 方法 | 职责 |
|------|------|
| `generateSetterToGetter(FormDef, ItemDef, BlockStmt)` | 生成 entity.setXxx(req.getXxx()) 含类型转换 |
| `generateCheckExistStatement(FormDef, IndexDef)` | 唯一索引存在性检查语句 |
| `allCanInput(FormDef, IndexDef, boolean onInit)` | 判断索引所有字段在指定场景下是否均可输入 |
| `generateMultiSelectAssociation(FormDef, BlockStmt)` | multiSelect 关联表 delete+recreate 循环 |
| `generateSetUpdatedAt(FormDef, BlockStmt)` | 生成 entity.setUpdatedAt(LocalDateTime.now()) |

内部辅助方法 `convertItemsToSearchConditions(ItemDef)` 也放入此类。

#### 新建 — CreateApiService

接口方法：
- `generateCreateInitDec(FormDef)` → `InitializerDeclaration`
- `generateCreateMethodBody(FormDef)` → `BlockStmt`

InitDec 生成规则：
- handler name: `create{FormName}`
- type: `"create"`
- ReqDTO: 仅 `canInputOnInit=true` 的字段，所有字段直接带 validation 注解，无 bizId
- RespDTO: 包含 bizId 字段（返回新创建记录标识）

MethodBody 生成规则：
1. 新建实体实例
2. 设置 bizId（短 UUID）
3. 遍历 `canInputOnInit=true` 的非 multiSelect 字段：isNonVoid 校验 + setter
4. 设置 createdAt
5. 唯一索引校验（allCanInput(form, index, true) 的索引）
6. 调用 `mutationApiSupport.generateSetUpdatedAt()`
7. 调用 mapper.insert
8. 调用 `mutationApiSupport.generateMultiSelectAssociation()`
9. return new Create{FormName}Resp().setBizId(...)

#### 新建 — UpdateApiService

接口方法：
- `generateUpdateInitDec(FormDef)` → `InitializerDeclaration`
- `generateUpdateMethodBody(FormDef)` → `BlockStmt`

InitDec 生成规则：
- handler name: `update{FormName}`
- type: `"update"`
- ReqDTO: bizId + `canInputOnEdit=true` 的字段，所有字段直接带 validation 注解
- RespDTO: 无（void 返回）

MethodBody 生成规则：
1. 根据 bizId 查询实体，not found 抛异常
2. 遍历 `canInputOnEdit=true` 的非 multiSelect 字段：isNonVoid 校验 + setter
3. 唯一索引校验（allCanInput(form, index, false) 的索引）
4. 调用 `mutationApiSupport.generateSetUpdatedAt()`
5. 调用 mapper.updateById
6. 调用 `mutationApiSupport.generateMultiSelectAssociation()`
7. 无返回值（void）

#### FormGenerator 调用变化

```java
// Before
InitializerDeclaration saveInitDec = saveApiService.generateSaveInitDec(form);
initDecs.add(saveInitDec);

// After
InitializerDeclaration createInitDec = createApiService.generateCreateInitDec(form);
InitializerDeclaration updateInitDec = updateApiService.generateUpdateInitDec(form);
initDecs.add(createInitDec);
initDecs.add(updateInitDec);
```

### 2. ApiType 枚举变化

```java
// Before
SAVE("save"), LIST("list"), GET_DETAIL("getDetail"), DELETE("delete")

// After
CREATE("create"), UPDATE("update"), LIST("list"), GET_DETAIL("getDetail"), DELETE("delete")
```

### 3. FormGeneratorServiceLayerExpansionServiceImpl 适配

- `buildServiceImplMethodBody()`：根据 expansion map 中 `type` 字段路由
  - `"create"` → `createApiService.generateCreateMethodBody(form)`
  - `"update"` → `updateApiService.generateUpdateMethodBody(form)`
- `buildAnnotationsFormServiceImplMethod()`：CREATE 和 UPDATE 都加 `@Transactional`
- `buildFieldsForServiceImpl()`：逻辑不变，create 和 update 都需要主表 + 关联表 mapper

### 4. app-generator 模块适配

#### ControllerAuthAnnotateServiceImpl

```java
// Before: 匹配 "save" → @WebApiAuth({CREATE_X, UPDATE_X})

// After:
// 匹配 "create" → @WebApiAuth(PermissionEnum.CREATE_X)
// 匹配 "update" → @WebApiAuth(PermissionEnum.UPDATE_X)
```

PermissionEnumGenerateServiceImpl 和 AppGenerator menu permissions 无变化。

### 5. 前端骨架适配

#### endpoints.ts

```typescript
// Before
export type CrudAction = 'list' | 'save' | 'delete' | 'getDetail'

// After
export type CrudAction = 'list' | 'create' | 'update' | 'delete' | 'getDetail'
```

`endpointOf` 新增：
- `'create'` → `/api/v1/{lower}/create{FormName}`
- `'update'` → `/api/v1/{lower}/update{FormName}`

删除 `'save'` 分支。

#### request-builder.ts

`buildSaveRequest` 拆分为：
- `buildCreateRequest(schema, formData)` — 仅 `canInputOnInit !== false` 的字段，不含 bizId
- `buildUpdateRequest(schema, formData)` — 仅 `canInputOnEdit !== false` 的字段 + bizId

不再需要 mode 参数。

#### useCrudPage.ts handleSubmit

```typescript
// Before
const endpoint = endpointOf(schema.name, 'save')
const body = buildSaveRequest(schema, formData, mode)

// After
if (mode === 'create') {
  const endpoint = endpointOf(schema.name, 'create')
  const body = buildCreateRequest(schema, formData)
} else {
  const endpoint = endpointOf(schema.name, 'update')
  const body = buildUpdateRequest(schema, formData)
}
```

#### EditModal.vue

无变化。字段可见性仍由 `canInputOnInit` / `canInputOnEdit` 控制，mode 传入逻辑不变。

#### 权限控制

无变化。`v-permission` 已使用独立的 `permissions.create` / `permissions.update`。

## 影响范围总结

| 模块 | 操作 |
|------|------|
| form-generator | 删除 SaveApiService/Impl，新建 CreateApiService/Impl、UpdateApiService/Impl、MutationApiSupport/Impl |
| form-generator | 修改 ApiType 枚举、FormGenerator、FormGeneratorServiceLayerExpansionServiceImpl |
| app-generator | 修改 ControllerAuthAnnotateServiceImpl |
| app-generator/frontend-skeleton | 修改 endpoints.ts、request-builder.ts、useCrudPage.ts |
| allison1875-cli | 更新相关集成测试的预期输出 |
