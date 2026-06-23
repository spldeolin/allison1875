# 创建人/更新人内置字段设计

## 目标

使 form-generator 生成的表单支持追踪每条记录的创建人和最近更新人，利用 app-generator 已有的 `CurrentUser` 登录态。通过 ExpansionService 模式实现，使 form-generator 无需感知 app-generator 的存在。

## 架构概览

四项变更组合实现该功能：

1. **模块组合内聚** — 将 `Allison1875.buildCompositeModule()` 逻辑内聚到 `FormGeneratorModule.configure()` 中，移除 `ToolEnum.isComposite` 字段，使 `Allison1875` 不再关注模块间组合关系。
2. **提取 letsGo 前置逻辑** — 将 domain 解析 + sourceRoot 解析 + DomainContext 设置提取为公共方法，供 app-generator 直接构建注射器时复用。
3. **CommonItemsExpansionService** — 将私有方法 `FormGenerator#addCommonItems` 改造为 DI 管理的接口；app-generator 实现额外添加 `createdBy` / `updatedBy` 字段。
4. **MutationExpansionService** — Create/Update/List 方法体生成的新扩展点；app-generator 实现生成 `CurrentUser.getUsername()` 赋值语句。

## 详细设计

### 1. 模块组合内聚 + 前置逻辑提取

#### 1a. FormGeneratorModule 内聚子模块组合

**变更：** 将 `Allison1875.buildCompositeModule()` 的逻辑移入 `FormGeneratorModule.configure()`。

`FormGeneratorModule` 自行加载并组合其依赖的 4 个子模块：

```java
@Override
protected void configure() {
    Module deps = Modules.combine(
        loadModule(config.getPersistenceGeneratorModule(), config),
        loadModule(config.getHandlerTransformerModule(), config),
        loadModule(config.getDocAnalyzerModule(), config),
        loadModule(config.getQueryTransformerModule(), config)
    );
    install(Modules.override(deps).with(new AbstractModule() {
        @Override
        protected void configure() {
            bind(ServiceLayerExpansionService.class)
                .toInstance(new FormGeneratorServiceLayerExpansionServiceImpl());
            bind(Config.class).toInstance(config);
            if (config.getIsDataModelWithoutLombok()) {
                bind(DataModelService.class).toInstance(new DataModelServiceNoLombokImpl());
            } else {
                bind(DataModelService.class).toInstance(new DataModelServiceImpl());
            }
        }
    }));
}
```

`loadModule` 为 FormGeneratorModule 内部私有方法（反射加载，与原 `Allison1875.loadModule()` 相同）。

**同步移除：**
- `Allison1875.buildCompositeModule()` 方法
- `Allison1875.buildModule()` 中的 `if (tool.isComposite())` 分支
- `Allison1875.loadModule()` 方法
- `ToolEnum.isComposite` 字段及 `FORM_GENERATOR` 的 `true` 参数

`ToolEnum` 枚举构造函数简化为 `(String toolName, Function<Config, String> moduleClassNameGetter)`。

#### 1b. 提取 `letsGo` 前置逻辑为公共方法

**文件：** `common/.../Allison1875.java`

将 `letsGo` 中的 domain 解析 + sourceRoot 解析 + DomainContext 设置提取为公共静态方法：

```java
public static void prepareDomain(Config config, String domainName) {
    DomainConfig domainConfig = resolveDomain(config, domainName);
    log.info("targetDomain={}", JsonUtils.toJson(domainConfig));
    resolveSourceRoots(domainConfig);
    DomainContext.set(domainConfig);
}
```

`letsGo` 内部改为调用 `prepareDomain` 后继续创建注射器。app-generator 调用 `prepareDomain` 后自行构建注射器（见第 5 节）。

`letsGo` 签名不变。

### 2. SpecialItemType 替换为 isBuiltinField

**删除** `SpecialItemType` 枚举及 `ItemDef.specialItemType` 字段。

**新增** `ItemDef` 中的 `boolean isBuiltinField`（默认 false）。所有由 `CommonItemsExpansionService.addCommonItems()` 添加的字段均设为 `isBuiltinField=true`。

**修改 `FormDef.getNonAuditedItems()`**：从位置索引过滤改为语义过滤：

