# form-generator DSL 简化：将 InitOrEditPattern 三态枚举改为 boolean

**日期**：2026-05-30
**作者**：Deolin

## 1. 背景与目标

`form-generator` 当前在 `ItemDef` 上使用 `InitOrEditPattern` 三态枚举（`USER_INPUT` / `DO_NOT` / `TODO`）描述字段在创建（init）和编辑（edit）阶段的行为：

- `USER_INPUT`：用户输入
- `DO_NOT`：不初始化 / 不可编辑
- `TODO`：生成 `// TODO 请补充初始值` 注释，由开发者自行补充

实际使用中 `DO_NOT` 与 `TODO` 在生成路径上分支重叠较多（"非用户输入"），并未带来用户层面的真实价值差异，反而增加了 DSL 的认知成本和生成代码的分支复杂度。

本设计将 DSL 简化为：

- 用 `Boolean canInputOnInit` 替代 `initPattern`，语义"创建时是否允许用户输入"
- 用 `Boolean canInputOnEdit` 替代 `editPattern`，语义"编辑时是否允许用户输入"
- 删除 `InitOrEditPattern` 枚举
- 同步重写 `SaveApiServiceImpl` 的代码生成逻辑、IT 用例、相关 spec 文档与 `super-dsl.yml`

## 2. 不在范围内

- `FormGenerator.addCommonItems()` 中疑似的 `bizId.setSpecialItemType(...)` 写错对象（应为 `createdAt`/`updatedAt`）的 bug，本次不处理。
- DSL 其他字段、其他 service、entity / mapper / DDL / Controller 等生成路径的语义不变。

## 3. DSL 变更

### 3.1 `ItemDef.java`

删除：

```java
@NotNull
InitOrEditPattern initPattern = USER_INPUT;

@NotNull
InitOrEditPattern editPattern = USER_INPUT;
```

新增：

```java
/**
 * 创建（init）时是否允许用户输入。默认 true。
 */
@NotNull
Boolean canInputOnInit = true;

/**
 * 编辑（edit）时是否允许用户输入。默认 true。
 */
@NotNull
Boolean canInputOnEdit = true;
```

同步去除对应的 `import` 语句（`InitOrEditPattern` 与 `USER_INPUT` 静态导入）。

### 3.2 删除 `InitOrEditPattern.java`

文件路径：`form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/InitOrEditPattern.java`

### 3.3 `FormGenerator.addCommonItems`

```java
bizId.setCanInputOnInit(false);
bizId.setCanInputOnEdit(false);
// SpecialItemType.BIZ_ID 仍然保留，用于驱动 SaveApiServiceImpl 中 shortUuid 生成

createdAt.setCanInputOnInit(false);
createdAt.setCanInputOnEdit(false);

updatedAt.setCanInputOnInit(false);
updatedAt.setCanInputOnEdit(false);
```

注意：原有 `updatedAt.setEditPattern(TODO)`（语义"编辑时由 service 生成 LocalDateTime.now()"）的实际行为完全由 `SpecialItemType.UPDATED_AT` 在 `SaveApiServiceImpl` 中显式追加 `setUpdatedAt(LocalDateTime.now())` 实现，与 boolean 字段无关——所以将 `updatedAt` 也设为 `(false,false)` 不会改变最终生成代码。

### 3.4 `MultiSelectItemService` 中辅助表单的 bizId / createdAt

```java
code.setCanInputOnInit(false);
code.setCanInputOnEdit(false);
createdAt.setCanInputOnInit(false);
createdAt.setCanInputOnEdit(false);
```

## 4. `SaveApiServiceImpl` 代码生成逻辑

针对每个 `ItemDef`（`item.specialItemType==null` 的普通字段；`MULTI_SELECT` 仍走原本的关联表单分支）按 4 种组合生成代码：

| canInputOnInit | canInputOnEdit | ReqDTO 字段 | 校验注解 / 校验语句 | entity setter 位置 | `if(toCreate)` 内默认值 |
|:--:|:--:|:--:|:--|:--|:--|
| true  | true  | ✓ | 在 ReqDTO 字段上加 `getJavaValidAnnotations()` 返回的注解 | common 节（`if/else` 之外） | — |
| true  | false | ✓ | 在 `if(toCreate)` 内、setter 之前调用 `getValidationStatement()` | `if(toCreate)` 内 | — |
| false | true  | ✓ | 在 `else` 内、setter 之前调用 `getValidationStatement()` | `else` 内 | `if(toCreate)` 内追加 `entity.setXxx(getTodoValue())` |
| false | false | ✗ | — | 不设置 | `if(toCreate)` 内追加 `entity.setXxx(getTodoValue())`（SpecialItemType 字段由上层特殊处理覆盖） |

