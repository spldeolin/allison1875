# 功能权限 — 权限定义模块设计

## 概述

为 app-generator 生成的应用增加 RBAC 功能权限体系。本文档覆盖第一阶段"权限定义"模块，包括权限枚举生成、权限查询接口、前端权限映射。

整体路线分三个阶段（三个会话）：
1. **权限定义**（本阶段）：定义权限点枚举、查询接口、前端映射声明
2. **授权**：角色 CRUD、角色-权限分配、用户-角色分配
3. **鉴权**：后端接口拦截、前端按钮/菜单显隐、路由守卫

## 设计决策

| 决策项 | 结论 |
|--------|------|
| 枚举项命名格式 | 动词+表单名：`LIST_ORDER`, `CREATE_ORDER`, `UPDATE_ORDER`, `DELETE_ORDER` |
| 权限查询接口返回格式 | 按 group 分组：`{groupCode, groupTitle, permissions: [{code, title, baseOn}]}` |
| 前端权限映射方式 | 显式声明在 `app.json` 的 `permissions` 字段中 |
| save 操作权限粒度 | 拆分为 `CREATE_X` 和 `UPDATE_X` 两个独立权限点 |
| 枚举生成方式 | 字符串拼接（非 AST 操作），实现简洁 |
| 架构模式 | 骨架声明空壳枚举结构，app-generator 填充枚举项 |

## 一、权限枚举结构（后端骨架）

### 文件位置

`src/main/java/__NAMESPACE_PATH__/enums/PermissionEnum.java`

### 骨架中的空壳结构

```java
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

### 生成后示例

```java
@Getter
@AllArgsConstructor
public enum PermissionEnum {

    LIST_ORDER("LIST_ORDER", "查看订单", Group.ORDER, null),
    CREATE_ORDER("CREATE_ORDER", "创建订单", Group.ORDER, LIST_ORDER),
    UPDATE_ORDER("UPDATE_ORDER", "编辑订单", Group.ORDER, LIST_ORDER),
    DELETE_ORDER("DELETE_ORDER", "删除订单", Group.ORDER, LIST_ORDER),

    LIST_PRODUCT("LIST_PRODUCT", "查看商品", Group.PRODUCT, null),
    CREATE_PRODUCT("CREATE_PRODUCT", "创建商品", Group.PRODUCT, LIST_PRODUCT),
    UPDATE_PRODUCT("UPDATE_PRODUCT", "编辑商品", Group.PRODUCT, LIST_PRODUCT),
    DELETE_PRODUCT("DELETE_PRODUCT", "删除商品", Group.PRODUCT, LIST_PRODUCT),
    ;

    private final String code;
    private final String title;
    private final Group group;
    private final PermissionEnum baseOn;

    @Getter
    @AllArgsConstructor
    public enum Group {
        ORDER("ORDER", "订单管理"),
        PRODUCT("PRODUCT", "商品管理"),
        ;

        private final String code;
        private final String title;
    }

}
```

### 命名规则

- 每个表单生成 4 个权限点：`LIST_X`, `CREATE_X`, `UPDATE_X`, `DELETE_X`
- `X` 为表单名转 UPPER_SNAKE（如 `Order` → `ORDER`，`UserProfile` → `USER_PROFILE`）
- `LIST_X` 的 `baseOn` 为 `null`，其余三个的 `baseOn` 均指向 `LIST_X`
- `LIST_X` 同时控制 list 和 getDetail 两个接口
- Group 枚举项命名与表单对应，title 格式为 `"{form.title}管理"`

### title 规则

| 权限点 | title |
|--------|-------|
| `LIST_X` | `"查看{form.title}"` |
| `CREATE_X` | `"创建{form.title}"` |
| `UPDATE_X` | `"编辑{form.title}"` |
| `DELETE_X` | `"删除{form.title}"` |

## 二、权限枚举生成服务（app-generator）

### 接口

```java
// app-generator/src/main/java/.../appgenerator/service/PermissionEnumGenerateService.java
public interface PermissionEnumGenerateService {
    void generatePermissionEnum(List<FormDef> allForms, Path backendOutputRoot, String namespace);
}
```

### 实现要点

- 接收所有表单列表（用户定义的 menus 中的 form + builtin 的 User 表单）
- 通过字符串拼接构造完整的 `PermissionEnum.java` 源码
- 写入到 `backendOutputRoot/src/main/java/{namespacePath}/enums/PermissionEnum.java`，覆盖骨架中的空壳文件
- 实现类用 `@Singleton` + `@Slf4j`，接口用 `@ImplementedBy`

### 调用时机

在 `AppGenerator.play()` 中：
1. `generateBackend()` — 拷贝骨架 + 占位符替换
2. **`permissionEnumGenerateService.generatePermissionEnum()`** — 填充权限枚举
3. `invokeFormGenerator()` — 委托 form-generator 生成 CRUD 代码

### 单元测试

- 验证给定一组 FormDef，生成的 Java 源码包含正确数量的枚举项
- 验证命名规则正确（UPPER_SNAKE 转换）
- 验证 baseOn 引用关系正确
- 验证 Group 内部枚举与表单一一对应
- 验证 builtin User 表单也生成权限点

## 三、权限 Controller（后端骨架）

### 文件位置

`src/main/java/__NAMESPACE_PATH__/controller/PermissionController.java`

### 接口

```
POST /api/v1/permission/listPermissions
Response: RequestResult<List<PermissionGroupResp>>
```

### 实现

```java
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

