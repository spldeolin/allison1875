# Permission Grant Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement RBAC authorization — Role CRUD form, role-permission granting, user-role granting — in both backend skeleton and frontend skeleton of app-generator.

**Architecture:** Role form added as builtin (alongside User) in `builtin-form.yml`. Two association tables (`role_permission`, `user_role`) with full-replacement grant APIs in the backend skeleton. Frontend adds page overrides (`RolePage.vue`, `UserPage.vue`) with grant modals. `PermissionEnumGenerateService` extended with `GRANT_PERMISSION` and `GRANT_ROLE` permission points.

**Tech Stack:** Java 21, Google Guice, JUnit 5, Spring Boot 2.7 (backend skeleton), MyBatis, Vue 3 + TypeScript + Naive UI

**AI/User Division of Labor:**
- **AI** writes all code changes to app-generator and skeleton files
- **Task 1** requires user interaction: AI writes a test app.yml, user generates & verifies, user moves backend code, AI moves DSL to builtin-form.yml
- All other tasks are AI-only changes to the skeleton/app-generator source

---

### Task 1: Role Form Verification & Integration into builtin-form.yml

**分工**：
- AI：编写 Role 测试用 app.yml
- 用户：使用 app-generator 生成应用，验收 Role CRUD
- 用户：将后端 Role 相关代码移动到后端骨架
- AI：将 Role DSL 移入 builtin-form.yml

**Files:**
- Create: `allison1875-cli/src/test/resources/it/app-generator/role-verify/app.yml` (临时验证用)
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/builtin-form.yml`

- [ ] **Step 1: Write test app.yml with Role form only**

Create `allison1875-cli/src/test/resources/it/app-generator/role-verify/app.yml`:

```yaml
namespace: com.example.roletest
name: RoleTest
title: 角色验证

menus:
  - group: 业务
    icon: 1
    order: 1
    form:
      name: Role
      title: 角色
      desc: 角色
      items:
        - type: text
          name: roleName
          title: 角色名称
          isNonVoid: true
          maxLength: 32
          canInputOnInit: true
          canInputOnEdit: true

        - type: text
          name: description
          title: 角色描述
          isNonVoid: false
          maxLength: 128
          canInputOnInit: true
          canInputOnEdit: true
      indices:
        - itemNames: [ roleName ]
          isUnique: true
```

- [ ] **Step 2: Hand off to user for verification**

告知用户：请使用 app-generator 生成应用并验收 Role CRUD 功能（创建、列表、编辑、删除）。验收通过后，请将后端 Role 代码（RoleEntity、RoleMapper、RoleController、RoleService 等）移入后端骨架对应目录，然后通知 AI 继续。

- [ ] **Step 3: Add Role to builtin-form.yml (after user confirms)**

Modify `app-generator/src/main/resources/frontend-skeleton/src/builtin-form.yml`, append after User menu:

```yaml
  - group: 系统
    icon: 1
    order: 2
    form:
      name: Role
      title: 角色
      desc: 角色
      items:
        - type: text
          name: roleName
          title: 角色名称
          isNonVoid: true
          maxLength: 32
          canInputOnInit: true
          canInputOnEdit: true

        - type: text
          name: description
          title: 角色描述
          isNonVoid: false
          maxLength: 128
          canInputOnInit: true
          canInputOnEdit: true
      indices:
        - itemNames: [ roleName ]
          isUnique: true
```

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/builtin-form.yml
git commit -m "feat: add Role form to builtin-form.yml as system menu"
```

---

### Task 2: Add association table DDLs to backend skeleton

**Files:**
- Modify: `app-generator/src/main/resources/backend-skeleton/sql/ddl.sql`

- [ ] **Step 1: Append role_permission and user_role DDL**

Append to end of `app-generator/src/main/resources/backend-skeleton/sql/ddl.sql`:

```sql

CREATE TABLE `role_permission` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id`         BIGINT      NOT NULL COMMENT '角色ID',
    `permission_code` VARCHAR(64) NOT NULL COMMENT '权限编码',
    `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_code`),
    PRIMARY KEY (`id`)
) COMMENT '角色-权限关联';

CREATE TABLE `user_role` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `role_id`    BIGINT   NOT NULL COMMENT '角色ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    PRIMARY KEY (`id`)
) COMMENT '用户-角色关联';
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/sql/ddl.sql
git commit -m "feat: add role_permission and user_role DDL to backend skeleton"
```

---

### Task 3: Add Entity and Mapper for association tables

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/entity/RolePermissionEntity.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/entity/UserRoleEntity.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mapper/RolePermissionMapper.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mapper/UserRoleMapper.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/resources/mapper/RolePermissionMapper.xml`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/resources/mapper/UserRoleMapper.xml`

- [ ] **Step 1: Create RolePermissionEntity**

```java
package __NAMESPACE__.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RolePermissionEntity {

    Long id;

    Long roleId;

    String permissionCode;

    LocalDateTime createdAt;

}
```

- [ ] **Step 2: Create UserRoleEntity**

```java
package __NAMESPACE__.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserRoleEntity {

    Long id;

    Long userId;

    Long roleId;

    LocalDateTime createdAt;

}
```

- [ ] **Step 3: Create RolePermissionMapper interface**

```java
package __NAMESPACE__.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import __NAMESPACE__.entity.RolePermissionEntity;

