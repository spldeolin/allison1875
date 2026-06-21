# Permission Enforcement (鉴权) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:
> executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enforce permission checks at backend API layer and frontend UI layer, and bootstrap the permission system with
admin user, initial roles, and automatic role assignment for new users.

**Architecture:** Backend uses a path-to-permission mapping registry approach — a `WebApiAuthRegistry` bean maps request
paths to required `PermissionEnum` codes at startup by scanning Controller annotations. The `ApiAuthFilter` queries this
registry per request. A `PermissionSystemInitializer` bean bootstraps admin user + 3 initial roles (
系统管理员/业务员/观察员) with correct permission bindings at startup. System groups (USER, ROLE) are identified by
hardcoded constants in the initializer, NOT by a field on PermissionEnum.Group. Frontend uses a Vue custom directive
`v-permission` that hides/disables elements based on `func-permission` attribute value checked against the auth store's
permission list.

**Tech Stack:** Spring Boot 2.7 (Filter, annotation, ComponentScan) · Vue 3 (custom directive, Pinia store) · Naive UI

---

### Task 1: Backend — `@WebApiAuth` Annotation

**Files:**

- Create:
  `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/annotation/WebApiAuth.java`

- [ ] **Step 1: Create the annotation file**

```java
package __NAMESPACE__.annotation;

import __NAMESPACE__.enums.PermissionEnum;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注接口所需的功能权限。ApiAuthFilter 将检查当前用户是否拥有声明的权限。
 *
 * @author Deolin
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebApiAuth {

    PermissionEnum[] value();

}
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/annotation/WebApiAuth.java
git commit -m "feat: add @WebApiAuth annotation for API permission declaration"
```

---

### Task 2: Backend — `WebApiAuthRegistry` Path-to-Permission Mapping

**Files:**

- Create:
  `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/common/WebApiAuthRegistry.java`

- [ ] **Step 1: Create the registry bean**

This bean scans all `@RestController` classes at startup, finds methods annotated with `@WebApiAuth`, resolves their
full request path (`@RequestMapping` class-level prefix + `@PostMapping` method-level suffix), and stores a
`Map<String, PermissionEnum[]>`.

```java
package __NAMESPACE__.common;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import __NAMESPACE__.annotation.WebApiAuth;
import __NAMESPACE__.enums.PermissionEnum;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;

@Component
@Slf4j
public class WebApiAuthRegistry {

    @Resource
    private ApplicationContext applicationContext;

    private final Map<String, PermissionEnum[]> pathPermissions = new HashMap<>();

    @PostConstruct
    public void init() {
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
        for (Object controller : controllers.values()) {
            Class<?> clazz = controller.getClass();
            // Spring CGLIB proxies — get the actual user class
            if (clazz.getName().contains("$$")) {
                clazz = clazz.getSuperclass();
            }
            String classPrefix = "";
            RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
            if (classMapping != null && classMapping.value().length > 0) {
                classPrefix = classMapping.value()[0];
            }
            for (Method method : clazz.getDeclaredMethods()) {
                WebApiAuth auth = method.getAnnotation(WebApiAuth.class);
                if (auth == null || auth.value().length == 0) {
                    continue;
                }
                PostMapping postMapping = method.getAnnotation(PostMapping.class);
                if (postMapping != null && postMapping.value().length > 0) {
                    String fullPath = classPrefix + "/" + postMapping.value()[0];
                    // Normalize double slashes
                    fullPath = fullPath.replaceAll("/+", "/");
                    pathPermissions.put(fullPath, auth.value());
                    log.debug("Registered permission: {} -> {}", fullPath, auth.value());
                }
            }
        }
        log.info("WebApiAuthRegistry initialized, {} paths registered", pathPermissions.size());
    }

    /**
     * 获取指定路径所需的权限列表，返回 null 表示无需鉴权
     */
    public PermissionEnum[] getRequiredPermissions(String requestPath) {
        return pathPermissions.get(requestPath);
    }

}
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/common/WebApiAuthRegistry.java
git commit -m "feat: add WebApiAuthRegistry to map API paths to required permissions"
```

