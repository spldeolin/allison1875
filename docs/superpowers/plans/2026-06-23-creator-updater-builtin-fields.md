# Creator/Updater Builtin Fields Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable form-generator to support `createdBy`/`updatedBy` builtin fields via an ExpansionService pattern that keeps form-generator unaware of app-generator.

**Architecture:** Four coordinated changes — (1) move composite module assembly into FormGeneratorModule, (2) extract `prepareDomain()` public method, (3) CommonItemsExpansionService interface + default/app implementations, (4) MutationExpansionService interface + default/app implementations. App-generator overrides bindings via `Modules.override()` when constructing its own injector.

**Tech Stack:** Java 21, Google Guice 5.1.0, JavaParser 3.28.1, JUnit 5

---

## File Structure

### Files to modify

| File | Responsibility |
|------|---------------|
| `common/.../Allison1875.java` | Extract `prepareDomain()`, remove `buildCompositeModule`/`loadModule` |
| `common/.../enums/ToolEnum.java` | Remove `composite` field, simplify to 2-arg constructor |
| `form-generator/.../FormGeneratorModule.java` | Self-contain sub-module composition |
| `form-generator/.../FormGenerator.java` | Inject `CommonItemsExpansionService`, remove private `addCommonItems` |
| `form-generator/.../dsl/ItemDef.java` | Replace `specialItemType` with `isBuiltinField` |
| `form-generator/.../dsl/FormDef.java` | Rewrite `getNonAuditedItems()` to semantic filter |
| `form-generator/.../service/impl/CreateApiServiceImpl.java` | Inject+call `MutationExpansionService` |
| `form-generator/.../service/impl/UpdateApiServiceImpl.java` | Inject+call `MutationExpansionService` |
| `form-generator/.../service/impl/ListApiServiceImpl.java` | Inject+call `MutationExpansionService` |
| `app-generator/.../AppGenerator.java` | Replace `letsGo()` with `prepareDomain()` + custom injector |

### Files to create

| File | Responsibility |
|------|---------------|
| `form-generator/.../service/CommonItemsExpansionService.java` | Interface with `@ImplementedBy` |
| `form-generator/.../service/impl/FormGeneratorCommonItemsExpansionServiceImpl.java` | Default: adds bizId/createdAt/updatedAt |
| `form-generator/.../service/MutationExpansionService.java` | Interface with `@ImplementedBy` |
| `form-generator/.../service/impl/FormGeneratorMutationExpansionServiceImpl.java` | Default: no-op |
| `app-generator/.../service/impl/AppGeneratorCommonItemsExpansionServiceImpl.java` | Adds createdBy/updatedBy |
| `app-generator/.../service/impl/AppGeneratorMutationExpansionServiceImpl.java` | Generates CurrentUser assignments |

### Files to delete

| File | Reason |
|------|--------|
| `form-generator/.../dsl/enums/SpecialItemType.java` | Replaced by `isBuiltinField` |

---

## Task 1: ToolEnum — Remove `composite` Field

**Files:**
- Modify: `common/src/main/java/com/spldeolin/allison1875/common/enums/ToolEnum.java`

- [ ] **Step 1: Simplify ToolEnum to 2-arg constructor**

```java
package com.spldeolin.allison1875.common.enums;

import java.util.function.Function;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2026-05-12
 */
@Getter
@AllArgsConstructor
public enum ToolEnum {

    DOC_ANALYZER("doc-analyzer", Config::getDocAnalyzerModule),

    HANDLER_TRANSFORMER("handler-transformer", Config::getHandlerTransformerModule),

    PERSISTENCE_GENERATOR("persistence-generator", Config::getPersistenceGeneratorModule),

    QUERY_TRANSFORMER("query-transformer", Config::getQueryTransformerModule),

    STAR_TRANSFORMER("star-transformer", Config::getStarTransformerModule),

    FORM_GENERATOR("form-generator", Config::getFormGeneratorModule),

    APP_GENERATOR("app-generator", Config::getAppGeneratorModule),

    ;

    private final String toolName;

    private final Function<Config, String> moduleClassNameGetter;

    public static ToolEnum of(String toolName) {
        for (ToolEnum tool : values()) {
            if (tool.toolName.equals(toolName)) {
                return tool;
            }
        }
        StringBuilder sb = new StringBuilder("不支持的工具名: ").append(toolName).append("，可选值: ");
        for (int i = 0; i < values().length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(values()[i].toolName);
        }
        throw new Allison1875Exception(sb.toString());
    }

}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl common -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add common/src/main/java/com/spldeolin/allison1875/common/enums/ToolEnum.java
git commit -m "refactor: remove composite field from ToolEnum"
```