public interface RolePermissionMapper {

    int batchInsert(@Param("entities") List<RolePermissionEntity> entities);

    int deleteByRoleId(@Param("roleId") Long roleId);

    List<String> queryPermissionCodesByRoleId(@Param("roleId") Long roleId);

    List<String> queryPermissionCodesByRoleIds(@Param("roleIds") List<Long> roleIds);

}
```

- [ ] **Step 4: Create UserRoleMapper interface**

```java
package __NAMESPACE__.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import __NAMESPACE__.entity.UserRoleEntity;

public interface UserRoleMapper {

    int batchInsert(@Param("entities") List<UserRoleEntity> entities);

    int deleteByUserId(@Param("userId") Long userId);

    List<Long> queryRoleIdsByUserId(@Param("userId") Long userId);

}
```

- [ ] **Step 5: Create RolePermissionMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="__NAMESPACE__.mapper.RolePermissionMapper">

    <insert id="batchInsert">
        INSERT INTO role_permission (role_id, permission_code)
        VALUES
        <foreach collection="entities" item="one" separator=",">(#{one.roleId}, #{one.permissionCode})</foreach>
    </insert>

    <delete id="deleteByRoleId">
        DELETE FROM role_permission WHERE role_id = #{roleId}
    </delete>

    <select id="queryPermissionCodesByRoleId" resultType="java.lang.String">
        SELECT permission_code FROM role_permission WHERE role_id = #{roleId}
    </select>

    <select id="queryPermissionCodesByRoleIds" resultType="java.lang.String">
        SELECT DISTINCT permission_code FROM role_permission
        WHERE role_id IN (<foreach collection="roleIds" item="one" separator=",">#{one}</foreach>)
    </select>

</mapper>
```