---

### Task 3: Backend — Enable Authorization in `ApiAuthFilter`

**Files:**

- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/filter/ApiAuthFilter.java`

- [ ] **Step 1: Uncomment and rewrite the authorization section**

Add `WebApiAuthRegistry` as a dependency and replace the commented-out authorization block (lines 118–134) with working
code:

After the line `log.debug("认证成功, currentUser={} requestPath={}", currentUser, requestPath);` (line 116), replace the
commented block with:

```java
                // ==================== 鉴权 ====================
PermissionEnum[] requiredPermissions = webApiAuthRegistry.getRequiredPermissions(requestPath);
                if(requiredPermissions !=null&&requiredPermissions.length >0){
        for(

PermissionEnum required :requiredPermissions){
        if(!userPermissions.

contains(required.getCode())){
        log.

warn("鉴权失败：用户缺少权限, username={}, missingPermission={}, path={}",
        user.getUsername(),required.

getCode(),requestPath);
        response.

setContentType("application/json;charset=UTF-8");
                            response.

getWriter().

write(JsonUtils.toJson(RequestResult.failure(ErrorCode.FORBIDDEN)));
        return;
        }
        }
        log.

debug("鉴权通过, username={}, path={}",user.getUsername(),requestPath);
        }
```

Add the new import and field:

```java
import __NAMESPACE__.enums.PermissionEnum;

// ... field:

@Resource
private WebApiAuthRegistry webApiAuthRegistry;
```

Remove the now-unused `parsePermissions` method (lines 149-164) since permissions are resolved from the role chain.

- [ ] **Step 2: Verify the complete file compiles conceptually** (check imports and references are consistent)

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/filter/ApiAuthFilter.java
git commit -m "feat: enable permission enforcement in ApiAuthFilter via WebApiAuthRegistry"
```

---

### Task 4: Backend — Annotate Skeleton Controllers with `@WebApiAuth`

**Files:**

- Modify:
  `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/UserController.java`
- Modify:
  `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/RoleController.java`

- [ ] **Step 1: Add `@WebApiAuth` to UserController methods**

```java
import __NAMESPACE__.annotation.WebApiAuth;
import __NAMESPACE__.enums.PermissionEnum;

// On saveUser (handles both create and update):
@WebApiAuth(PermissionEnum.CREATE_USER)  // Note: saveUser handles create; edit permission checked at service/form layer
@PostMapping("saveUser")

// On listUsers:
@WebApiAuth(PermissionEnum.LIST_USER) @PostMapping("listUsers")

// On getUserDetail:
@WebApiAuth(PermissionEnum.LIST_USER) @PostMapping("getUserDetail")

// On deleteUser:
@WebApiAuth(PermissionEnum.DELETE_USER) @PostMapping("deleteUser")

// On grantRoles:
@WebApiAuth(PermissionEnum.GRANT_ROLE) @PostMapping("grantRoles")

// On listUserRoles:
@WebApiAuth(PermissionEnum.LIST_USER) @PostMapping("listUserRoles")
```

- [ ] **Step 2: Add `@WebApiAuth` to RoleController methods**

```java
import __NAMESPACE__.annotation.WebApiAuth;
import __NAMESPACE__.enums.PermissionEnum;

// On saveRole:
@WebApiAuth(PermissionEnum.CREATE_ROLE) @PostMapping("saveRole")

// On listRoles:
@WebApiAuth(PermissionEnum.LIST_ROLE) @PostMapping("listRoles")

// On getRoleDetail:
@WebApiAuth(PermissionEnum.LIST_ROLE) @PostMapping("getRoleDetail")

// On deleteRole:
@WebApiAuth(PermissionEnum.DELETE_ROLE) @PostMapping("deleteRole")

// On grantPermissions:
@WebApiAuth(PermissionEnum.GRANT_PERMISSION) @PostMapping("grantPermissions")

// On listRolePermissions:
@WebApiAuth(PermissionEnum.LIST_ROLE) @PostMapping("listRolePermissions")
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/UserController.java
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/RoleController.java
git commit -m "feat: annotate skeleton controllers with @WebApiAuth permission declarations"
```