---

## Task 2: Allison1875 — Extract `prepareDomain()` and Remove Composite Logic

**Files:**
- Modify: `common/src/main/java/com/spldeolin/allison1875/common/Allison1875.java`

- [ ] **Step 1: Refactor Allison1875.java**

Remove methods: `buildCompositeModule()`, `loadModule()`, `buildModule()`.
Extract `prepareDomain()` as a public static method.
Simplify `letsGo()` to call `prepareDomain()` then construct module via `buildSimpleModule()` directly.

```java
public static void prepareDomain(Config config, String domainName) {
    DomainConfig domainConfig = resolveDomain(config, domainName);
    log.info("targetDomain={}", JsonUtils.toJson(domainConfig));
    resolveSourceRoots(domainConfig);
    DomainContext.set(domainConfig);
}

public static void letsGo(ToolEnum tool, Config config, String domainName) {
    prepareDomain(config, domainName);

    // 构造Allison1875Module
    Allison1875Module allison1875Module = buildSimpleModule(tool, config);

    // append built-in guice modules
    List<Module> guiceModules = Lists.newArrayList(allison1875Module, new ValidationModule());

    // create guice container
    Injector injector;
    try {
        injector = Guice.createInjector(guiceModules);
    } catch (CreationException e) {
        if (e.getCause() instanceof Allison1875Exception) {
            throw (Allison1875Exception) e.getCause();
        }
        throw e;
    }

    // process main service
    try {
        injector.getInstance(allison1875Module.declareMainService()).play();
    } catch (Throwable e) {
        log.error("main process failed", e);
        throw new Allison1875Exception(e);
    }
}
```

Remove `buildModule()`, `buildCompositeModule()`, `loadModule()` methods entirely. Keep `buildSimpleModule()`, `resolveDomain()`, `resolveSourceRoots()` as private static.

- [ ] **Step 2: Verify compilation (expect failure — FormGeneratorModule not yet updated)**

Run: `mvn compile -pl common -am -q`
Expected: BUILD SUCCESS (common itself compiles; form-generator will fail until Task 3)

- [ ] **Step 3: Commit**

```bash
git add common/src/main/java/com/spldeolin/allison1875/common/Allison1875.java
git commit -m "refactor: extract prepareDomain() and remove composite module logic from Allison1875"
```

---

## Task 3: FormGeneratorModule — Self-Contain Sub-Module Composition

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGeneratorModule.java`

- [ ] **Step 1: Rewrite FormGeneratorModule.configure() to load and combine sub-modules**

```java
package com.spldeolin.allison1875.formgenerator;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.google.inject.AbstractModule;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.guice.Allison1875Game;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceImpl;
import com.spldeolin.allison1875.common.service.impl.DataModelServiceNoLombokImpl;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorServiceLayerExpansionServiceImpl;
import com.spldeolin.allison1875.handlertransformer.service.ServiceLayerExpansionService;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-05-05
 */
@Slf4j
@ToString
public class FormGeneratorModule extends Allison1875Module {

    private final Config config;

    public FormGeneratorModule(Config config) {
        this.config = config;
    }

    @Override
    public final Class<? extends Allison1875Game> declareMainService() {
        return FormGenerator.class;
    }

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