`isNonVoid==false` 时不生成校验注解与校验语句；`isNonVoid==false` 且 case(false,*) 时也不写默认值（因为允许 null）。

### 4.1 `ItemService` 新增抽象方法

```java
/**
 * 生成"如果字段为空则抛出异常"的校验语句，用于条件分支内的延迟校验。
 * 示例：if (req.getXxx() == null) { throw new IllegalArgumentException("xxx不能为空"); }
 *
 * 仅在 isNonVoid==true 且字段处于 (true,false) 或 (false,true) 组合时调用。
 */
Statement getValidationStatement(I itemDef);
```

各 `ItemService` 实现：

- 文本类（`TextItemService`/`SecretItemService`）：检查 `StringUtils.isBlank(req.getXxx())` 抛 `IllegalArgumentException("xx不能为空")`
- 其他（`NumberItemService`/`SelectItemService`/`OnOffItemService`/`TimeItemService`）：检查 `req.getXxx() == null` 抛 `IllegalArgumentException("xx不能为空")`
- `MultiSelectItemService`：返回 `null` 或抛不支持（multiSelect 走关联表单路径，不进入此分支）
- `PrimaryItemServiceImpl` 默认实现委派到具体子类

### 4.2 ReqDTO 字段筛选

`generateSaveInitDec` 中：

```java
for (ItemDef item : form.getItems()) {
    if (item.getCanInputOnInit() || item.getCanInputOnEdit()) {
        // 字段进入 ReqDTO
    }
}
```

### 4.3 SaveApiServiceImpl 三个分支生成

`generateMethodBody` 主体（common 节）：仅对 `(true,true)` 字段生成 `entity.setXxx(req.getXxx())`。

`generateIfThenBody`：
- 对 `(true,false)` 字段：if `isNonVoid` 写 `getValidationStatement`，再写 setter
- 对 `(false,*)` 字段：if `isNonVoid` 写 `entity.setXxx(getTodoValue())`（SpecialItemType 优先：BIZ_ID / CREATED_AT 走原有特殊路径）

`generateElseBody`：
- 对 `(false,true)` 字段：if `isNonVoid` 写 `getValidationStatement`，再写 setter

不再生成 `// TODO 请补充初始值` / `// TODO 请补充更新值` 行注释。

## 5. allison1875-cli IT 用例同步

### 5.1 删除目录与对应测试类

- `allison1875-cli/src/test/resources/it/form-generator/init-pattern-todo/`
- `allison1875-cli/src/test/resources/it/form-generator/edit-pattern-donot/`
- `allison1875-cli/src/test/resources/it/form-generator/mixed-init-edit-pattern/`
- `InitPatternTodoItTest.java`
- `EditPatternDoNotItTest.java`
- `MixedInitEditPatternItTest.java`

### 5.2 新增 2 个 IT 用例

#### `cannot-input-on-edit/forms.yml`（覆盖 case(true,false)）

```yaml
- name: Document
  title: 文档
  desc: 文档管理
  items:
    - type: text
      name: title
      title: 标题
      isNonVoid: true
      maxLength: 200
    - type: text
      name: authorName
      title: 作者
      isNonVoid: true
      maxLength: 50
      canInputOnInit: true
      canInputOnEdit: false
  indices: []
```

`CannotInputOnEditItTest.java` 断言：
- `SaveDocumentReq` 包含 `authorName` 与 `@NotBlank`/`@NotNull`（仍要求 ReqDTO 的字段名出现，但**不再要求** ReqDTO 上的注解——校验在分支内，已改为 if-throw）
- `SaveDocumentServiceImpl` 中 `if(toCreate)` 内含 `if (StringUtils.isBlank(req.getAuthorName()))` 抛异常 + `document.setAuthorName(req.getAuthorName())`
- common 节与 `else` 中均不出现 `document.setAuthorName`
- 不出现 `// TODO` 行注释

#### `cannot-input-on-init/forms.yml`（覆盖 case(false,true) 与 case(false,false)）

```yaml
- name: Task
  title: 任务
  desc: 任务管理
  items:
    - type: text
      name: taskName
      title: 任务名称
      isNonVoid: true
      maxLength: 100
    - type: text
      name: assigneeId
      title: 负责人ID
      isNonVoid: false
      maxLength: 36
      canInputOnInit: false
      canInputOnEdit: true
    - type: text
      name: internalCode
      title: 内部编码
      isNonVoid: true
      maxLength: 36
      canInputOnInit: false
      canInputOnEdit: false
  indices: []
```