---

### Task 5: Backend — Generate `@WebApiAuth` on form-generated Controllers

**Files:**

- Modify: The form-generator template/code that generates Controller classes (need to locate the controller generation
  code in `form-generator` or `handler-transformer` modules)

This task adds `@WebApiAuth` annotations to form-generator-produced Controller methods. The generation logic already
knows the form name; it constructs the permission enum reference as `PermissionEnum.LIST_{UPPER_SNAKE}`,
`PermissionEnum.CREATE_{UPPER_SNAKE}`, etc.

- [ ] **Step 1: Locate the Controller generation template**

Find where `form-generator` generates the `@PostMapping("save...")`, `@PostMapping("list...")`,
`@PostMapping("getDetail...")`, `@PostMapping("delete...")` methods. This is likely in `form-generator` module's service
implementation.

- [ ] **Step 2: Add `@WebApiAuth` annotation generation**

For each generated controller method, add the corresponding `@WebApiAuth` annotation:

| Method pattern    | Permission                            |
|-------------------|---------------------------------------|
| `save{Form}`      | `PermissionEnum.CREATE_{UPPER_SNAKE}` |
| `list{Form}s`     | `PermissionEnum.LIST_{UPPER_SNAKE}`   |
| `get{Form}Detail` | `PermissionEnum.LIST_{UPPER_SNAKE}`   |
| `delete{Form}`    | `PermissionEnum.DELETE_{UPPER_SNAKE}` |

Add the imports:

```java
import {namespace}.annotation.WebApiAuth;
import {namespace}.enums.PermissionEnum;
```

- [ ] **Step 3: Verify by running existing IT tests**

```bash
mvn test -pl allison1875-cli -am -Dtest=BasicMarkdownItTest
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: generate @WebApiAuth annotations on form-generator-produced controllers"
```

---

### Task 6: Backend — Handle `save` Permission (CREATE vs UPDATE)

**Files:**

- Modify: Same file as Task 5 (controller generation)

The `save{Form}` endpoint handles both create and update (determined by whether `bizId` is present in the request).
Currently we annotate it with `CREATE_X`. To properly enforce:

- [ ] **Step 1: Annotate save with both CREATE and UPDATE**

Change the `save` method annotation to require EITHER create or update permission based on the request content. Since
`@WebApiAuth` checks ALL listed permissions (AND logic), we need a different approach.

**Design decision:** Use a single `CREATE_{FORM}` permission for save. The rationale:

- The `UPDATE_{FORM}` permission controls visibility of the "edit" button in the frontend
- If a user has `UPDATE_{FORM}` they necessarily have `LIST_{FORM}` (baseOn relationship)
- But `CREATE_{FORM}` and `UPDATE_{FORM}` are siblings (both baseOn `LIST_{FORM}`)
- Solution: Annotate `save` with **no** permission at backend level — the frontend already guards create/edit buttons
  separately. The backend checks authN only for save.

**Alternative (chosen):** Add a special handling in filter — if `@WebApiAuth` has multiple values, treat as OR (any one
matches = pass). Then annotate save with `{PermissionEnum.CREATE_X, PermissionEnum.UPDATE_X}`.

Update the annotation:

```java
@WebApiAuth({PermissionEnum.CREATE_{UPPER_SNAKE}, PermissionEnum.UPDATE_{UPPER_SNAKE}}) @PostMapping("save{Form}")
```

- [ ] **Step 2: Update `ApiAuthFilter` authorization logic to use OR semantics**

Change the permission check from "user must have ALL" to "user must have ANY ONE":