    private static Module loadModule(String moduleClassName, Config config) {
        try {
            log.info("load module: {}", moduleClassName);
            return (Module) Class.forName(moduleClassName).getConstructor(Config.class).newInstance(config);
        } catch (Exception e) {
            throw new Allison1875Exception("加载模块失败: " + moduleClassName, e);
        }
    }

}
```

- [ ] **Step 2: Verify full compilation**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGeneratorModule.java
git commit -m "refactor: move composite module assembly into FormGeneratorModule.configure()"
```

---

## Task 4: ItemDef — Replace `specialItemType` with `isBuiltinField`

**Files:**
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/ItemDef.java`
- Delete: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/SpecialItemType.java`

- [ ] **Step 1: Replace specialItemType field in ItemDef**

In `ItemDef.java`, remove:
```java
import com.spldeolin.allison1875.formgenerator.dsl.enums.SpecialItemType;
```
and:
```java
    /**
     * 特殊字段类型，null代表非特殊字段
     */
    @JsonIgnore
    SpecialItemType specialItemType;
```

Add:
```java
    /**
     * 是否为内置字段（由 CommonItemsExpansionService 添加的字段）
     */
    @JsonIgnore
    Boolean isBuiltinField;
```

- [ ] **Step 2: Delete SpecialItemType.java**

```bash
rm form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/dsl/enums/SpecialItemType.java
```

- [ ] **Step 3: Update FormDef.getNonAuditedItems() to semantic filtering**

In `FormDef.java`, replace:
```java
    @JsonIgnore
    public List<ItemDef> getNonAuditedItems() {
        return items.subList(1, items.size() - 2);
    }
```
with:
```java
    @JsonIgnore
    public List<ItemDef> getNonAuditedItems() {
        return items.stream()
                .filter(item -> !Boolean.TRUE.equals(item.getIsBuiltinField()))
                .collect(Collectors.toList());
    }
```

Add import: `import java.util.stream.Collectors;`

- [ ] **Step 4: Remove SpecialItemType import from FormGenerator.java**

Remove the import line:
```java
import com.spldeolin.allison1875.formgenerator.dsl.enums.SpecialItemType;
```

And in `addCommonItems()`, replace all `setSpecialItemType(SpecialItemType.XXX)` calls with `setIsBuiltinField(true)`.

Note: The existing code has a bug — it calls `bizId.setSpecialItemType(...)` for all three items instead of the correct variable. When migrating, set `setIsBuiltinField(true)` on the correct variables: `bizId`, `createdAt`, `updatedAt`.

- [ ] **Step 5: Verify compilation**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor: replace SpecialItemType with isBuiltinField in ItemDef"
```

---

## Task 5: CommonItemsExpansionService — Interface + Default Implementation

**Files:**
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/CommonItemsExpansionService.java`
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/FormGeneratorCommonItemsExpansionServiceImpl.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/FormGenerator.java`

- [ ] **Step 1: Create CommonItemsExpansionService interface**

```java
package com.spldeolin.allison1875.formgenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorCommonItemsExpansionServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(FormGeneratorCommonItemsExpansionServiceImpl.class)
public interface CommonItemsExpansionService {

    void addCommonItems(List<FormDef> forms);

}
```

- [ ] **Step 2: Create FormGeneratorCommonItemsExpansionServiceImpl**

Extract existing logic from `FormGenerator.addCommonItems()`, setting `isBuiltinField=true` on all added items:

```java
package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.IndexDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import com.spldeolin.allison1875.formgenerator.service.CommonItemsExpansionService;

/**
 * @author Deolin 2026-06-23
 */
public class FormGeneratorCommonItemsExpansionServiceImpl implements CommonItemsExpansionService {