```java
public List<ItemDef> getNonAuditedItems() {
    return items.stream()
        .filter(item -> !Boolean.TRUE.equals(item.getIsBuiltinField()))
        .collect(Collectors.toList());
}
```

### 3. CommonItemsExpansionService

**接口**位于 `form-generator/.../service/CommonItemsExpansionService.java`：

```java
@ImplementedBy(FormGeneratorCommonItemsExpansionServiceImpl.class)
public interface CommonItemsExpansionService {
    void addCommonItems(List<FormDef> forms);
}
```

**默认实现**（form-generator）— `FormGeneratorCommonItemsExpansionServiceImpl`：

从 `FormGenerator#addCommonItems` 提取现有逻辑：
- 添加 `bizId`（TextItemDef，isBuiltinField=true，maxLength=36，唯一索引）
- 添加 `createdAt`（TimeItemDef，isBuiltinField=true）
- 添加 `updatedAt`（TimeItemDef，isBuiltinField=true）

**App-generator 实现** — `AppGeneratorCommonItemsExpansionServiceImpl`：

- 内部实例化 `FormGeneratorCommonItemsExpansionServiceImpl` 并委托调用以获得基础字段（组合优于继承：`new FormGeneratorCommonItemsExpansionServiceImpl().addCommonItems(forms)`）
- 额外为每个 form 添加：
  - `createdBy`：TextItemDef，title="创建人"，isNonVoid=true，canInputOnInit=false，canInputOnEdit=false，isBuiltinField=true，maxLength=32
  - `updatedBy`：TextItemDef，title="最近更新人"，isNonVoid=true，canInputOnInit=false，canInputOnEdit=false，isBuiltinField=true，maxLength=32

**FormGenerator.play() 变更：** 将 `addCommonItems(forms)` 替换为注入的 `commonItemsExpansionService.addCommonItems(forms)`（通过 `@Inject`）。

### 4. MutationExpansionService

**接口**位于 `form-generator/.../service/MutationExpansionService.java`：

```java
@ImplementedBy(FormGeneratorMutationExpansionServiceImpl.class)
public interface MutationExpansionService {
    List<String> expandCreateMethodBody(FormDef form, BlockStmt body);
    List<String> expandUpdateMethodBody(FormDef form, BlockStmt body);
    List<String> expandListSetterStatements(FormDef form, BlockStmt body, String entityVarName);
}
```

返回值 `List<String>` 为生成代码所需的额外 import 全限定类名。

**默认实现**（form-generator）— `FormGeneratorMutationExpansionServiceImpl`：

三个方法均为空操作，返回空列表。

**App-generator 实现** — `AppGeneratorMutationExpansionServiceImpl`：

- `expandCreateMethodBody(form, body)`：生成
  ```java
  entity.setcreatedBy(CurrentUser.getUsername());
  entity.setupdatedBy(CurrentUser.getUsername());
  ```
  返回 `List.of("__NAMESPACE__.common.CurrentUser")`

- `expandUpdateMethodBody(form, body)`：生成
  ```java
  entity.setupdatedBy(CurrentUser.getUsername());
  ```
  返回 `List.of("__NAMESPACE__.common.CurrentUser")`

- `expandListSetterStatements(form, body, entityVarName)`：生成
  ```java
  dto.setcreatedBy(entity.getcreatedBy());
  dto.setupdatedBy(entity.getupdatedBy());
  ```
  返回空列表（无需额外 import）

**调用点：**

- `CreateApiServiceImpl.generateCreateMethodBody` — 在 `setCreatedAt(LocalDateTime.now())` 之后调用 `mutationExpansionService.expandCreateMethodBody(form, body)`
- `UpdateApiServiceImpl.generateUpdateMethodBody` — 在 `mutationApiSupport.generateSetUpdatedAt(form, body)` 之后调用 `mutationExpansionService.expandUpdateMethodBody(form, body)`
- `ListApiServiceImpl.generateMethodBody` — 在 entity→DTO 映射循环之后调用 `mutationExpansionService.expandListSetterStatements(form, body, form.getVarName())`

### 5. AppGenerator 集成

**`AppGenerator.invokeFormGenerator()` 变更：**

不再调用 `Allison1875.letsGo()`，改为自行构建注射器：