```java
                if(requiredPermissions !=null&&requiredPermissions.length >0){

boolean hasAny = false;
                    for(

PermissionEnum required :requiredPermissions){
        if(userPermissions.

contains(required.getCode())){
hasAny =true;
        break;
        }
        }
        if(!hasAny){
        log.

warn("鉴权失败：用户缺少权限, username={}, requiredAnyOf={}, path={}",
        user.getUsername(),
                                Arrays.

stream(requiredPermissions).

map(PermissionEnum::getCode)
                                        .

collect(Collectors.joining(",")),
requestPath);
        response.

setContentType("application/json;charset=UTF-8");
                        response.

getWriter().

write(JsonUtils.toJson(RequestResult.failure(ErrorCode.FORBIDDEN)));
        return;
        }
        log.

debug("鉴权通过, username={}, path={}",user.getUsername(),requestPath);
        }
```

- [ ] **Step 3: Update skeleton UserController and RoleController save methods**

```java
// UserController:
@WebApiAuth({PermissionEnum.CREATE_USER, PermissionEnum.UPDATE_USER}) @PostMapping("saveUser")

// RoleController:
@WebApiAuth({PermissionEnum.CREATE_ROLE, PermissionEnum.UPDATE_ROLE}) @PostMapping("saveRole")
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: use OR semantics for @WebApiAuth and annotate save with CREATE|UPDATE"
```

---

### Task 7: Frontend — `v-permission` Custom Directive

**Files:**

- Create: `app-generator/src/main/resources/frontend-skeleton/src/directives/permission.ts`
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/main.ts`

- [ ] **Step 1: Create the permission directive**

```typescript
import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

/**
 * v-permission directive: hides elements when the current user lacks the specified permission.
 *
 * Usage:
 *   <n-button v-permission="'CREATE_ORDER'">创建</n-button>
 *   <n-button v-permission="permissions?.create">创建</n-button>
 *
 * If the value is undefined/null/empty string, the element remains visible (no permission required).
 */
export const permissionDirective: Directive<HTMLElement, string | undefined | null> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | undefined | null>) {
    checkPermission(el, binding.value)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<string | undefined | null>) {
    checkPermission(el, binding.value)
  }
}

function checkPermission(el: HTMLElement, permission: string | undefined | null) {
  if (!permission) {
    el.style.display = ''
    return
  }
  const authStore = useAuthStore()
  if (!authStore.hasPermission(permission)) {
    el.style.display = 'none'
  } else {
    el.style.display = ''
  }
}
```

- [ ] **Step 2: Register the directive in `main.ts`**

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles/global.css'
import appDef from './app.json'
import { permissionDirective } from './directives/permission'

document.title = appDef.title || appDef.name

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.directive('permission', permissionDirective)
app.mount('#app')
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/directives/permission.ts
git add app-generator/src/main/resources/frontend-skeleton/src/main.ts
git commit -m "feat: add v-permission directive for frontend permission enforcement"
```

---

### Task 8: Frontend — Apply `v-permission` to CrudPage Buttons

**Files:**

- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/CrudPage.vue`

- [ ] **Step 1: Replace `func-permission` prop binding with `v-permission` directive**

The `func-permission` attribute was a placeholder. Replace with `v-permission`:

In `CrudPage.vue`, change:

```html

<NButton type="primary" :func-permission="permissions?.create" @click="handleCreate">创建</NButton>
```

to:

```html

<NButton type="primary" v-permission="permissions?.create" @click="handleCreate">创建</NButton>
```

And:

```html

<NButton
        type="error"
        :disabled="checkedRowKeys.length === 0"
        :func-permission="permissions?.delete"
>
```

to:

```html

<NButton
        type="error"
        :disabled="checkedRowKeys.length === 0"
        v-permission="permissions?.delete"
>
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/CrudPage.vue
git commit -m "feat: apply v-permission directive to CrudPage buttons"
```

---

### Task 9: Frontend — Apply `v-permission` to DataTable Row Actions

**Files:**

- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/DataTable.vue`

- [ ] **Step 1: Create a `usePermission` composable for render-function usage**

Since `v-permission` is a template directive and cannot be used inside `h()` render functions, we need a composable that
provides a visibility check. The DataTable uses `h()` for its action column.