    @Override
    public void addCommonItems(List<FormDef> forms) {
        for (FormDef form : forms) {
            TextItemDef bizId = new TextItemDef();
            bizId.setName(StringUtils.uncapitalize(form.getName()) + "Code");
            bizId.setTitle("业务主键");
            bizId.setIsNonVoid(true);
            bizId.setCanInputOnInit(false);
            bizId.setCanInputOnEdit(false);
            bizId.setIsBuiltinField(true);
            bizId.setMaxLength(36);
            form.getItems().add(0, bizId);

            TimeItemDef createdAt = new TimeItemDef();
            createdAt.setName("createdAt");
            createdAt.setTitle("创建时间");
            createdAt.setIsNonVoid(true);
            createdAt.setCanInputOnInit(false);
            createdAt.setCanInputOnEdit(false);
            createdAt.setIsBuiltinField(true);
            form.getItems().add(createdAt);

            TimeItemDef updatedAt = new TimeItemDef();
            updatedAt.setName("updatedAt");
            updatedAt.setTitle("更新时间");
            updatedAt.setIsNonVoid(true);
            updatedAt.setCanInputOnInit(false);
            updatedAt.setCanInputOnEdit(false);
            updatedAt.setIsBuiltinField(true);
            form.getItems().add(updatedAt);

            IndexDef index = new IndexDef();
            index.setItemNames(Lists.newArrayList(bizId.getName()));
            index.setIsUnique(true);
            if (form.getIndices() == null) {
                form.setIndices(Lists.newArrayList());
            }
            form.getIndices().addFirst(index);
        }
    }

}
```

- [ ] **Step 3: Modify FormGenerator — inject CommonItemsExpansionService and remove private method**

In `FormGenerator.java`:

Add field:
```java
@Inject
private CommonItemsExpansionService commonItemsExpansionService;
```

In `play()`, replace `addCommonItems(forms);` with `commonItemsExpansionService.addCommonItems(forms);`.

Delete the entire `private void addCommonItems(List<FormDef> forms)` method.

Remove unused imports: `SpecialItemType`, `TextItemDef`, `TimeItemDef`, `IndexDef`, `StringUtils`.
Add import: `com.spldeolin.allison1875.formgenerator.service.CommonItemsExpansionService`.

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Run existing form-generator ITs to confirm no regression**

Run: `mvn test -pl allison1875-cli -am -Dtest="com.spldeolin.allison1875.cli.it.formgenerator.*" -q`
Expected: All tests PASS

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor: extract addCommonItems into CommonItemsExpansionService"
```

---

## Task 6: MutationExpansionService — Interface + Default Implementation

**Files:**
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/MutationExpansionService.java`
- Create: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/FormGeneratorMutationExpansionServiceImpl.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/CreateApiServiceImpl.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/UpdateApiServiceImpl.java`
- Modify: `form-generator/src/main/java/com/spldeolin/allison1875/formgenerator/service/impl/ListApiServiceImpl.java`

- [ ] **Step 1: Create MutationExpansionService interface**

```java
package com.spldeolin.allison1875.formgenerator.service;

import java.util.List;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorMutationExpansionServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(FormGeneratorMutationExpansionServiceImpl.class)
public interface MutationExpansionService {

    List<String> expandCreateMethodBody(FormDef form, BlockStmt body);

    List<String> expandUpdateMethodBody(FormDef form, BlockStmt body);

    List<String> expandListSetterStatements(FormDef form, BlockStmt body, String entityVarName);

}
```

- [ ] **Step 2: Create FormGeneratorMutationExpansionServiceImpl (no-op)**

```java
package com.spldeolin.allison1875.formgenerator.service.impl;

import java.util.Collections;
import java.util.List;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;

/**
 * @author Deolin 2026-06-23
 */
public class FormGeneratorMutationExpansionServiceImpl implements MutationExpansionService {

    @Override
    public List<String> expandCreateMethodBody(FormDef form, BlockStmt body) {
        return Collections.emptyList();
    }

    @Override
    public List<String> expandUpdateMethodBody(FormDef form, BlockStmt body) {
        return Collections.emptyList();
    }

    @Override
    public List<String> expandListSetterStatements(FormDef form, BlockStmt body, String entityVarName) {
        return Collections.emptyList();
    }

}
```

- [ ] **Step 3: Inject MutationExpansionService into CreateApiServiceImpl**

Add field:
```java
@Inject
private MutationExpansionService mutationExpansionService;
```

