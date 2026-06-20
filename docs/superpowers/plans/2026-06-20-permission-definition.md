# Permission Definition Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generate a permission enum for all forms in an app, add a permission query API to the backend skeleton, and wire frontend buttons/menus to permission codes.

**Architecture:** Backend skeleton declares an empty `PermissionEnum` shell (with fields and inner `Group` enum). App-generator's new `PermissionEnumGenerateService` fills it via string concatenation. Frontend skeleton gets `data-permission` attributes on buttons, and `app.json` carries explicit permission mappings per menu.

**Tech Stack:** Java 21, Google Guice, JUnit 5, Vue 3 + TypeScript + Naive UI

---

### Task 1: Add `PermissionEnum` empty shell to backend skeleton

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/enums/PermissionEnum.java`

- [ ] **Step 1: Create the enums directory and PermissionEnum.java**

```java
package __NAMESPACE__.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionEnum {

    // === 枚举项由 app-generator 生成，勿手动修改 ===
    ;

    private final String code;
    private final String title;
    private final Group group;
    private final PermissionEnum baseOn;

    @Getter
    @AllArgsConstructor
    public enum Group {
        // === 由 app-generator 生成 ===
        ;

        private final String code;
        private final String title;
    }

}
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/enums/PermissionEnum.java
git commit -m "feat: add PermissionEnum empty shell to backend skeleton"
```

---

### Task 2: Add `PermissionController` and DTOs to backend skeleton

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/PermissionController.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/PermissionGroupResp.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/PermissionResp.java`

- [ ] **Step 1: Create PermissionResp.java**

```java
package __NAMESPACE__.dto.resp;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionResp {

    String code;

    String title;

    String baseOn;

}
```

- [ ] **Step 2: Create PermissionGroupResp.java**

```java
package __NAMESPACE__.dto.resp;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionGroupResp {

    String groupCode;

    String groupTitle;

    List<PermissionResp> permissions;

}
```

- [ ] **Step 3: Create PermissionController.java**

```java
package __NAMESPACE__.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.resp.PermissionGroupResp;
import __NAMESPACE__.dto.resp.PermissionResp;
import __NAMESPACE__.enums.PermissionEnum;

@RestController
@RequestMapping("/api/v1/permission")
public class PermissionController {

    @PostMapping("listPermissions")
    public RequestResult<List<PermissionGroupResp>> listPermissions() {
        List<PermissionGroupResp> result = new ArrayList<>();
        for (PermissionEnum.Group group : PermissionEnum.Group.values()) {
            PermissionGroupResp groupResp = new PermissionGroupResp();
            groupResp.setGroupCode(group.getCode());
            groupResp.setGroupTitle(group.getTitle());
            groupResp.setPermissions(
                    Arrays.stream(PermissionEnum.values())
                            .filter(p -> p.getGroup() == group)
                            .map(p -> {
                                PermissionResp resp = new PermissionResp();
                                resp.setCode(p.getCode());
                                resp.setTitle(p.getTitle());
                                resp.setBaseOn(p.getBaseOn() != null ? p.getBaseOn().getCode() : null);
                                return resp;
                            })
                            .collect(Collectors.toList())
            );
            result.add(groupResp);
        }
        return RequestResult.success(result);
    }

}
```

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/PermissionController.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/PermissionGroupResp.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/PermissionResp.java
git commit -m "feat: add PermissionController and DTOs to backend skeleton"
```

---

### Task 3: Create `PermissionEnumGenerateService` interface and implementation

**Files:**
- Create: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/PermissionEnumGenerateService.java`
- Create: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/PermissionEnumGenerateServiceImpl.java`

- [ ] **Step 1: Create the service interface**

```java
package com.spldeolin.allison1875.appgenerator.service;

import java.nio.file.Path;
import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.appgenerator.service.impl.PermissionEnumGenerateServiceImpl;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;

/**
 * @author Deolin 2026-06-20
 */
@ImplementedBy(PermissionEnumGenerateServiceImpl.class)
public interface PermissionEnumGenerateService {

    void generatePermissionEnum(List<FormDef> allForms, Path backendOutputRoot, String namespace);

}
```

- [ ] **Step 2: Create the service implementation**

```java
package com.spldeolin.allison1875.appgenerator.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.appgenerator.service.PermissionEnumGenerateService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-20
 */
@Singleton
@Slf4j
public class PermissionEnumGenerateServiceImpl implements PermissionEnumGenerateService {