Create: `app-generator/src/main/resources/frontend-skeleton/src/directives/usePermission.ts`

```typescript
import { useAuthStore } from '@/stores/auth'

/**
 * Check if current user has the given permission.
 * Returns true if permission is not specified (null/undefined/empty) or user has it.
 */
export function checkPermission(permission: string | undefined | null): boolean {
  if (!permission) return true
  const authStore = useAuthStore()
  return authStore.hasPermission(permission)
}
```

- [ ] **Step 2: Use `checkPermission` in DataTable render function**

In `DataTable.vue`, import the helper and conditionally render buttons:

```typescript
import { checkPermission } from '@/directives/usePermission'
```

In the actions column render function, wrap each button in a permission check. Change:

```typescript
h(NButton, {
  ...
  'func-permission': props.permissions?.update,
  onClick: () => emit('edit', row)
}, { default: () => '编辑' }),
```

to:

```typescript
...(checkPermission(props.permissions?.update) ? [h(NButton, {
  size: 'small',
  quaternary: true,
  type: 'primary',
  loading: isThisRowLoading,
  disabled: props.editingRowKey != null && !isThisRowLoading,
  onClick: () => emit('edit', row)
}, { default: () => '编辑' })] : []),
```

Apply the same pattern to the delete button and extra action buttons.

Full replacement for the actions column render function:

```typescript
render(row: Record<string, any>) {
  const isThisRowLoading = props.editingRowKey != null
    && props.bizKey != null
    && row[props.bizKey] === props.editingRowKey
  return h(NSpace, { wrap: false, size: 4 }, {
    default: () => [
      ...(props.extraActions || []).filter(action => checkPermission(action.permission)).map(action =>
        h(NButton, {
          size: 'small',
          quaternary: true,
          type: (action.type || 'info') as any,
          disabled: props.editingRowKey != null,
          onClick: () => action.onClick(row)
        }, { default: () => action.label })
      ),
      ...(checkPermission(props.permissions?.update) ? [h(NButton, {
        size: 'small',
        quaternary: true,
        type: 'primary',
        loading: isThisRowLoading,
        disabled: props.editingRowKey != null && !isThisRowLoading,
        onClick: () => emit('edit', row)
      }, { default: () => '编辑' })] : []),
      ...(checkPermission(props.permissions?.delete) ? [h(NPopconfirm, { onPositiveClick: () => emit('delete', row) }, {
        trigger: () => h(NButton, {
          size: 'small',
          quaternary: true,
          type: 'error',
          disabled: props.editingRowKey != null
        }, { default: () => '删除' }),
        default: () => '确定要删除吗？'
      })] : [])
    ]
  })
}
```

- [ ] **Step 3: Remove `func-permission` prop attributes from DataTable render function** (they are now replaced by the
  conditional rendering above)

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/directives/usePermission.ts
git add app-generator/src/main/resources/frontend-skeleton/src/core/DataTable.vue
git commit -m "feat: enforce permissions in DataTable row actions via checkPermission"
```

---

### Task 10: Frontend — Apply `v-permission` to RolePage and UserPage Custom Buttons

**Files:**

- Modify: `app-generator/src/main/resources/frontend-skeleton/src/pages/RolePage.vue`
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/pages/UserPage.vue`

- [ ] **Step 1: Update RolePage "授予权限" button**

Change from `:func-permission="'GRANT_PERMISSION'"` to `v-permission="'GRANT_PERMISSION'"`.

The extraActions passed to DataTable should use the `permission` field (which is already handled by Task 9's
`checkPermission` in the render function). Verify that `extraActions` items have their `permission` field set correctly.

- [ ] **Step 2: Update UserPage "授予角色" button**

