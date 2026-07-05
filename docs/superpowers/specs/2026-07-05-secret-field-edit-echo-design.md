# Secret 字段编辑回显与可选覆盖 — 设计文档

> 2026-07-05 · form-generator + app-generator(frontend-skeleton)

## 背景与问题

form-generator 的 `secret` 字段类型（密码/密钥类）为安全考虑，在 `getXxxDetail`
接口中**不返回**明文。这导致编辑弹框存在缺陷：对于 `canInputOnEdit=true`
的 secret 字段，编辑时输入框回显为空，用户若不重新输入就无法保存（当前 EditReqDTO
带 `@NotEmpty`，且后端无条件 `entity.setX(req.getX())` 会用空值覆盖原值）。

期望：用户打开编辑弹框，**什么都不做也能保存**（保留原 secret 值），也可以选择覆盖或清空。

## 目标场景（严格限定）

本设计**仅**改变以下场景的行为：

- 字段类型为 `secret`
- 且 `canInputOnEdit=true`
- 且处于 **edit-update**（编辑已有记录）场景

**明确不改变**：

- 其他字段类型（text/number/select/multiSelect/time/onOff/file）的任何行为
- secret 字段在 **create** 场景的行为（仍必填、仍带 `@NotEmpty`）
- `canInputOnEdit=false` 的 secret（本就不进入 EditReqDTO）
- `GetDetailApiService` 排除 secret 的行为（安全要求，保持不返回明文）
- 审计日志排除 secret 的行为

## 核心协议

对目标场景中的 secret 字段，EditReq 传值语义：

| EditReq 传值 | 含义 | 后端行为 |
|---|---|---|
| `null` | 未修改 | 跳过校验、跳过 setter（保留数据库原值） |
| `""` | 清空 | `isNonVoid=false` → setter 存空串；`isNonVoid=true` → 抛业务异常 |
| 非空字符串 | 覆盖 | setter 存新值 |

## 后端改动（form-generator）

### 1. EditReqDTO 不为 secret 生成 `@NotEmpty`

`UpdateApiServiceImpl.generateUpdateInitDec`：为字段添加校验注解时，若
`item.getType() == ItemType.SECRET` 则跳过 `itemService.getJavaValidAnnotations(item)`
（`null` 是合法的"不修改"信号，不能被 `@NotEmpty` 拦截）。

- `CreateApiServiceImpl` 不改动 → create 的 secret 仍带 `@NotEmpty`。
- 非 secret 字段的注解生成不变。

### 2. Update 方法体为 secret 生成 null-skip 包裹块

`UpdateApiServiceImpl.generateUpdateMethodBody`：遍历字段生成 setter 时，
对 `secret` 且 `canInputOnEdit=true` 的字段，不再无条件调用
`mutationApiSupport.generateSetterToGetter`，改为生成条件块。

`isNonVoid=true`：

```java
if (req.getApiKey() != null) {
    if (req.getApiKey().isEmpty()) {
        throw new BizException("API密钥不能为空");
    }
    entity.setApiKey(req.getApiKey());
}
```

`isNonVoid=false`：

```java
if (req.getApiKey() != null) {
    entity.setApiKey(req.getApiKey());
}
```

实现要点：

- 业务异常类型取 `config.getCodeSnippet().getBizExceptionQualifier()`，
  异常信息为 `{title}不能为空`（与现有 secret 校验文案一致）。
- 空判用 JDK `String.isEmpty()`，与 `@NotEmpty` 语义一致，无需引入
  Spring `StringUtils`，避免额外 import。
- setter 语句本身仍复用 `generateSetterToGetter`（保证与其他字段一致），
  仅在其外层包裹 null-skip 与（必要时）非空校验。secret 是 `String` 类型，
  `generateSetterToGetter` 对其无特殊转换分支，可直接复用。

### 2b. 清理死代码 `getValidationStatement`

`ItemService.getValidationStatement()` 是 save 接口拆分为 create/update 后的遗留
死代码，生成流程中无任何调用者（仅 `PrimaryItemServiceImpl` 的 delegate 自引用）。
本设计不复用它——null-skip 块由 `UpdateApiServiceImpl` 内联生成，以便精确控制
null 跳过与非空校验的嵌套结构。顺带彻底删除：

- 接口声明：`service/ItemService.java`（1 处）
- 全部实现（10 个）：`PrimaryItemServiceImpl`（含 delegate）、`SelectItemService`、
  `MultiSelectItemService`、`TextItemService`、`NumberItemService`、`TimeItemService`、
  `OnOffItemService`、`SecretItemService`、`FileItemService`
- 删除后清理各实现中因此不再使用的 import（如 `SecretItemService` 的
  `parseStatement` 静态导入、`StringUtils`、`Statement` 等——逐个实现核实）。

### 不改动的后端点

- `GetDetailApiServiceImpl` 继续排除 secret（不返回明文）——回显为空的根因，也是安全要求。
- `ListApiServiceImpl` 继续排除 secret。
- `AppGeneratorMutationExpansionServiceImpl` 审计日志继续整体排除 secret。

## 前端改动（app-generator/frontend-skeleton，4 个文件）

改动均在 `app-generator/src/main/resources/frontend-skeleton/src/core/` 下。
不改 `output/` 下已生成的样例（生成产物，由重新生成覆盖）。