```java
private void invokeFormGenerator(Config fgConfig, ...) {
    // 复用公共前置逻辑
    Allison1875.prepareDomain(fgConfig, null);
    AstForestContext.set(astForest);

    // 构建 FormGeneratorModule + 覆盖 ExpansionService 绑定
    Module fgModule = new FormGeneratorModule(fgConfig);
    Module expansionOverride = new AbstractModule() {
        @Override
        protected void configure() {
            bind(CommonItemsExpansionService.class)
                .toInstance(new AppGeneratorCommonItemsExpansionServiceImpl());
            bind(MutationExpansionService.class)
                .toInstance(new AppGeneratorMutationExpansionServiceImpl());
        }
    };
    Module combined = Modules.override(fgModule).with(expansionOverride);

    // 创建注射器并执行
    Injector injector = Guice.createInjector(combined, new ValidationModule());
    injector.getInstance(FormGenerator.class).play();
}
```

这样 `AppGeneratorModule.configure()` 中不需要声明 ExpansionService 绑定（它们属于 form-generator 的注射器），app-generator 在调用点直接通过 `Modules.override()` 注入。

### 6. 生成代码的 Import 处理

`MutationExpansionService` 方法返回 `List<String>` 表示需要的额外 import。调用点（CreateApiServiceImpl、UpdateApiServiceImpl、ListApiServiceImpl）收集这些 import 并传播到上层的 `BuildServiceImplMethodBodyRetval.neededImports` 中，与现有 `java.time`、`java.math` 等 import 合并。

### 7. CLAUDE.md 文档更新

在根 CLAUDE.md 中新增「ExpansionService 模式」段落，记录：
- 模式说明：接口用 `@ImplementedBy` 声明默认实现，通过模块绑定覆盖
- `prepareDomain()` + 自行构建注射器的机制用于跨工具扩展
- 现有实例：`ServiceLayerExpansionService`、`CommonItemsExpansionService`、`MutationExpansionService`

## 变更文件清单

| 模块 | 文件 | 变更 |
|------|------|------|
| common | `Allison1875.java` | 提取 `prepareDomain()` 公共方法，移除 `buildCompositeModule`/`buildModule`/`loadModule` |
| common | `ToolEnum.java` | 移除 `isComposite` 字段，构造函数简化为二参数 |
| form-generator | `SpecialItemType.java` | **删除** |
| form-generator | `ItemDef.java` | 新增 `Boolean isBuiltinField` 字段 |
| form-generator | `FormDef.java` | `getNonAuditedItems()` 改为基于 `isBuiltinField` 的语义过滤 |
| form-generator | `FormGeneratorModule.java` | 内聚子模块组合逻辑（install Modules.override(deps)） |
| form-generator | `CommonItemsExpansionService.java` | 新接口 |
| form-generator | `FormGeneratorCommonItemsExpansionServiceImpl.java` | 新实现（从 FormGenerator 提取） |
| form-generator | `MutationExpansionService.java` | 新接口 |
| form-generator | `FormGeneratorMutationExpansionServiceImpl.java` | 新实现（空操作） |
| form-generator | `FormGenerator.java` | 注入 CommonItemsExpansionService，删除私有方法 |
| form-generator | `CreateApiServiceImpl.java` | 注入 MutationExpansionService，添加钩子调用 |
| form-generator | `UpdateApiServiceImpl.java` | 注入 MutationExpansionService，添加钩子调用 |
| form-generator | `ListApiServiceImpl.java` | 注入 MutationExpansionService，添加钩子调用 |
| app-generator | `AppGeneratorCommonItemsExpansionServiceImpl.java` | 新实现 |
| app-generator | `AppGeneratorMutationExpansionServiceImpl.java` | 新实现 |
| app-generator | `AppGenerator.java` | 改为 `prepareDomain` + 自行构建注射器 + `Modules.override` 注入 ExpansionService |
| 根目录 | `CLAUDE.md` | 记录 ExpansionService 模式 |

## 测试

- 现有 form-generator IT 测试必须原样通过（默认实现保持行为不变）
- App-generator IT 测试验证生成代码包含 `createdBy`/`updatedBy`：
  - DDL（VARCHAR 列）
  - Entity 类（字段）
  - Create API service impl（CurrentUser.getUsername() 赋值）
  - Update API service impl（CurrentUser.getUsername() 赋值）
  - List 响应 DTO（字段存在）