In `generateCreateMethodBody()`, after line 114 (`body.addStatement(parseStatement("%s.setCreatedAt(LocalDateTime.now());", form.getVarName()));`), add:
```java
        mutationExpansionService.expandCreateMethodBody(form, body);
```

Add import: `com.spldeolin.allison1875.formgenerator.service.MutationExpansionService`.

- [ ] **Step 4: Inject MutationExpansionService into UpdateApiServiceImpl**

Add field:
```java
@Inject
private MutationExpansionService mutationExpansionService;
```

In `generateUpdateMethodBody()`, after line 113 (`mutationApiSupport.generateSetUpdatedAt(form, body);`), add:
```java
        mutationExpansionService.expandUpdateMethodBody(form, body);
```

Wait — design says to call BEFORE `generateSetUpdatedAt`. Re-reading spec:
- Create: after `setCreatedAt` → call `expandCreateMethodBody`
- Update: after `mutationApiSupport.generateSetUpdatedAt` → call `expandUpdateMethodBody`

Actually re-reading the spec more carefully:
- Create: "在 `setCreatedAt(LocalDateTime.now())` 之后调用"
- Update: "在 `mutationApiSupport.generateSetUpdatedAt(form, body)` 之后调用"

So for Update, insert after `generateSetUpdatedAt`:
```java
        mutationExpansionService.expandUpdateMethodBody(form, body);
```

Add import: `com.spldeolin.allison1875.formgenerator.service.MutationExpansionService`.

- [ ] **Step 5: Inject MutationExpansionService into ListApiServiceImpl**

Add field:
```java
@Inject
private MutationExpansionService mutationExpansionService;
```

In `generateMethodBody()`, after the forEach loop adds items to `dtos` (after line 209: `forEachBody.addStatement("dtos.add(dto)");`), but BEFORE adding `dtos.add(dto)`, add expansion statements inside `forEachBody`:

Actually, the design says: "在 entity→DTO 映射循环之后调用". This means AFTER the entire forEach loop, call:
```java
        mutationExpansionService.expandListSetterStatements(form, forEachBody, form.getVarName());
```

Insert this line after the entity→DTO field-copy loop (line 208) but before `forEachBody.addStatement("dtos.add(dto)");` (line 209).

Add import: `com.spldeolin.allison1875.formgenerator.service.MutationExpansionService`.

- [ ] **Step 6: Verify compilation**

Run: `mvn compile -pl form-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Run form-generator ITs**

Run: `mvn test -pl allison1875-cli -am -Dtest="com.spldeolin.allison1875.cli.it.formgenerator.*" -q`
Expected: All tests PASS (no-op default doesn't change behavior)

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: add MutationExpansionService interface with no-op default"
```

---

## Task 7: App-Generator — ExpansionService Implementations

**Files:**
- Create: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/AppGeneratorCommonItemsExpansionServiceImpl.java`
- Create: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/AppGeneratorMutationExpansionServiceImpl.java`

- [ ] **Step 1: Create AppGeneratorCommonItemsExpansionServiceImpl**

```java
package com.spldeolin.allison1875.appgenerator.service.impl;

import java.util.List;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.service.CommonItemsExpansionService;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorCommonItemsExpansionServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
public class AppGeneratorCommonItemsExpansionServiceImpl implements CommonItemsExpansionService {

    @Override
    public void addCommonItems(List<FormDef> forms) {
        new FormGeneratorCommonItemsExpansionServiceImpl().addCommonItems(forms);

        for (FormDef form : forms) {
            TextItemDef createdBy = new TextItemDef();
            createdBy.setName("createdBy");
            createdBy.setTitle("创建人");
            createdBy.setIsNonVoid(true);
            createdBy.setCanInputOnInit(false);
            createdBy.setCanInputOnEdit(false);
            createdBy.setIsBuiltinField(true);
            createdBy.setMaxLength(32);
            form.getItems().add(createdBy);

            TextItemDef updatedBy = new TextItemDef();
            updatedBy.setName("updatedBy");
            updatedBy.setTitle("最近更新人");
            updatedBy.setIsNonVoid(true);
            updatedBy.setCanInputOnInit(false);
            updatedBy.setCanInputOnEdit(false);
            updatedBy.setIsBuiltinField(true);
            updatedBy.setMaxLength(32);
            form.getItems().add(updatedBy);
        }
    }

}
```