    @Override
    public void generatePermissionEnum(List<FormDef> allForms, Path backendOutputRoot, String namespace) {
        String namespacePath = namespace.replace('.', '/');
        Path targetFile = backendOutputRoot.resolve("src/main/java/" + namespacePath + "/enums/PermissionEnum.java");

        String sourceCode = buildSourceCode(allForms, namespace);

        try {
            Files.createDirectories(targetFile.getParent());
            Files.writeString(targetFile, sourceCode, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.info("generated PermissionEnum with {} permission points for {} forms",
                allForms.size() * 4, allForms.size());
    }

    private String buildSourceCode(List<FormDef> allForms, String namespace) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(namespace).append(".enums;\n\n");
        sb.append("import lombok.AllArgsConstructor;\n");
        sb.append("import lombok.Getter;\n\n");
        sb.append("@Getter\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("public enum PermissionEnum {\n\n");

        // Enum constants
        for (int i = 0; i < allForms.size(); i++) {
            FormDef form = allForms.get(i);
            String upperSnake = toUpperSnake(form.getName());
            String title = form.getTitle();
            String groupRef = "Group." + upperSnake;

            sb.append("    LIST_").append(upperSnake).append("(\"LIST_").append(upperSnake)
                    .append("\", \"查看").append(title).append("\", ").append(groupRef).append(", null),\n");
            sb.append("    CREATE_").append(upperSnake).append("(\"CREATE_").append(upperSnake)
                    .append("\", \"创建").append(title).append("\", ").append(groupRef)
                    .append(", LIST_").append(upperSnake).append("),\n");
            sb.append("    UPDATE_").append(upperSnake).append("(\"UPDATE_").append(upperSnake)
                    .append("\", \"编辑").append(title).append("\", ").append(groupRef)
                    .append(", LIST_").append(upperSnake).append("),\n");
            sb.append("    DELETE_").append(upperSnake).append("(\"DELETE_").append(upperSnake)
                    .append("\", \"删除").append(title).append("\", ").append(groupRef)
                    .append(", LIST_").append(upperSnake).append("),\n");

            if (i < allForms.size() - 1) {
                sb.append("\n");
            }
        }

        sb.append("    ;\n\n");
        sb.append("    private final String code;\n");
        sb.append("    private final String title;\n");
        sb.append("    private final Group group;\n");
        sb.append("    private final PermissionEnum baseOn;\n\n");

        // Inner Group enum
        sb.append("    @Getter\n");
        sb.append("    @AllArgsConstructor\n");
        sb.append("    public enum Group {\n");

        String groupEntries = allForms.stream()
                .map(form -> {
                    String upperSnake = toUpperSnake(form.getName());
                    return "        " + upperSnake + "(\"" + upperSnake + "\", \"" + form.getTitle() + "管理\")";
                })
                .collect(Collectors.joining(",\n"));
        sb.append(groupEntries).append(",\n");

        sb.append("        ;\n\n");
        sb.append("        private final String code;\n");
        sb.append("        private final String title;\n");
        sb.append("    }\n\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String toUpperSnake(String upperCamelName) {
        return MoreStringUtils.camelToSnakeCase(upperCamelName).toUpperCase();
    }

}
```

- [ ] **Step 3: Create the service/impl directories if they don't exist, verify compilation**

```bash
mvn compile -pl app-generator -am -q
```

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/PermissionEnumGenerateService.java \
       app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/PermissionEnumGenerateServiceImpl.java
git commit -m "feat: add PermissionEnumGenerateService for permission enum generation"
```

---

### Task 4: Integrate `PermissionEnumGenerateService` into `AppGenerator`

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java`

- [ ] **Step 1: Add the @Inject field**

Add after `private Config config;` (line 45):

```java
    @Inject
    private PermissionEnumGenerateService permissionEnumGenerateService;
```

Add the import at the top:

```java
import com.spldeolin.allison1875.appgenerator.service.PermissionEnumGenerateService;
```

- [ ] **Step 2: Add the permission enum generation call in `generateBackend()`**

In the `generateBackend` method, insert the permission enum generation **after** the directory rename and **before** the form extraction. Specifically, add after the `deleteEmptyParents` try-catch block (after line 133) and before the `// Extract FormDefs` comment (line 135):

```java
        // Generate permission enum (fills the skeleton's empty PermissionEnum shell)
        List<FormDef> allForms = Lists.newArrayList(appDef.getMenus().stream()
                .map(MenuDef::getForm).collect(Collectors.toList()));
        allForms.addAll(parseBuiltinMenus().stream().map(MenuDef::getForm).collect(Collectors.toList()));
        permissionEnumGenerateService.generatePermissionEnum(allForms, output, appDef.getNamespace());
```

Then update the existing form extraction line (currently line 136) to reuse:

```java
        List<FormDef> forms = appDef.getMenus().stream().map(MenuDef::getForm).collect(Collectors.toList());
```

- [ ] **Step 3: Verify compilation**

```bash
mvn compile -pl app-generator -am -q
```

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java
git commit -m "feat: integrate PermissionEnumGenerateService into AppGenerator"
```

---

### Task 5: Write unit test for `PermissionEnumGenerateServiceImpl`

**Files:**
- Create: `app-generator/src/test/java/com/spldeolin/allison1875/appgenerator/service/impl/PermissionEnumGenerateServiceImplTest.java`

- [ ] **Step 1: Add test dependencies to app-generator pom.xml if not present**

Check if `junit-jupiter` is already inherited from parent. If not, add:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Create the test class**

```java
package com.spldeolin.allison1875.appgenerator.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class PermissionEnumGenerateServiceImplTest {

    private PermissionEnumGenerateServiceImpl service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new PermissionEnumGenerateServiceImpl();
    }

    @Test
    void generatePermissionEnum_singleForm_generates4PermissionPoints() throws IOException {
        FormDef form = new FormDef();
        form.setName("Order");
        form.setTitle("订单");

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        assertTrue(Files.exists(file));

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.contains("LIST_ORDER(\"LIST_ORDER\", \"查看订单\", Group.ORDER, null)"));
        assertTrue(content.contains("CREATE_ORDER(\"CREATE_ORDER\", \"创建订单\", Group.ORDER, LIST_ORDER)"));
        assertTrue(content.contains("UPDATE_ORDER(\"UPDATE_ORDER\", \"编辑订单\", Group.ORDER, LIST_ORDER)"));
        assertTrue(content.contains("DELETE_ORDER(\"DELETE_ORDER\", \"删除订单\", Group.ORDER, LIST_ORDER)"));
    }

    @Test
    void generatePermissionEnum_multipleFormsIncludingBuiltinUser() throws IOException {
        FormDef orderForm = new FormDef();
        orderForm.setName("Order");
        orderForm.setTitle("订单");

        FormDef userForm = new FormDef();
        userForm.setName("User");
        userForm.setTitle("用户");

        List<FormDef> forms = Arrays.asList(orderForm, userForm);
        service.generatePermissionEnum(forms, tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        // Verify Order permissions
        assertTrue(content.contains("LIST_ORDER"));
        assertTrue(content.contains("CREATE_ORDER"));
        assertTrue(content.contains("UPDATE_ORDER"));
        assertTrue(content.contains("DELETE_ORDER"));

        // Verify User permissions
        assertTrue(content.contains("LIST_USER"));
        assertTrue(content.contains("CREATE_USER"));
        assertTrue(content.contains("UPDATE_USER"));
        assertTrue(content.contains("DELETE_USER"));

        // Verify Group inner enum
        assertTrue(content.contains("ORDER(\"ORDER\", \"订单管理\")"));
        assertTrue(content.contains("USER(\"USER\", \"用户管理\")"));
    }

    @Test
    void generatePermissionEnum_multiWordFormName_convertsToUpperSnake() throws IOException {
        FormDef form = new FormDef();
        form.setName("UserProfile");
        form.setTitle("用户信息");

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains("LIST_USER_PROFILE"));
        assertTrue(content.contains("CREATE_USER_PROFILE"));
        assertTrue(content.contains("UPDATE_USER_PROFILE"));
        assertTrue(content.contains("DELETE_USER_PROFILE"));
        assertTrue(content.contains("Group.USER_PROFILE"));
        assertTrue(content.contains("USER_PROFILE(\"USER_PROFILE\", \"用户信息管理\")"));
    }

    @Test
    void generatePermissionEnum_verifyBaseOnReferences() throws IOException {
        FormDef form = new FormDef();
        form.setName("Product");
        form.setTitle("商品");

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        // LIST has null baseOn
        assertTrue(content.contains("LIST_PRODUCT(\"LIST_PRODUCT\", \"查看商品\", Group.PRODUCT, null)"));
        // Others reference LIST_PRODUCT
        assertTrue(content.contains("CREATE_PRODUCT(\"CREATE_PRODUCT\", \"创建商品\", Group.PRODUCT, LIST_PRODUCT)"));
        assertTrue(content.contains("UPDATE_PRODUCT(\"UPDATE_PRODUCT\", \"编辑商品\", Group.PRODUCT, LIST_PRODUCT)"));
        assertTrue(content.contains("DELETE_PRODUCT(\"DELETE_PRODUCT\", \"删除商品\", Group.PRODUCT, LIST_PRODUCT)"));
    }

    @Test
    void generatePermissionEnum_verifyPackageDeclaration() throws IOException {
        FormDef form = new FormDef();
        form.setName("Order");
        form.setTitle("订单");

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.myapp.demo");

        Path file = tempDir.resolve("src/main/java/com/myapp/demo/enums/PermissionEnum.java");
        assertTrue(Files.exists(file));

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.startsWith("package com.myapp.demo.enums;"));
    }

}
```

- [ ] **Step 3: Run the tests**

```bash
mvn test -pl app-generator -Dtest=PermissionEnumGenerateServiceImplTest -am
```

Expected: All 5 tests pass.

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/test/java/com/spldeolin/allison1875/appgenerator/service/impl/PermissionEnumGenerateServiceImplTest.java
git commit -m "test: add unit tests for PermissionEnumGenerateServiceImpl"
```

---

### Task 6: Add `permissions` field to frontend `MenuDef` type and `app.json` generation

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/schema/types.ts`
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java`

- [ ] **Step 1: Add `PermissionsDef` interface and extend `MenuDef` in types.ts**

In `app-generator/src/main/resources/frontend-skeleton/src/schema/types.ts`, add before the `AppDef` interface:

```typescript
export interface PermissionsDef {
  list: string
  create: string
  update: string
  delete: string
}
```

And add `permissions?: PermissionsDef` to the `MenuDef` interface:

```typescript
export interface MenuDef {
  group?: string
  icon?: string
  order?: number
  form: FormDef
  permissions?: PermissionsDef
}
```

- [ ] **Step 2: Modify `generateFrontend()` in `AppGenerator.java` to inject permissions**

In the `generateFrontend` method, after merging menus and before writing `app.json`, add permission injection logic. Replace the app.json writing block (lines 155-163) with:

```java
        // Write app.json with merged menus and permissions
        try {
            // Inject permissions into each menu
            for (MenuDef menu : mergedMenus) {
                String upperSnake = MoreStringUtils.camelToSnakeCase(menu.getForm().getName()).toUpperCase();
                menu.setPermissions(new MenuDef.Permissions()
                        .setList("LIST_" + upperSnake)
                        .setCreate("CREATE_" + upperSnake)
                        .setUpdate("UPDATE_" + upperSnake)
                        .setDelete("DELETE_" + upperSnake));
            }

            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            AppDef frontendAppDef = new AppDef().setNamespace(appDef.getNamespace()).setName(appDef.getName())
                    .setTitle(appDef.getTitle()).setMenus(mergedMenus);
            String appJson = mapper.writeValueAsString(frontendAppDef);
            Files.writeString(output.resolve("src/app.json"), appJson, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
```

Add the import:

```java
import com.spldeolin.allison1875.common.util.MoreStringUtils;
```

- [ ] **Step 3: Add `Permissions` inner class to `MenuDef.java`**

In `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/dsl/MenuDef.java`, add a `permissions` field and inner class:

```java
    Permissions permissions;

    @Data
    @Accessors(chain = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Permissions {

        String list;

        String create;

        String update;

        String delete;

    }
```

- [ ] **Step 4: Verify compilation**

```bash
mvn compile -pl app-generator -am -q
```

- [ ] **Step 5: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/schema/types.ts \
       app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java \
       app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/dsl/MenuDef.java
git commit -m "feat: inject permissions mapping into app.json during frontend generation"
```

---

### Task 7: Add `data-permission` attributes to frontend skeleton buttons

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/CrudPage.vue`
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/DataTable.vue`

- [ ] **Step 1: Pass `permissions` prop from CrudPage to DataTable**

In `CrudPage.vue`, the `schema` prop already contains the full `FormDef`. We need to pass permissions from the router. Modify `CrudPage.vue` to accept permissions and pass them down.

Update the props type in `CrudPage.vue` script (line 9):

```typescript
const props = defineProps<{
  schema: FormDef
  permissions?: { list: string; create: string; update: string; delete: string }
}>()
```

Add `data-permission` to the "创建" button (line 37):

```html
          <NButton type="primary" :data-permission="permissions?.create" @click="handleCreate">创建</NButton>
```

Add `data-permission` to the "批量删除" button (inside the NPopconfirm trigger, line 42-46):

```html
              <NButton
                type="error"
                :disabled="checkedRowKeys.length === 0"
                :data-permission="permissions?.delete"
              >
```

Pass permissions to DataTable (add prop after `:checked-row-keys`, around line 60):

```html
        :permissions="permissions"
```

- [ ] **Step 2: Add `permissions` prop to DataTable and add `data-permission` to row action buttons**

In `DataTable.vue`, add `permissions` to props (after `checkedRowKeys` prop, line 22):

```typescript
  permissions?: { list: string; create: string; update: string; delete: string }
```

In the action column render function (line 130-148), add `'data-permission'` attribute to the edit and delete buttons:

For the edit button (line 131-137), add `'data-permission': props.permissions?.update`:

```javascript
          h(NButton, {
            size: 'small',
            quaternary: true,
            type: 'primary',
            loading: isThisRowLoading,
            disabled: props.editingRowKey != null && !isThisRowLoading,
            'data-permission': props.permissions?.update,
            onClick: () => emit('edit', row)
          }, { default: () => '编辑' }),
```

For the delete button trigger (line 139-144), add `'data-permission': props.permissions?.delete`:

```javascript
            trigger: () => h(NButton, {
              size: 'small',
              quaternary: true,
              type: 'error',
              disabled: props.editingRowKey != null,
              'data-permission': props.permissions?.delete
            }, { default: () => '删除' }),
```

- [ ] **Step 3: Update router to pass `permissions` as props**

In `app-generator/src/main/resources/frontend-skeleton/src/router/index.ts`, modify the `dslRoutes` mapping (line 22-28) to pass `permissions`:

```typescript
const dslRoutes: RouteRecordRaw[] = app.menus.map(menu => ({
  path: `/${upperCamelToKebab(menu.form.name)}`,
  name: menu.form.name,
  component: resolvePageComponent(menu.form.name),
  props: { schema: menu.form, permissions: menu.permissions },
  meta: { title: menu.form.title, group: menu.group, icon: menu.icon, order: menu.order }
}))
```

- [ ] **Step 4: Add `data-permission` to DashboardLayout menu items**

In `DashboardLayout.vue`, the menu options are built from router routes. We need to associate `permissions.list` with each menu item. Modify the `menuOptions` computed (line 27-63).

In the children mapping (line 52-59), add a `data-permission` extra attribute to each menu option. Since Naive UI's NMenu doesn't directly support `data-*` on rendered items, we'll store the permission on the route's meta instead.

Update the router's meta (already done in step 3 via `props`), and update the `hasPermission` check in DashboardLayout to use the menu's permission code for the `list` action. The menu filtering on line 35 already calls `authStore.hasPermission(r.name as string)`. We need to update this to use the `permissions.list` code from the menu definition.

Modify DashboardLayout.vue — update the `menuOptions` computed to reference the actual permission code. Change line 35 from:

```typescript
    if (!authStore.hasPermission(r.name as string)) continue
```

to:

```typescript
    const menuDef = app.menus.find(m => m.form.name === r.name)
    const listPermission = menuDef?.permissions?.list
    if (listPermission && !authStore.hasPermission(listPermission)) continue
```

Add `MenuDef` to the import of types (line 11):

```typescript
import type { AppDef, MenuDef } from '@/schema/types'
```

- [ ] **Step 5: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/CrudPage.vue \
       app-generator/src/main/resources/frontend-skeleton/src/core/DataTable.vue \
       app-generator/src/main/resources/frontend-skeleton/src/router/index.ts \
       app-generator/src/main/resources/frontend-skeleton/src/layouts/DashboardLayout.vue
git commit -m "feat: add data-permission attributes to frontend skeleton buttons and menus"
```

---

### Task 8: Run full build and verify

**Files:**
- No new files

- [ ] **Step 1: Run full compilation**

```bash
mvn compile -am
```

Expected: BUILD SUCCESS

- [ ] **Step 2: Run the unit tests**

```bash
mvn test -pl app-generator -Dtest=PermissionEnumGenerateServiceImplTest -am
```

Expected: All tests pass.

- [ ] **Step 3: Run the full project verification (if applicable IT exists)**

```bash
mvn verify -pl allison1875-cli -am
```

Expected: BUILD SUCCESS (or skip if no relevant IT exists yet).

- [ ] **Step 4: Commit any fixes if needed, then final commit**

If all passes cleanly, no additional commit needed. If fixes were required, commit them.

---

### Task 9: Update spec with milestone completion prompt

**Files:**
- Modify: `docs/superpowers/specs/2026-06-20-permission-definition-design.md`

- [ ] **Step 1: Append the milestone completion section**

At the end of the spec file, ensure the "已完成：权限定义模块" section (already present in the spec from the design phase) accurately reflects the implementation. Verify all details match.

- [ ] **Step 2: Commit**

```bash
git add docs/superpowers/specs/2026-06-20-permission-definition-design.md
git commit -m "docs: mark permission definition module milestone as complete"
```