- [ ] **Step 6: Create UserRoleMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="__NAMESPACE__.mapper.UserRoleMapper">

    <insert id="batchInsert">
        INSERT INTO user_role (user_id, role_id)
        VALUES
        <foreach collection="entities" item="one" separator=",">(#{one.userId}, #{one.roleId})</foreach>
    </insert>

    <delete id="deleteByUserId">
        DELETE FROM user_role WHERE user_id = #{userId}
    </delete>

    <select id="queryRoleIdsByUserId" resultType="java.lang.Long">
        SELECT role_id FROM user_role WHERE user_id = #{userId}
    </select>

</mapper>
```

- [ ] **Step 7: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/entity/RolePermissionEntity.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/entity/UserRoleEntity.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mapper/RolePermissionMapper.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mapper/UserRoleMapper.java \
       app-generator/src/main/resources/backend-skeleton/src/main/resources/mapper/RolePermissionMapper.xml \
       app-generator/src/main/resources/backend-skeleton/src/main/resources/mapper/UserRoleMapper.xml
git commit -m "feat: add Entity and Mapper for role_permission and user_role tables"
```

---

### Task 4: Add grant API DTOs

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/req/GrantPermissionsReq.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/req/ListRolePermissionsReq.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/req/GrantRolesReq.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/req/ListUserRolesReq.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/RoleBriefResp.java`

- [ ] **Step 1: Create GrantPermissionsReq**

```java
package __NAMESPACE__.dto.req;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GrantPermissionsReq {

    @NotBlank
    String roleBizId;

    @NotNull
    List<String> permissionCodes;

}
```

- [ ] **Step 2: Create ListRolePermissionsReq**

```java
package __NAMESPACE__.dto.req;

import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListRolePermissionsReq {

    @NotBlank
    String roleBizId;

}
```

- [ ] **Step 3: Create GrantRolesReq**

```java
package __NAMESPACE__.dto.req;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GrantRolesReq {

    @NotBlank
    String userBizId;

    @NotNull
    List<String> roleBizIds;

}
```

- [ ] **Step 4: Create ListUserRolesReq**

```java
package __NAMESPACE__.dto.req;

import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListUserRolesReq {

    @NotBlank
    String userBizId;

}
```

- [ ] **Step 5: Create RoleBriefResp**

```java
package __NAMESPACE__.dto.resp;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleBriefResp {

    String bizId;

    String roleName;

}
```

- [ ] **Step 6: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/req/GrantPermissionsReq.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/req/ListRolePermissionsReq.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/req/GrantRolesReq.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/req/ListUserRolesReq.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/RoleBriefResp.java
git commit -m "feat: add DTOs for grant permission and grant role APIs"
```

---

### Task 5: Add grant API Service and Controller for Role

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/RoleGrantService.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/RoleGrantServiceImpl.java`
- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/RoleController.java` (已由用户从生成代码移入骨架，在此基础上追加方法)

注意：RoleController.java 由用户在 Task 1 验收通过后移入骨架。本 Task 在其上追加 grantPermissions 和 listRolePermissions 方法。若 RoleController 尚未就位，本 Task 须在 Task 1 完成后执行。

- [ ] **Step 1: Create RoleGrantService interface**

```java
package __NAMESPACE__.service;

import java.util.List;
import __NAMESPACE__.dto.req.GrantPermissionsReq;
import __NAMESPACE__.dto.req.ListRolePermissionsReq;

public interface RoleGrantService {

    void grantPermissions(GrantPermissionsReq req);

    List<String> listRolePermissions(ListRolePermissionsReq req);

}
```

- [ ] **Step 2: Create RoleGrantServiceImpl**

```java
package __NAMESPACE__.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.dto.req.GrantPermissionsReq;
import __NAMESPACE__.dto.req.ListRolePermissionsReq;
import __NAMESPACE__.entity.RoleEntity;
import __NAMESPACE__.entity.RolePermissionEntity;
import __NAMESPACE__.enums.PermissionEnum;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.mapper.RolePermissionMapper;
import __NAMESPACE__.service.RoleGrantService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoleGrantServiceImpl implements RoleGrantService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Transactional
    @Override
    public void grantPermissions(GrantPermissionsReq req) {
        RoleEntity role = roleMapper.queryByRoleCode(req.getRoleBizId());
        if (role == null) {
            throw new BizException("角色不存在或已被删除");
        }

        Set<String> validCodes = Arrays.stream(PermissionEnum.values())
                .map(PermissionEnum::getCode)
                .collect(Collectors.toSet());
        for (String code : req.getPermissionCodes()) {
            if (!validCodes.contains(code)) {
                throw new BizException("无效的权限编码: " + code);
            }
        }

        rolePermissionMapper.deleteByRoleId(role.getId());

        if (!req.getPermissionCodes().isEmpty()) {
            List<RolePermissionEntity> entities = req.getPermissionCodes().stream()
                    .map(code -> new RolePermissionEntity().setRoleId(role.getId()).setPermissionCode(code))
                    .collect(Collectors.toList());
            rolePermissionMapper.batchInsert(entities);
        }

        log.info("granted {} permissions to role {}", req.getPermissionCodes().size(), req.getRoleBizId());
    }

    @Override
    public List<String> listRolePermissions(ListRolePermissionsReq req) {
        RoleEntity role = roleMapper.queryByRoleCode(req.getRoleBizId());
        if (role == null) {
            throw new BizException("角色不存在或已被删除");
        }
        return rolePermissionMapper.queryPermissionCodesByRoleId(role.getId());
    }

}
```

- [ ] **Step 3: Add grantPermissions and listRolePermissions to RoleController**

Append to the existing `RoleController.java` (which was moved from generated code by the user):

```java
    @Resource
    private RoleGrantService roleGrantService;

    @PostMapping("grantPermissions")
    public RequestResult<Void> grantPermissions(@RequestBody @Valid GrantPermissionsReq req) {
        roleGrantService.grantPermissions(req);
        return RequestResult.success();
    }

    @PostMapping("listRolePermissions")
    public RequestResult<List<String>> listRolePermissions(@RequestBody @Valid ListRolePermissionsReq req) {
        return RequestResult.success(roleGrantService.listRolePermissions(req));
    }
```

Add the necessary imports:

```java
import java.util.List;
import __NAMESPACE__.dto.req.GrantPermissionsReq;
import __NAMESPACE__.dto.req.ListRolePermissionsReq;
import __NAMESPACE__.service.RoleGrantService;
```

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/RoleGrantService.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/RoleGrantServiceImpl.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/RoleController.java
git commit -m "feat: add grantPermissions and listRolePermissions API to backend skeleton"
```

---

### Task 6: Add grant API Service and Controller for User-Role

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/UserGrantService.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/UserGrantServiceImpl.java`
- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/UserController.java`

- [ ] **Step 1: Create UserGrantService interface**

```java
package __NAMESPACE__.service;

import java.util.List;
import __NAMESPACE__.dto.req.GrantRolesReq;
import __NAMESPACE__.dto.req.ListUserRolesReq;
import __NAMESPACE__.dto.resp.RoleBriefResp;

public interface UserGrantService {

    void grantRoles(GrantRolesReq req);

    List<RoleBriefResp> listUserRoles(ListUserRolesReq req);

}
```

- [ ] **Step 2: Create UserGrantServiceImpl**

```java
package __NAMESPACE__.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.dto.req.GrantRolesReq;
import __NAMESPACE__.dto.req.ListUserRolesReq;
import __NAMESPACE__.dto.resp.RoleBriefResp;
import __NAMESPACE__.entity.RoleEntity;
import __NAMESPACE__.entity.UserEntity;
import __NAMESPACE__.entity.UserRoleEntity;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.mapper.UserMapper;
import __NAMESPACE__.mapper.UserRoleMapper;
import __NAMESPACE__.service.UserGrantService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserGrantServiceImpl implements UserGrantService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Transactional
    @Override
    public void grantRoles(GrantRolesReq req) {
        UserEntity user = userMapper.queryByUserCode(req.getUserBizId());
        if (user == null) {
            throw new BizException("用户不存在或已被删除");
        }

        if (!req.getRoleBizIds().isEmpty()) {
            List<RoleEntity> roles = roleMapper.queryByRoleCodes(req.getRoleBizIds());
            if (roles.size() != req.getRoleBizIds().size()) {
                throw new BizException("部分角色不存在或已被删除");
            }

            userRoleMapper.deleteByUserId(user.getId());

            List<UserRoleEntity> entities = roles.stream()
                    .map(role -> new UserRoleEntity().setUserId(user.getId()).setRoleId(role.getId()))
                    .collect(Collectors.toList());
            userRoleMapper.batchInsert(entities);
        } else {
            userRoleMapper.deleteByUserId(user.getId());
        }

        log.info("granted {} roles to user {}", req.getRoleBizIds().size(), req.getUserBizId());
    }

    @Override
    public List<RoleBriefResp> listUserRoles(ListUserRolesReq req) {
        UserEntity user = userMapper.queryByUserCode(req.getUserBizId());
        if (user == null) {
            throw new BizException("用户不存在或已被删除");
        }

        List<Long> roleIds = userRoleMapper.queryRoleIdsByUserId(user.getId());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<RoleEntity> roles = roleMapper.queryByIds(roleIds);
        return roles.stream()
                .map(role -> new RoleBriefResp().setBizId(role.getRoleCode()).setRoleName(role.getRoleName()))
                .collect(Collectors.toList());
    }

}
```

- [ ] **Step 3: Add grantRoles and listUserRoles to UserController**

Append to `UserController.java`:

```java
    @Resource
    private UserGrantService userGrantService;

    @PostMapping("grantRoles")
    public RequestResult<Void> grantRoles(@RequestBody @Valid GrantRolesReq req) {
        userGrantService.grantRoles(req);
        return RequestResult.success();
    }

    @PostMapping("listUserRoles")
    public RequestResult<List<RoleBriefResp>> listUserRoles(@RequestBody @Valid ListUserRolesReq req) {
        return RequestResult.success(userGrantService.listUserRoles(req));
    }