- [ ] **Step 2: Create AppGeneratorMutationExpansionServiceImpl**

```java
package com.spldeolin.allison1875.appgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;

import java.util.Collections;
import java.util.List;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;

/**
 * @author Deolin 2026-06-23
 */
public class AppGeneratorMutationExpansionServiceImpl implements MutationExpansionService {

    private final String namespace;

    public AppGeneratorMutationExpansionServiceImpl(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public List<String> expandCreateMethodBody(FormDef form, BlockStmt body) {
        body.addStatement(parseStatement("%s.setCreatedBy(CurrentUser.getUsername());", form.getVarName()));
        body.addStatement(parseStatement("%s.setUpdatedBy(CurrentUser.getUsername());", form.getVarName()));
        return List.of(namespace + ".common.CurrentUser");
    }

    @Override
    public List<String> expandUpdateMethodBody(FormDef form, BlockStmt body) {
        body.addStatement(parseStatement("%s.setUpdatedBy(CurrentUser.getUsername());", form.getVarName()));
        return List.of(namespace + ".common.CurrentUser");
    }

    @Override
    public List<String> expandListSetterStatements(FormDef form, BlockStmt body, String entityVarName) {
        body.addStatement(parseStatement("dto.setCreatedBy(%s.getCreatedBy());", entityVarName));
        body.addStatement(parseStatement("dto.setUpdatedBy(%s.getUpdatedBy());", entityVarName));
        return Collections.emptyList();
    }

}
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl app-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add app-generator ExpansionService implementations for createdBy/updatedBy"
```

---