### DTO

```java
// dto/resp/PermissionGroupResp.java
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionGroupResp {
    String groupCode;
    String groupTitle;
    List<PermissionResp> permissions;
}

// dto/resp/PermissionResp.java
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionResp {
    String code;
    String title;
    String baseOn;
}
```

### 设计要点

- 纯骨架代码，逻辑固定（遍历枚举值），无需 app-generator 生成
- 枚举为空时返回空列表，不报错
- 本阶段无鉴权保护（权限列表是公开元数据）

## 四、前端权限映射

### 4.1 app.json 中的 permissions 字段

app-generator 生成 `src/app.json` 时，为每个 menu 追加 `permissions` 对象：

```json
{
  "menus": [
    {
      "group": "业务",
      "icon": "ShoppingCart",
      "order": 1,
      "form": { "name": "Order", "title": "订单", "items": [] },
      "permissions": {
        "list": "LIST_ORDER",
        "create": "CREATE_ORDER",
        "update": "UPDATE_ORDER",
        "delete": "DELETE_ORDER"
      }
    }
  ]
}
```

### 4.2 按钮与权限点的关联

通过 `data-permission` 属性在按钮元素上声明所需权限代码：

```html
<!-- DataTable.vue -->
<n-button :data-permission="schema.permissions?.create" @click="handleCreate">新建</n-button>
<n-button :data-permission="schema.permissions?.update" @click="handleEdit(row)">编辑</n-button>
<n-button :data-permission="schema.permissions?.delete" @click="handleDelete(row)">删除</n-button>
```

### 4.3 菜单与权限点的关联

`DashboardLayout.vue` 中每个菜单项携带 `permissions.list` 作为标识。

### 4.4 本阶段范围

- 只建立映射关系，按钮仍全部展示
- 后续"鉴权模块"基于 `data-permission` 实现 `v-permission` 指令或组合式函数控制显隐
- `permissions` 从 `app.json` 通过 router props 传入 CrudPage → DataTable

## 五、里程碑提示词（写入 spec 末尾）

权限定义模块完成后，在本文档末尾追加以下内容，为后续会话提供上下文：

---

## 已完成：权限定义模块

### 后端
- `PermissionEnum` 位于 `{namespace}.enums.PermissionEnum`
- 每个表单 4 个权限点：LIST_X, CREATE_X, UPDATE_X, DELETE_X
- 内部枚举 `Group`，每个表单一个 group
- 每个权限点有 code, title, group, baseOn 属性
- `PermissionController` 提供 `POST /api/v1/permission/listPermissions` 接口，返回按 group 分组的权限列表
- User 表单（builtin）同样生成权限点

### 前端
- `app.json` 每个 menu 包含 `permissions: {list, create, update, delete}` 映射
- 按钮通过 `data-permission` 属性声明所需权限代码
- 菜单项通过 `permissions.list` 标识

### 下一步：授权模块
需要实现：
1. 角色表（RBAC 的 R）：角色 CRUD
2. 角色-权限关联：为角色分配权限点
3. 用户-角色关联：为用户分配角色
4. 授权管理界面

### 再下一步：鉴权模块
需要实现：
1. 后端接口鉴权拦截器（基于当前用户角色→权限判断）
2. 前端按钮/菜单显隐（基于 data-permission + 当前用户权限列表）
3. 前端路由守卫（基于 permissions.list 控制菜单可见性）