Same pattern — ensure the extraActions `permission` field is `'GRANT_ROLE'`.

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/pages/RolePage.vue
git add app-generator/src/main/resources/frontend-skeleton/src/pages/UserPage.vue
git commit -m "feat: apply v-permission to custom page action buttons"
```

---

### Task 11: Frontend — Route Guard Permission Enforcement

**Files:**

- Modify: `app-generator/src/main/resources/frontend-skeleton/src/router/index.ts`

- [ ] **Step 1: Add permission-based route blocking**

Currently, the router guard only authenticates (checks token + fetches user). After `fetchCurrentUser()` succeeds, add a
check: if the target route has a `permissions.list` requirement and the user lacks it, redirect to the first accessible
route or show a 403 page.

Update the `beforeEach` guard:

```typescript
router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  const isLoginPage = to.path === '/login'

  if (!authStore.isAuthenticated) {
    if (!isLoginPage) {
      authStore.saveRedirect(to.fullPath)
      return { path: '/login' }
    }
    return
  }

  const ok = await authStore.fetchCurrentUser()
  if (!ok) {
    authStore.logout()
    if (!isLoginPage) {
      authStore.saveRedirect(to.fullPath)
      return { path: '/login' }
    }
    return
  }

  if (isLoginPage) {
    const saved = authStore.popRedirect()
    return saved || firstFormPath
  }

  // Permission-based route guard: check if user has LIST permission for this page
  const listPermission = (to.props as any)?.default?.permissions?.list
    ?? app.menus.find(m => m.form.name === to.name)?.permissions?.list
  if (listPermission && !authStore.hasPermission(listPermission)) {
    // Redirect to first accessible route
    const firstAccessible = dslRoutes.find(r => {
      const menuDef = app.menus.find(m => m.form.name === r.name)
      const perm = menuDef?.permissions?.list
      return !perm || authStore.hasPermission(perm)
    })
    if (firstAccessible) {
      return { path: firstAccessible.path }
    }
    // No accessible route — stay on current or go to login
    return { path: '/login' }
  }
})
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/router/index.ts
git commit -m "feat: add permission-based route guard to block unauthorized page access"
```

---

### Task 12: Frontend — Handle 403 Response in Axios Interceptor

**Files:**

- Modify: `app-generator/src/main/resources/frontend-skeleton/src/utils/request.ts`

- [ ] **Step 1: Add 403 handling in response interceptor**

When the backend returns `errorCode: "403"`, show a user-friendly message instead of a generic error:

```typescript
request.interceptors.response.use(
  response => {
    const data = response.data as RequestResult
    if (data.errorCode === null) {
      return response
    }
    if (data.errorCode === '401') {
      const authStore = useAuthStore()
      authStore.logout()
      return Promise.reject(new Error('认证已过期'))
    }
    if (data.errorCode === '403') {
      return Promise.reject(new Error('没有操作权限'))
    }
    return Promise.reject(new Error(data.errorMsg || '请求失败'))
  },
  error => Promise.reject(error)
)
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/utils/request.ts
git commit -m "feat: handle 403 forbidden response in axios interceptor"
```

---

### Task 13: Backend — `PermissionSystemInitializer` Bean

**Files:**

- Create:
  `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/common/PermissionSystemInitializer.java`
- Modify:
  `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/property/AuthcProperties.java`
- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/resources/application.yml`

This component runs at startup to bootstrap the permission system: ensures admin user, initial roles, and their
permission bindings exist. System groups are identified by hardcoded constants (
`SYSTEM_GROUPS = Set.of("USER", "ROLE")`), NOT by a field on PermissionEnum.Group.

- [ ] **Step 1: Add `adminPassword` property to `AuthcProperties`**

```java
/**
 * 管理员初始密码
 */
String adminPassword;
```

- [ ] **Step 2: Add the config to `application.yml`**

Under `__APP_NAME__.authc`:

```yaml
    adminPassword: admin123
```

- [ ] **Step 3: Create `PermissionSystemInitializer`**