## Task 8: AppGenerator — Replace `letsGo()` with Custom Injector

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java`

- [ ] **Step 1: Rewrite invokeFormGenerator() method**

Replace the existing `invokeFormGenerator()` method (lines 303-369). The new version:
1. Calls `Allison1875.prepareDomain(fgConfig, null)` instead of `Allison1875.letsGo(...)`
2. Constructs `FormGeneratorModule` + override module with expansion services
3. Creates injector and calls `play()`

```java
private void invokeFormGenerator(AppDef appDef, Path backendOutput, List<FormDef> forms) {
    Path tempDsl = writeTempFormsDsl(forms);
    String absPath = backendOutput.toAbsolutePath().toString();

    // Construct config for form-generator
    Config fgConfig = new Config();
    fgConfig.setDslPath(tempDsl.toFile());
    fgConfig.setAuthor(config.getAuthor());
    fgConfig.setJdbcUrl(null);
    fgConfig.setEnableGenerateDesign(true);
    fgConfig.setIsEntityEndWithEntity(true);
    fgConfig.setEnableJavaxMoveToJakarta(false);
    fgConfig.setEnableOneService(true);
    fgConfig.setMarkdownDir(new File(absPath + "/api-docs"));

    Config.CodeSnippet cs = new Config.CodeSnippet();
    String ns = appDef.getNamespace();
    cs.setRequestResultQualifier(ns + ".common.RequestResult");
    cs.setRequestResultTypeDeclaration("RequestResult<${dataType}>");
    cs.setRequestResultSuccessNoData("RequestResult.success()");
    cs.setRequestResultSuccessWithData("RequestResult.success(${data})");
    cs.setBizExceptionQualifier(ns + ".common.BizException");
    fgConfig.setCodeSnippet(cs);

    DomainConfig dc = new DomainConfig();
    dc.setName("default");
    dc.setControllerModule(absPath);
    dc.setControllerPackage(ns + ".controller");
    dc.setDtoModule(absPath);
    dc.setReqDTOPackage(ns + ".dto.req");
    dc.setRespDTOPackage(ns + ".dto.resp");
    dc.setEnumModule(absPath);
    dc.setEnumPackage(ns + ".enums");
    dc.setServiceModule(absPath);
    dc.setServicePackage(ns + ".service");
    dc.setServiceImplModule(absPath);
    dc.setServiceImplPackage(ns + ".service.impl");
    dc.setPersistenceModule(absPath);
    dc.setMapperPackage(ns + ".mapper");
    dc.setEntityPackage(ns + ".entity");
    dc.setDesignPackage(ns + ".design");
    dc.setParamDTOPackage(ns + ".dto.param");
    dc.setRecordDTOPackage(ns + ".dto.record");
    dc.setWholeDTOPackage(ns + ".dto");
    fgConfig.setDomains(Lists.newArrayList(dc));

    // Build AstForest for the new project
    AstForest astForest = new DefaultAstForest(MavenUtils.buildClassLoader(new File(absPath), null),
            new File(absPath));
    AstForestContext.set(astForest);

    // prepareDomain (resolves sourceRoots, sets DomainContext)
    Allison1875.prepareDomain(fgConfig, null);

    // Build FormGeneratorModule + override with app-generator expansion services
    Module fgModule = new FormGeneratorModule(fgConfig);
    Module expansionOverride = new AbstractModule() {
        @Override
        protected void configure() {
            bind(CommonItemsExpansionService.class)
                    .toInstance(new AppGeneratorCommonItemsExpansionServiceImpl());
            bind(MutationExpansionService.class)
                    .toInstance(new AppGeneratorMutationExpansionServiceImpl(ns));
        }
    };
    Module combined = Modules.override(fgModule).with(expansionOverride);

    log.info("invoking form-generator for {} forms...", forms.size());
    Injector injector = Guice.createInjector(combined, new ValidationModule());
    injector.getInstance(FormGenerator.class).play();
    log.info("form-generator completed");

    // Cleanup temp file
    try {
        Files.deleteIfExists(tempDsl);
    } catch (IOException ignored) {
    }
}
```

Add required imports to `AppGenerator.java`:
```java
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.spldeolin.allison1875.common.guice.ValidationModule;
import com.spldeolin.allison1875.appgenerator.service.impl.AppGeneratorCommonItemsExpansionServiceImpl;
import com.spldeolin.allison1875.appgenerator.service.impl.AppGeneratorMutationExpansionServiceImpl;
import com.spldeolin.allison1875.formgenerator.FormGenerator;
import com.spldeolin.allison1875.formgenerator.FormGeneratorModule;
import com.spldeolin.allison1875.formgenerator.service.CommonItemsExpansionService;
import com.spldeolin.allison1875.formgenerator.service.MutationExpansionService;
```

Remove now-unused imports:
```java
import com.spldeolin.allison1875.common.enums.ToolEnum;
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl app-generator -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java
git commit -m "feat: AppGenerator uses prepareDomain + custom injector with ExpansionService overrides"
```

---

## Task 9: Full Verification — `mvn verify`

**Files:** None (verification only)

- [ ] **Step 1: Run full verify**

Run: `mvn verify`
Expected: BUILD SUCCESS with all tests passing

- [ ] **Step 2: If tests fail, diagnose and fix**

Key areas to check:
- `getNonAuditedItems()` — the new semantic filter must exclude exactly the same items as the old positional filter. The old filter excluded index 0 (bizId) and last 2 (createdAt, updatedAt). The new filter excludes all items where `isBuiltinField=true`. Verify that app-generator's added `createdBy`/`updatedBy` items (also `isBuiltinField=true`) are correctly excluded from `getNonAuditedItems()` — this is the desired behavior since they should not appear in user input forms.
- `ListApiServiceImpl.generateMethodBody()` line 179: `form.getItems().subList(1, form.getItems().size() - 1)` — this is a hardcoded positional access for search conditions that skips bizId (index 0) and updatedAt (last). With app-generator's extra fields, this subList would include `createdBy`/`updatedBy` items — but since they're `TextItemDef` type, they'll get `.like()` search conditions which is acceptable (they will filter in the WHERE clause). However, the design intent is that search conditions for builtin fields should NOT be generated. If this causes issues, the fix is to also filter by `!isBuiltinField` in the search conditions loop.

- [ ] **Step 3: Commit any fixes**

```bash
git add -A
git commit -m "fix: resolve test failures after creator/updater builtin fields integration"
```

---

## Task 10: CLAUDE.md — Document ExpansionService Pattern

**Files:**
- Modify: `CLAUDE.md` (root)

- [ ] **Step 1: Add ExpansionService pattern section**

Add after the "AST 处理管道" section:

```markdown
## ExpansionService 模式