`CannotInputOnInitItTest.java` 断言：
- `SaveTaskReq` 包含 `assigneeId`，**不**包含 `internalCode`
- `SaveTaskServiceImpl` 中：
  - `if(toCreate)` 内含 `task.setInternalCode(...)`（默认值，来自 `getTodoValue`）
  - `else` 内含 `task.setAssigneeId(req.getAssigneeId())`（assigneeId 是可空字段，无 if-throw 校验）
  - common 节中均不出现 `setAssigneeId` / `setInternalCode`
- 不出现 `// TODO` 行注释

### 5.3 调整既有用例的断言

#### `SaveApiItTest.java`

`forms.yml` 现有 7 个 user 字段都是 (userInput, userInput) → 都是 (true,true)，断言基本不变；移除任何依赖 TODO/doNot 的间接断言（当前没有，行为不变）。

#### `CommonItemsAutoAddItTest.java`

注释里的 `initPattern=TODO` / `editPattern=DO_NOT` 改写为 `canInputOnInit=false` / `canInputOnEdit=false`；断言行为不变（bizId/createdAt/updatedAt 都不进 ReqDTO；bizId 仍出现在 ReqDTO 中作为业务 ID 由 SaveApiServiceImpl 显式追加，逻辑不变）。

`Save service should set updatedAt in common section` 仍成立（由 SpecialItemType 显式追加 `LocalDateTime.now()`）。
`createdAt should NOT be set in else/edit branch` 仍成立。

### 5.4 测试 README 更新

`allison1875-cli/src/test/java/com/spldeolin/allison1875/cli/it/formgenerator/README.md` 中描述用例的列表更新（删除 3 项、新增 2 项、措辞改为 `canInputOn*`）。

## 6. Spec 文件同步（仅替换字段名）

按用户指示，**只更新字段名**，不重写语义段落。涉及文件：

- `docs/superpowers/specs/2026-05-23-form-web-design.md`
- `docs/superpowers/specs/2026-05-24-app-generator-design.md`
- `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator-design.md`
- `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/super-dsl-readme.md`
- `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/gaps.md`
- `docs/superpowers/specs/2026-05-28-aligning-frontend-with-form-generator/contract.md`

替换规则：
- `initPattern` → `canInputOnInit`
- `editPattern` → `canInputOnEdit`
- `InitOrEditPattern` → `Boolean`
- `userInput` → `true`
- `doNot` → `false`
- `todo` → `false`

`generate-form` skill 的 SKILL.md（`skills/generate-form/SKILL.md`）和示例 yml（`skills/integrate-allison1875/examples/forms.yml`）也按相同规则替换字段名与值。

## 7. `super-dsl.yml` 更新

完整 4 种组合在 super-dsl.yml 中的覆盖：

| 组合 | 覆盖字段 |
|:--|:--|
| (true,true) | StudentProfile 中的 idCard / studentName / bio / age；StudentDormitory 中的 studentId / hasAllergy / facilities / doorPassword / monthlyRent；StudentExam 全部字段 |
| (true,false) | StudentProfile.gender |
| (false,true) | 在 StudentDormitory 中新增一个字段（如 `assignedDormRoom`，文本，`canInputOnInit: false, canInputOnEdit: true`） |
| (false,false) | StudentDormitory.emergencyContact（原 `todo+doNot`，现 `(false,false)`） |

`super-dsl.yml` 中所有 `initPattern: userInput` / `editPattern: userInput` 替换为 `canInputOnInit: true` / `canInputOnEdit: true`；`doNot`/`todo` 值替换为 `false`。

`super-dsl-readme.md` 与 `super-dsl.yml` 一并更新（前者已在 § 6 列出）。

## 8. 实施步骤概要

1. 修改 `ItemDef.java`、删除 `InitOrEditPattern.java`
2. 修改 `FormGenerator.addCommonItems`、`MultiSelectItemService` 中对枚举的引用
3. 在 `ItemService` 接口新增 `getValidationStatement` 抽象方法；各实现类补实现
4. 重写 `SaveApiServiceImpl`（按 § 4 的规则）
5. 删除 / 新增 / 调整 IT 用例与测试类（§ 5）
6. 同步更新 spec 文件、skill 文件名（§ 6）
7. 更新 `super-dsl.yml` 与 `super-dsl-readme.md`（§ 7）
8. 跑 `mvn -pl form-generator test`、跑 form-generator 集成测试

## 9. 风险与回滚

- 风险：`SaveApiServiceImpl` 重写涉及 4 个分支与新增校验注入，可能影响其他依赖 `if(toCreate)` 文本结构的下游测试。缓解：保持 `if(toCreate)` / `} else {` / common 节三段式结构不变，仅在分支内部追加内容。
- 回滚：本次工作集中在 6~8 个文件 + IT 文档替换，git revert 单个 commit 即可。