```java
package __NAMESPACE__.common;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import __NAMESPACE__.entity.RoleEntity;
import __NAMESPACE__.entity.RolePermissionEntity;
import __NAMESPACE__.entity.UserEntity;
import __NAMESPACE__.entity.UserRoleEntity;
import __NAMESPACE__.enums.PermissionEnum;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.mapper.RolePermissionMapper;
import __NAMESPACE__.mapper.UserMapper;
import __NAMESPACE__.mapper.UserRoleMapper;
import __NAMESPACE__.property.AuthcProperties;
import __NAMESPACE__.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 权限体系初始化器
 * <p>应用启动时确保admin用户、初始角色及其权限绑定存在
 *
 * @author Deolin
 */
@Component
@Slf4j
public class PermissionSystemInitializer {

    private static final String ADMIN_USERNAME = "admin";

    private static final Set<String> SYSTEM_GROUPS = new HashSet<>(Arrays.asList("USER", "ROLE"));

    private static final InitRole SYSTEM_ADMIN = new InitRole("系统管理员", "拥有全部功能权限，不可删除");

    private static final InitRole BUSINESS_OPERATOR = new InitRole("业务员",
            "拥有所有业务表单的读写权限，新用户默认角色");

    private static final InitRole OBSERVER = new InitRole("观察员", "拥有所有业务表单的只读权限");

    @Resource
    private AuthcProperties authcProperties;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @PostConstruct
    @Transactional
    public void init() {
        UserEntity adminUser = ensureAdminUser();
        RoleEntity adminRole = ensureRole(SYSTEM_ADMIN);
        RoleEntity operatorRole = ensureRole(BUSINESS_OPERATOR);
        RoleEntity observerRole = ensureRole(OBSERVER);

        ensureRolePermissions(adminRole, allPermissionCodes());
        ensureRolePermissions(operatorRole, nonSystemAllPermissionCodes());
        ensureRolePermissions(observerRole, nonSystemListPermissionCodes());

        ensureUserRole(adminUser, adminRole);

        log.info("Permission system initialized: admin={}, roles=[{}, {}, {}]", adminUser.getUsername(),
                SYSTEM_ADMIN.name, BUSINESS_OPERATOR.name, OBSERVER.name);
    }

    private UserEntity ensureAdminUser() {
        UserEntity existing = userMapper.queryByUsername(ADMIN_USERNAME);
        if (existing != null) {
            return existing;
        }
        UserEntity admin = new UserEntity();
        admin.setUserCode(UuidUtils.generateShort());
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(BCrypt.hashpw(authcProperties.getAdminPassword(), BCrypt.gensalt()));
        admin.setNickName("管理员");
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(admin);
        log.info("Created admin user: username={}", ADMIN_USERNAME);
        return userMapper.queryByUsername(ADMIN_USERNAME);
    }

    private RoleEntity ensureRole(InitRole initRole) {
        RoleEntity existing = roleMapper.queryByRoleName(initRole.name);
        if (existing != null) {
            return existing;
        }
        RoleEntity role = new RoleEntity();
        role.setRoleCode(UuidUtils.generateShort());
        role.setRoleName(initRole.name);
        role.setDescription(initRole.description);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        log.info("Created initial role: {}", initRole.name);
        return roleMapper.queryByRoleName(initRole.name);
    }

    private void ensureRolePermissions(RoleEntity role, List<String> expectedCodes) {
        List<String> currentCodes = rolePermissionMapper.queryPermissionCodesByRoleId(role.getId());
        if (currentCodes.size() == expectedCodes.size() && new HashSet<>(currentCodes).containsAll(expectedCodes)) {
            return;
        }
        rolePermissionMapper.deleteByRoleId(role.getId());
        if (expectedCodes.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<RolePermissionEntity> entities = expectedCodes.stream()
                .map(code -> new RolePermissionEntity().setRoleId(role.getId()).setPermissionCode(code)
                        .setCreatedAt(now)).collect(Collectors.toList());
        rolePermissionMapper.batchInsert(entities);
        log.info("Synced permissions for role '{}': {} codes", role.getRoleName(), expectedCodes.size());
    }

    private void ensureUserRole(UserEntity user, RoleEntity role) {
        List<Long> roleIds = userRoleMapper.queryRoleIdsByUserId(user.getId());
        if (roleIds.contains(role.getId())) {
            return;
        }
        UserRoleEntity entity = new UserRoleEntity();
        entity.setUserId(user.getId());
        entity.setRoleId(role.getId());
        entity.setCreatedAt(LocalDateTime.now());
        userRoleMapper.batchInsert(Collections.singletonList(entity));
        log.info("Bound role '{}' to user '{}'", role.getRoleName(), user.getUsername());
    }

    /**
     * 获取"观察员"角色名称，供用户创建时自动绑定
     */
    public static String getDefaultRoleName() {
        return OBSERVER.name;
    }

    private List<String> allPermissionCodes() {
        return Arrays.stream(PermissionEnum.values()).map(PermissionEnum::getCode).collect(Collectors.toList());
    }

    private List<String> nonSystemAllPermissionCodes() {
        return Arrays.stream(PermissionEnum.values()).filter(p -> !SYSTEM_GROUPS.contains(p.getGroup().getCode()))
                .map(PermissionEnum::getCode).collect(Collectors.toList());
    }

    private List<String> nonSystemListPermissionCodes() {
        return Arrays.stream(PermissionEnum.values()).filter(p -> !SYSTEM_GROUPS.contains(p.getGroup().getCode()))
                .filter(p -> p.getCode().startsWith("LIST_")).map(PermissionEnum::getCode).collect(Collectors.toList());
    }

    private record InitRole(String name, String description) {

    }

}
```

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/common/PermissionSystemInitializer.java
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/property/AuthcProperties.java
git add app-generator/src/main/resources/backend-skeleton/src/main/resources/application.yml
git commit -m "feat: add PermissionSystemInitializer for admin user and initial roles bootstrap"
```

---

### Task 14: Backend — Auto-Assign Observer Role on User Creation

**Files:**

- Modify:
  `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/UserServiceImpl.java`

- [ ] **Step 1: Inject RoleMapper and UserRoleMapper, assign default role on create**

After `userMapper.insert(user)` in the create branch, add logic to assign the "观察员" role:

```java