接口用 `@ImplementedBy` 声明默认实现，通过 Guice 模块绑定覆盖。用于跨工具扩展而不引入硬依赖。

**机制：** 工具 A（如 form-generator）定义 ExpansionService 接口 + 无操作默认实现。工具 B（如 app-generator）通过 `Allison1875.prepareDomain()` + 自行构建注射器 + `Modules.override()` 注入自定义实现。

**`prepareDomain(Config, String)` 用法：** 调用此方法完成 domain 解析 + sourceRoot 解析 + DomainContext 设置后，调用方可自行构建注射器（跳过 `letsGo()` 的标准流程）。

**现有实例：**
- `ServiceLayerExpansionService` — handler-transformer 的 Service 层代码扩展点
- `CommonItemsExpansionService` — form-generator 的公共字段注入扩展点
- `MutationExpansionService` — form-generator 的 Create/Update/List 方法体扩展点
```

- [ ] **Step 2: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: document ExpansionService pattern in root CLAUDE.md"
```

---

## Compatibility Analysis: `getNonAuditedItems()` Callers

After replacing the positional filter `items.subList(1, items.size() - 2)` with semantic `!isBuiltinField`:

| Caller | Old behavior | New behavior | Compatible? |
|--------|-------------|-------------|-------------|
| `CreateApiServiceImpl:88` — iterate non-audited items for setter generation | Skips bizId + last 2 | Skips all `isBuiltinField=true` items | Yes — all common items (bizId, createdAt, updatedAt) + app-generator items (createdBy, updatedBy) are `isBuiltinField=true` and `canInputOnInit=false`, so they're correctly excluded from user input processing |
| `CreateApiServiceImpl:103` — set defaults for non-input non-void items | Same skip | Same skip | Yes — builtin items with `canInputOnInit=false && isNonVoid=true` were handled specially (setCreatedAt, setBizId) before this loop anyway |
| `UpdateApiServiceImpl:89` — iterate non-audited items for setter generation | Same pattern | Same pattern | Yes |
| `MutationApiServiceImpl:125` — multiSelect association generation | Skips bizId + last 2 | Skips all builtin | Yes — no builtin field is MULTI_SELECT type |
| `ListApiServiceImpl:76` — search condition generation in req DTO | Skips bizId + last 2 | Skips all builtin | Yes — createdBy/updatedBy excluded from search (correct since they shouldn't be searchable by default) |

**Note on `ListApiServiceImpl:179`**: The line `form.getItems().subList(1, form.getItems().size() - 1)` for Design chain search conditions does NOT use `getNonAuditedItems()`. It's a separate hardcoded positional access. This should remain compatible because:
- In form-generator standalone mode: layout is [bizId, ...user..., createdAt, updatedAt] — subList(1, size-1) skips bizId and updatedAt, includes createdAt (which generates a time range condition as before)
- In app-generator mode: layout is [bizId, ...user..., createdAt, updatedAt, createdBy, updatedBy] — subList(1, size-1) skips bizId and updatedBy, includes createdAt+updatedAt+createdBy. The extra fields are `TextItemDef` so they get `.like()` conditions which is valid. However, this may generate unwanted search filters. If undesired, a follow-up fix can be applied to filter by `!isBuiltinField` in this loop too. For now, this is acceptable behavior since the Design chain conditions only fire when the user passes a non-null value.