```

Add the necessary imports:

```java
import java.util.List;
import __NAMESPACE__.dto.req.GrantRolesReq;
import __NAMESPACE__.dto.req.ListUserRolesReq;
import __NAMESPACE__.dto.resp.RoleBriefResp;
import __NAMESPACE__.service.UserGrantService;
```

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/UserGrantService.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/UserGrantServiceImpl.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/controller/UserController.java
git commit -m "feat: add grantRoles and listUserRoles API to backend skeleton"
```

---

### Task 7: Update getCurrentUser to aggregate permissions from association tables

**Files:**
- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/AuthcServiceImpl.java`
- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/filter/ApiAuthFilter.java`

- [ ] **Step 1: Update AuthcServiceImpl.login() to fetch permissions from association tables**

In `AuthcServiceImpl.java`, add field injections:

```java
    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;
```

Add import:

```java
import __NAMESPACE__.mapper.UserRoleMapper;
import __NAMESPACE__.mapper.RolePermissionMapper;
```

Replace the `login()` method's permission fetching section (lines 55-56):

```java
        // 从 "empty list" 改为：
        // List<String> permission = Lists.newArrayList();
        // 替换为：
        List<String> permission = resolvePermissions(user.getId());
```

Add a new private method:

```java
    private List<String> resolvePermissions(Long userId) {
        List<Long> roleIds = userRoleMapper.queryRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return rolePermissionMapper.queryPermissionCodesByRoleIds(roleIds);
    }
```

Add import:

```java
import java.util.Collections;
```

- [ ] **Step 2: Update ApiAuthFilter to set permissions in CurrentUserDTO**

In `ApiAuthFilter.java`, add field injections:

```java
    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;
```

Add imports:

```java
import __NAMESPACE__.mapper.UserRoleMapper;
import __NAMESPACE__.mapper.RolePermissionMapper;
```

In the `doFilterInternal()` method, after building `CurrentUserDTO` (around line 100-102), add permissions:

```java
                // 聚合用户权限
                List<Long> roleIds = userRoleMapper.queryRoleIdsByUserId(user.getId());
                List<String> userPermissions = roleIds.isEmpty()
                        ? Collections.emptyList()
                        : rolePermissionMapper.queryPermissionCodesByRoleIds(roleIds);
                currentUser.setPermissions(userPermissions);
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/AuthcServiceImpl.java \
       app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/filter/ApiAuthFilter.java
git commit -m "feat: aggregate user permissions from role_permission via user_role tables"
```

---