@Resource
private RoleMapper roleMapper;

@Resource
private UserRoleMapper userRoleMapper;
```

In the `saveUser` method, after the `userMapper.insert(user)` line (inside the `if (toCreate)` block):

```java
if(toCreate){
        userMapper.

insert(user);

// Auto-assign default role (观察员) to new users
RoleEntity defaultRole = roleMapper.queryByRoleName(PermissionSystemInitializer.getDefaultRoleName());
    if(defaultRole !=null){

UserRoleEntity userRole = new UserRoleEntity();
        userRole.

setUserId(user.getId());
        userRole.

setRoleId(defaultRole.getId());
        userRole.

setCreatedAt(LocalDateTime.now());
        userRoleMapper.

batchInsert(Collections.singletonList(userRole));
        }
        }else{
        userMapper.

updateById(user);
}
```

Add the needed imports:

```java
import java.util.Collections;
import __NAMESPACE__.common.PermissionSystemInitializer;
import __NAMESPACE__.entity.RoleEntity;
import __NAMESPACE__.entity.UserRoleEntity;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.mapper.UserRoleMapper;
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/UserServiceImpl.java
git commit -m "feat: auto-assign observer role to newly created users"
```

---

### Task 15: End-to-End Verification

- [ ] **Step 1: Generate a test application using app-generator**

Create a minimal `app.yml` with one or two forms, run the app-generator, and inspect the output to verify:

- Controllers have `@WebApiAuth` annotations
- Frontend has `v-permission` directives on buttons
- `ApiAuthFilter` contains the authorization enforcement block
- `WebApiAuthRegistry` is present
- `PermissionSystemInitializer` is present with correct role definitions
- `permission.ts` directive is registered

- [ ] **Step 2: Run existing IT tests to verify no regressions**

```bash
mvn verify -pl allison1875-cli -am
```

- [ ] **Step 3: Commit any fixes discovered during verification**