### 3. `protocol/field-policy.ts` — 隐藏不可编辑 secret

`isVisible` 的 `edit-update` 分支新增 secret 专属规则：

- `item.type === 'secret' && canInputOnEdit === false` → 返回 `false`（隐藏）。

其他类型 `canInputOnEdit=false`（只读展示）行为不变。

### 4. `EditModal.vue` — required 规则排除 secret + 透传 editMode

- `rules` computed：`edit-update` 下的 secret 字段**不生成** required 规则
  （否则 NForm 会拦截"未修改"的空提交，导致特性失效）。create 下 secret 仍必填。
- NFormItem 的 `:required` 同理：edit-update 的 secret 不显示红星必填标记。
- 向 `FieldRenderer` 透传 `editMode`（`'edit-create' | 'edit-update'`），
  沿用现有 `readonly` 的模式。

### 5. `fields/FieldRenderer.vue` — 透传 editMode 给 SecretField

- 新增可选 prop `editMode?: 'edit-create' | 'edit-update'`。
- 扩展 `extraProps`：仅当 `item.type === 'secret'` 时把 `editMode` 传入组件
  （避免向其他 fragment-root 组件传多余属性触发 Vue 警告，与现有 `readonly`
  仅传 file 的处理一致）。

### 6. `fields/SecretField.vue` — 区分 create / edit-update

新增 prop `editMode?: 'edit-create' | 'edit-update'`。渲染分支：

- **create 或未传 editMode**：维持现状（普通 password NInput，必填，clearable）。
- **edit-update 且 canInputOnEdit=true**（即弹框把它渲染为 edit 的场景）：
  - 初始绑定值为 `null`（detail 不返回 secret → formData 无该键 → `null`）。
  - `type="password"`，`placeholder="••••••"`：因无从知晓是否有值，一律显示星号
    占位；未聚焦、聚焦但未输入时都显示浅灰占位符（"聚焦变淡"由 placeholder
    的浅灰天然体现）；用户输入时 native password 逐字符显示圆点，让用户感知
    "正在覆盖输入"。
  - 输入 → `emit('update:value', 字符串)`。
  - `isNonVoid === false`：**常显**一个清空按钮，点击 → `emit('update:value', '')`，
    表示清空数据库中的值。
  - `isNonVoid === true`：不显示清空按钮（清空必填 secret 注定被后端拒绝）；
    用户只能覆盖或不动。

> `canInputOnEdit=false` 的 secret 已被 field-policy 在 edit-update 隐藏，
> 不会走到 SecretField 的编辑分支。

### 不改动的前端点

- `protocol/request-builder.ts` `buildUpdateRequest` **不改**：
  - `v ?? null` 已把 `null` / `""` / 非空串原样传递给后端。
  - secret `canInputOnEdit=true` 本就不被 `if (item.canInputOnEdit === false) continue` 跳过。
- `protocol/response-parser.ts` **不改**：secret 的 parse 规则是 no-op passthrough，
  detail 无该键时 `formData[name]` 为 `undefined`，模板 `?? null` 归一为 `null`。

## 数据流（edit-update，secret，canInputOnEdit=true）

```
handleEdit → getDetail（不含 secret）
  → parseDetailDto → formData 无 secret 键
  → EditModal localModel[name] = undefined → SecretField value = null（星号占位）

用户不动    → formData 无变化 → buildUpdateRequest 传 null → 后端跳过 setter（保留原值）
用户输入串  → formData[name]=串 → 传串 → 后端 setter 覆盖
用户点清空  → formData[name]='' → 传 '' → 后端：isNonVoid=false 存空串 / isNonVoid=true 抛异常
```

## 测试

- **form-generator IT**：新增/扩展含 secret 字段的 form 用例（`isNonVoid=true` 与
  `false` 各一），断言：
  1. 生成的 Update handler 含 null-skip 条件块（isNonVoid=true 版含非空校验分支）。
  2. EditReqDTO 的 secret 字段**无** `@NotEmpty`。
  3. CreateReqDTO 的 secret 字段**仍有** `@NotEmpty`。
  - 遵循 `allison1875-cli` IT 规范：继承对应 `ItBaseTest`，资源置于
    `allison1875-cli/src/test/resources/it/<tool>/<caseName>/`。
- **前端 skeleton**：静态资源，无前端测试设施 → 靠 TypeScript 类型检查 + 人工审查。

## 涉及文件清单

后端（form-generator）：
- `src/main/java/.../service/impl/UpdateApiServiceImpl.java`（改 2 处）
- `src/main/java/.../service/ItemService.java`（删 `getValidationStatement` 声明）
- `src/main/java/.../service/impl/{Primary,Select,MultiSelect,Text,Number,Time,OnOff,Secret,File}ItemService*.java`
  （删 `getValidationStatement` 实现 + 清理相关 import）

前端（app-generator/frontend-skeleton）：
- `src/main/resources/frontend-skeleton/src/core/protocol/field-policy.ts`
- `src/main/resources/frontend-skeleton/src/core/EditModal.vue`
- `src/main/resources/frontend-skeleton/src/core/fields/FieldRenderer.vue`
- `src/main/resources/frontend-skeleton/src/core/fields/SecretField.vue`

文档：
- `form-generator/CLAUDE.md`：更新 `secret` 类型说明（"编辑时只能重置不能修改"
  已过时，改为描述可选覆盖协议）。