### Task 8: Extend PermissionEnumGenerateService with GRANT_PERMISSION and GRANT_ROLE

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/PermissionEnumGenerateServiceImpl.java`
- Modify: `app-generator/src/test/java/com/spldeolin/allison1875/appgenerator/service/impl/PermissionEnumGenerateServiceImplTest.java`

- [ ] **Step 1: Write the failing test**

Add to `PermissionEnumGenerateServiceImplTest.java`:

```java
    @Test
    void generatePermissionEnum_containsGrantPermissionAndGrantRole() throws IOException {
        FormDef roleForm = new FormDef();
        roleForm.setName("Role");
        roleForm.setTitle("角色");

        FormDef orderForm = new FormDef();
        orderForm.setName("Order");
        orderForm.setTitle("订单");

        service.generatePermissionEnum(Arrays.asList(orderForm, roleForm), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains("GRANT_PERMISSION(\"GRANT_PERMISSION\", \"授予权限\", Group.ROLE, LIST_ROLE)"));
        assertTrue(content.contains("GRANT_ROLE(\"GRANT_ROLE\", \"授予角色\", Group.ROLE, LIST_ROLE)"));
    }

    @Test
    void generatePermissionEnum_noRoleForm_noGrantPermissions() throws IOException {
        FormDef orderForm = new FormDef();
        orderForm.setName("Order");
        orderForm.setTitle("订单");

        service.generatePermissionEnum(Collections.singletonList(orderForm), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        assertFalse(content.contains("GRANT_PERMISSION"));
        assertFalse(content.contains("GRANT_ROLE"));
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl app-generator -Dtest=PermissionEnumGenerateServiceImplTest#generatePermissionEnum_containsGrantPermissionAndGrantRole -am`
Expected: FAIL — generated source does not contain GRANT_PERMISSION or GRANT_ROLE

- [ ] **Step 3: Update PermissionEnumGenerateServiceImpl to append grant permissions for Role form**

In `PermissionEnumGenerateServiceImpl.java`, in `buildSourceCode()` method, after the main for-loop that generates CRUD permissions (after line 67 `sb.append("\n");`), but before `sb.append("    ;\n\n");` (line 73), add:

```java
        // Append GRANT_PERMISSION and GRANT_ROLE if Role form exists
        boolean hasRoleForm = allForms.stream().anyMatch(f -> "Role".equals(f.getName()));
        if (hasRoleForm) {
            sb.append("\n");
            sb.append("    GRANT_PERMISSION(\"GRANT_PERMISSION\", \"授予权限\", Group.ROLE, LIST_ROLE),\n");
            sb.append("    GRANT_ROLE(\"GRANT_ROLE\", \"授予角色\", Group.ROLE, LIST_ROLE),\n");
        }
```

Also update the log message (line 37) to account for extra permissions:

```java
        int extraPerms = allForms.stream().anyMatch(f -> "Role".equals(f.getName())) ? 2 : 0;
        log.info("generated PermissionEnum with {} permission points for {} forms",
                allForms.size() * 4 + extraPerms, allForms.size());
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -pl app-generator -Dtest=PermissionEnumGenerateServiceImplTest -am`
Expected: ALL PASS

- [ ] **Step 5: Commit**

```bash
git add app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/service/impl/PermissionEnumGenerateServiceImpl.java \
       app-generator/src/test/java/com/spldeolin/allison1875/appgenerator/service/impl/PermissionEnumGenerateServiceImplTest.java
git commit -m "feat: generate GRANT_PERMISSION and GRANT_ROLE permission points for Role form"
```

---

### Task 9: Inject GRANT_PERMISSION and GRANT_ROLE into app.json permissions

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java`
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/dsl/MenuDef.java` (if `Permissions` class needs new fields)

- [ ] **Step 1: Check MenuDef.Permissions class**

Read `MenuDef.java` to verify if `Permissions` class needs `grantPermission` and `grantRole` fields. If the frontend reads these from `app.json`, they need to be in the schema.

Actually, per the design spec, `GRANT_PERMISSION` and `GRANT_ROLE` are hardcoded in the frontend page overrides (`RolePage.vue` and `UserPage.vue`), not dynamically driven from `app.json`. So no changes needed to `MenuDef.Permissions` or `AppGenerator.generateFrontend()`.

This task is **not needed** — skip.

---

### Task 10: Create RolePage.vue frontend page override (Grant Permissions Modal)

**Files:**
- Create: `app-generator/src/main/resources/frontend-skeleton/src/pages/RolePage.vue`

- [ ] **Step 1: Create RolePage.vue**

Use ui-ux-pro-max skill for design guidance. The page extends the standard CrudPage with a "授予权限" button and modal.

```vue
<script setup lang="ts">
import { ref } from 'vue'
import type { FormDef } from '@/schema/types'
import { useCrudPage } from '@/core/composables/useCrudPage'
import SearchForm from '@/core/SearchForm.vue'
import DataTable from '@/core/DataTable.vue'
import EditModal from '@/core/EditModal.vue'
import {
  NButton, NSpace, NPopconfirm, NModal, NCheckbox, NCheckboxGroup, NDivider,
  useMessage
} from 'naive-ui'
import request from '@/utils/request'

const props = defineProps<{
  schema: FormDef
  permissions?: { list: string; create: string; update: string; delete: string }
}>()
const message = useMessage()

const {
  searchParams, tableData, tableLoading, pagination,
  checkedRowKeys, editingRowKey, bizKey,
  modalVisible, modalMode, formData, submitLoading,
  fetchData, handleSearch, handleReset,
  handleCreate, handleEdit, handleDelete, handleBatchDelete,
  handleSubmit, handlePaginationUpdate,
} = useCrudPage(() => props.schema)

// ─── Grant Permissions Modal ─────────────────────────────────
interface PermissionItem {
  code: string
  title: string
  baseOn: string | null
}
interface PermissionGroup {
  groupCode: string
  groupTitle: string
  permissions: PermissionItem[]
}

const grantModalVisible = ref(false)
const grantModalTitle = ref('')
const grantRoleBizId = ref('')
const allPermissionGroups = ref<PermissionGroup[]>([])
const selectedPermissionCodes = ref<string[]>([])
const grantLoading = ref(false)

async function handleOpenGrantModal(row: Record<string, any>) {
  grantRoleBizId.value = row[bizKey.value] as string
  grantModalTitle.value = `授予权限 — ${row.roleName}`
  grantLoading.value = true
  grantModalVisible.value = true

  try {
    const [permRes, rolePermRes] = await Promise.all([
      request.post('/api/v1/permission/listPermissions'),
      request.post('/api/v1/role/listRolePermissions', { roleBizId: grantRoleBizId.value }),
    ])
    allPermissionGroups.value = permRes.data.data as PermissionGroup[]
    selectedPermissionCodes.value = rolePermRes.data.data as string[]
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '加载权限数据失败')
    grantModalVisible.value = false
  } finally {
    grantLoading.value = false
  }
}

function isGroupAllSelected(group: PermissionGroup): boolean {
  return group.permissions.every(p => selectedPermissionCodes.value.includes(p.code))
}

function isGroupPartialSelected(group: PermissionGroup): boolean {
  const selected = group.permissions.filter(p => selectedPermissionCodes.value.includes(p.code))
  return selected.length > 0 && selected.length < group.permissions.length
}

function toggleGroupAll(group: PermissionGroup, checked: boolean) {
  const codes = group.permissions.map(p => p.code)
  if (checked) {
    const set = new Set([...selectedPermissionCodes.value, ...codes])
    selectedPermissionCodes.value = [...set]
  } else {
    selectedPermissionCodes.value = selectedPermissionCodes.value.filter(c => !codes.includes(c))
  }
}

async function handleGrantSubmit() {
  grantLoading.value = true
  try {
    await request.post('/api/v1/role/grantPermissions', {
      roleBizId: grantRoleBizId.value,
      permissionCodes: selectedPermissionCodes.value,
    })
    message.success('权限授予成功')
    grantModalVisible.value = false
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '权限授予失败')
  } finally {
    grantLoading.value = false
  }
}
</script>

<template>
  <div class="crud-page">
    <div class="crud-search-card">
      <SearchForm
        :items="schema.items"
        v-model="searchParams"
        @search="handleSearch"
        @reset="handleReset"
      />
    </div>
    <div class="crud-table-card">
      <div class="crud-table-header">
        <h3 class="crud-table-title">{{ schema.title }}</h3>
        <NSpace>
          <NButton type="primary" :func-permission="permissions?.create" @click="handleCreate">创建</NButton>
          <NPopconfirm
            :disabled="checkedRowKeys.length === 0"
            @positive-click="handleBatchDelete"
          >
            <template #trigger>
              <NButton
                type="error"
                :disabled="checkedRowKeys.length === 0"
                :func-permission="permissions?.delete"
              >
                批量删除{{ checkedRowKeys.length > 0 ? `（${checkedRowKeys.length}）` : '' }}
              </NButton>
            </template>
            确定要删除选中的 {{ checkedRowKeys.length }} 条记录吗？
          </NPopconfirm>
        </NSpace>
      </div>
      <DataTable
        :items="schema.items"
        :data="tableData"
        :loading="tableLoading"
        :form-title="schema.title"
        :editing-row-key="editingRowKey"
        :biz-key="bizKey"
        :pagination="pagination"
        :checked-row-keys="checkedRowKeys"
        :permissions="permissions"
        @edit="handleEdit"
        @delete="handleDelete"
        @update:pagination="handlePaginationUpdate"
        @update:checked-row-keys="checkedRowKeys = $event"
      >
        <template #actions="{ row }">
          <NButton
            size="small"
            quaternary
            type="info"
            func-permission="GRANT_PERMISSION"
            @click="handleOpenGrantModal(row)"
          >授予权限</NButton>
        </template>
      </DataTable>
    </div>
    <EditModal
      :visible="modalVisible"
      :mode="modalMode"
      :form-title="schema.title"
      :items="schema.items"
      v-model="formData"
      :loading="submitLoading"
      @update:visible="modalVisible = $event"
      @submit="handleSubmit"
    />

    <!-- Grant Permissions Modal -->
    <NModal
      v-model:show="grantModalVisible"
      preset="card"
      :title="grantModalTitle"
      :style="{ width: '600px' }"
      :mask-closable="false"
    >
      <div v-if="grantLoading" style="text-align: center; padding: 40px 0; color: #94a3b8">
        加载中...
      </div>
      <div v-else>
        <NCheckboxGroup v-model:value="selectedPermissionCodes">
          <div v-for="group in allPermissionGroups" :key="group.groupCode" style="margin-bottom: 16px">
            <div style="display: flex; align-items: center; margin-bottom: 8px">
              <NCheckbox
                :checked="isGroupAllSelected(group)"
                :indeterminate="isGroupPartialSelected(group)"
                @update:checked="(v: boolean) => toggleGroupAll(group, v)"
              >
                <span style="font-weight: 600; font-size: 14px; color: #1e293b">{{ group.groupTitle }}</span>
              </NCheckbox>
            </div>
            <div style="padding-left: 24px; display: flex; flex-wrap: wrap; gap: 8px 16px">
              <NCheckbox
                v-for="perm in group.permissions"
                :key="perm.code"
                :value="perm.code"
                :label="perm.title"
              />
            </div>
            <NDivider style="margin: 12px 0" />
          </div>
        </NCheckboxGroup>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px">
          <NButton @click="grantModalVisible = false">取消</NButton>
          <NButton type="primary" :loading="grantLoading" @click="handleGrantSubmit">确定</NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.crud-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.crud-search-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  border: 1px solid #f1f5f9;
  flex-shrink: 0;
}
.crud-table-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  border: 1px solid #f1f5f9;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.crud-table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.crud-table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
</style>
```

**注意**: DataTable 组件当前不支持 `#actions` 插槽。需要在 Task 11 中为 DataTable 添加自定义行操作插槽，或者直接在 RolePage.vue 中通过不同方式注入"授予权限"按钮。具体方案在 Task 11 确定。

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/pages/RolePage.vue
git commit -m "feat: add RolePage.vue with grant permissions modal"
```

---

### Task 11: Extend DataTable to support custom row action buttons

DataTable 当前行操作栏硬编码了"编辑"和"删除"按钮。为了让 RolePage 和 UserPage 能注入自定义行操作按钮（如"授予权限"、"授予角色"），需要为 DataTable 添加 `extraActions` prop。

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/core/DataTable.vue`

- [ ] **Step 1: Add extraActions prop to DataTable**

In `DataTable.vue`, add a new prop:

```typescript
  /** Extra action buttons to render in the actions column */
  extraActions?: Array<{
    label: string
    type?: 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error'
    permission?: string
    onClick: (row: Record<string, any>) => void
  }>
```

In the actions column render function (the `cols.push({ title: '操作', ... })` block), insert extra action buttons before the "编辑" button:

```typescript
        default: () => [
          ...(props.extraActions || []).map(action =>
            h(NButton, {
              size: 'small',
              quaternary: true,
              type: action.type || 'info',
              disabled: props.editingRowKey != null,
              'func-permission': action.permission,
              onClick: () => action.onClick(row)
            }, { default: () => action.label })
          ),
          // existing edit and delete buttons...
        ]
```

Adjust the actions column width to accommodate extra buttons: change `width: 120` to a computed value based on `extraActions` length:

```typescript
    width: 120 + (props.extraActions?.length ?? 0) * 80,
```

- [ ] **Step 2: Update RolePage.vue to use extraActions prop**

Remove the `<template #actions>` slot approach and instead pass `extraActions` prop to DataTable:

```vue
      <DataTable
        :items="schema.items"
        :data="tableData"
        :loading="tableLoading"
        :form-title="schema.title"
        :editing-row-key="editingRowKey"
        :biz-key="bizKey"
        :pagination="pagination"
        :checked-row-keys="checkedRowKeys"
        :permissions="permissions"
        :extra-actions="[
          { label: '授予权限', type: 'info', permission: 'GRANT_PERMISSION', onClick: handleOpenGrantModal }
        ]"
        @edit="handleEdit"
        @delete="handleDelete"
        @update:pagination="handlePaginationUpdate"
        @update:checked-row-keys="checkedRowKeys = $event"
      />
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/core/DataTable.vue \
       app-generator/src/main/resources/frontend-skeleton/src/pages/RolePage.vue
git commit -m "feat: add extraActions prop to DataTable for custom row action buttons"
```

---

### Task 12: Create UserPage.vue frontend page override (Grant Roles Modal)

**Files:**
- Create: `app-generator/src/main/resources/frontend-skeleton/src/pages/UserPage.vue`

- [ ] **Step 1: Create UserPage.vue**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import type { FormDef } from '@/schema/types'
import { useCrudPage } from '@/core/composables/useCrudPage'
import SearchForm from '@/core/SearchForm.vue'
import DataTable from '@/core/DataTable.vue'
import EditModal from '@/core/EditModal.vue'
import {
  NButton, NSpace, NPopconfirm, NModal, NCheckbox, NCheckboxGroup,
  useMessage
} from 'naive-ui'
import request from '@/utils/request'

const props = defineProps<{
  schema: FormDef
  permissions?: { list: string; create: string; update: string; delete: string }
}>()
const message = useMessage()

const {
  searchParams, tableData, tableLoading, pagination,
  checkedRowKeys, editingRowKey, bizKey,
  modalVisible, modalMode, formData, submitLoading,
  fetchData, handleSearch, handleReset,
  handleCreate, handleEdit, handleDelete, handleBatchDelete,
  handleSubmit, handlePaginationUpdate,
} = useCrudPage(() => props.schema)

// ─── Grant Roles Modal ─────────────────────────────────
interface RoleBrief {
  bizId: string
  roleName: string
}

const grantModalVisible = ref(false)
const grantModalTitle = ref('')
const grantUserBizId = ref('')
const allRoles = ref<RoleBrief[]>([])
const selectedRoleBizIds = ref<string[]>([])
const grantLoading = ref(false)

async function handleOpenGrantModal(row: Record<string, any>) {
  grantUserBizId.value = row[bizKey.value] as string
  grantModalTitle.value = `授予角色 — ${row.username}`
  grantLoading.value = true
  grantModalVisible.value = true

  try {
    const [rolesRes, userRolesRes] = await Promise.all([
      request.post('/api/v1/role/listRoles', { pageNum: 1, pageSize: 9999 }),
      request.post('/api/v1/user/listUserRoles', { userBizId: grantUserBizId.value }),
    ])
    const pageResult = rolesRes.data.data as { list: Record<string, any>[] }
    allRoles.value = pageResult.list.map((r: Record<string, any>) => ({
      bizId: r.roleCode as string,
      roleName: r.roleName as string,
    }))
    const userRoles = userRolesRes.data.data as RoleBrief[]
    selectedRoleBizIds.value = userRoles.map(r => r.bizId)
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '加载角色数据失败')
    grantModalVisible.value = false
  } finally {
    grantLoading.value = false
  }
}

async function handleGrantSubmit() {
  grantLoading.value = true
  try {
    await request.post('/api/v1/user/grantRoles', {
      userBizId: grantUserBizId.value,
      roleBizIds: selectedRoleBizIds.value,
    })
    message.success('角色授予成功')
    grantModalVisible.value = false
  } catch (e: unknown) {
    message.error((e instanceof Error ? e.message : String(e)) || '角色授予失败')
  } finally {
    grantLoading.value = false
  }
}
</script>

<template>
  <div class="crud-page">
    <div class="crud-search-card">
      <SearchForm
        :items="schema.items"
        v-model="searchParams"
        @search="handleSearch"
        @reset="handleReset"
      />
    </div>
    <div class="crud-table-card">
      <div class="crud-table-header">
        <h3 class="crud-table-title">{{ schema.title }}</h3>
        <NSpace>
          <NButton type="primary" :func-permission="permissions?.create" @click="handleCreate">创建</NButton>
          <NPopconfirm
            :disabled="checkedRowKeys.length === 0"
            @positive-click="handleBatchDelete"
          >
            <template #trigger>
              <NButton
                type="error"
                :disabled="checkedRowKeys.length === 0"
                :func-permission="permissions?.delete"
              >
                批量删除{{ checkedRowKeys.length > 0 ? `（${checkedRowKeys.length}）` : '' }}
              </NButton>
            </template>
            确定要删除选中的 {{ checkedRowKeys.length }} 条记录吗？
          </NPopconfirm>
        </NSpace>
      </div>
      <DataTable
        :items="schema.items"
        :data="tableData"
        :loading="tableLoading"
        :form-title="schema.title"
        :editing-row-key="editingRowKey"
        :biz-key="bizKey"
        :pagination="pagination"
        :checked-row-keys="checkedRowKeys"
        :permissions="permissions"
        :extra-actions="[
          { label: '授予角色', type: 'info', permission: 'GRANT_ROLE', onClick: handleOpenGrantModal }
        ]"
        @edit="handleEdit"
        @delete="handleDelete"
        @update:pagination="handlePaginationUpdate"
        @update:checked-row-keys="checkedRowKeys = $event"
      />
    </div>
    <EditModal
      :visible="modalVisible"
      :mode="modalMode"
      :form-title="schema.title"
      :items="schema.items"
      v-model="formData"
      :loading="submitLoading"
      @update:visible="modalVisible = $event"
      @submit="handleSubmit"
    />

    <!-- Grant Roles Modal -->
    <NModal
      v-model:show="grantModalVisible"
      preset="card"
      :title="grantModalTitle"
      :style="{ width: '480px' }"
      :mask-closable="false"
    >
      <div v-if="grantLoading" style="text-align: center; padding: 40px 0; color: #94a3b8">
        加载中...
      </div>
      <div v-else>
        <div v-if="allRoles.length === 0" style="text-align: center; padding: 20px; color: #94a3b8">
          暂无可用角色
        </div>
        <NCheckboxGroup v-else v-model:value="selectedRoleBizIds">
          <div style="display: flex; flex-direction: column; gap: 8px">
            <NCheckbox
              v-for="role in allRoles"
              :key="role.bizId"
              :value="role.bizId"
              :label="role.roleName"
            />
          </div>
        </NCheckboxGroup>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px">
          <NButton @click="grantModalVisible = false">取消</NButton>
          <NButton type="primary" :loading="grantLoading" @click="handleGrantSubmit">确定</NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.crud-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.crud-search-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  border: 1px solid #f1f5f9;
  flex-shrink: 0;
}
.crud-table-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  border: 1px solid #f1f5f9;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.crud-table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.crud-table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/pages/UserPage.vue
git commit -m "feat: add UserPage.vue with grant roles modal"
```

---

### Task 13: Run full test suite and verify

**Files:**
- No file changes

- [ ] **Step 1: Run all tests**

```bash
mvn verify
```

Expected: ALL PASS

- [ ] **Step 2: Update the permission definition spec with milestone completion**

Append the "已完成：授权模块" section from the spec (`docs/superpowers/specs/2026-06-20-permission-grant-design.md` section 七) to the permission definition spec.

- [ ] **Step 3: Final commit**

```bash
git add docs/superpowers/specs/
git commit -m "docs: mark permission grant module as completed milestone"
```
