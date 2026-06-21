# User Granted Permissions Column Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a "已授予权限" column to the user list page showing roles as Tags with a hover Popover displaying grouped permissions, and rename the Role menu from "角色" to "角色权限".

**Architecture:** Backend enriches the user list response with per-user role and permission data via batch joins. Frontend replaces the core DataTable with raw NDataTable for full column control, adding a custom column with NTag + NPopover.

**Tech Stack:** Java 8 / Spring Boot / MyBatis (backend skeleton) · Vue 3 / Naive UI / TypeScript (frontend skeleton)

---

## File Structure

| File | Action | Responsibility |
|------|--------|---------------|
| `backend-skeleton/.../mapper/UserRoleMapper.java` | Modify | Add batch query method |
| `backend-skeleton/.../mapper/UserRoleMapper.xml` | Modify | Add batch query SQL |
| `backend-skeleton/.../dto/resp/ListUsersResp.java` | Modify | Add grantedRoles + grantedPermissions fields |
| `backend-skeleton/.../service/impl/UserServiceImpl.java` | Modify | Enrich list response with role/permission data |
| `frontend-skeleton/src/builtin-form.yml` | Modify | Rename Role title |
| `frontend-skeleton/src/pages/UserPage.vue` | Modify | Replace DataTable with NDataTable + custom column |

---

### Task 1: Backend — Add batch user-role query to UserRoleMapper

**Files:**
- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mapper/UserRoleMapper.java`
- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/resources/mapper/UserRoleMapper.xml`

- [ ] **Step 1: Add `queryByUserIds` method to UserRoleMapper.java**

Add the following method to the `UserRoleMapper` interface (after the existing `queryRoleIdsByUserId` method):

```java
List<UserRoleEntity> queryByUserIds(@Param("userIds") List<Long> userIds);
```

The final file should have these imports and methods:

```java
package __NAMESPACE__.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import __NAMESPACE__.entity.UserRoleEntity;

public interface UserRoleMapper {

    int batchInsert(@Param("entities") List<UserRoleEntity> entities);

    int deleteByUserId(@Param("userId") Long userId);

    List<Long> queryRoleIdsByUserId(@Param("userId") Long userId);

    List<UserRoleEntity> queryByUserIds(@Param("userIds") List<Long> userIds);

}
```

- [ ] **Step 2: Add corresponding SQL to UserRoleMapper.xml**

Add the following `<select>` element after the existing `queryRoleIdsByUserId` select:

```xml
    <select id="queryByUserIds" resultType="__NAMESPACE__.entity.UserRoleEntity">
        SELECT id, user_id, role_id, created_at
        FROM user_role
        WHERE user_id IN (<foreach collection="userIds" item="one" separator=",">#{one}</foreach>)
    </select>
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mapper/UserRoleMapper.java app-generator/src/main/resources/backend-skeleton/src/main/resources/mapper/UserRoleMapper.xml
git commit -m "feat: add batch user-role query to UserRoleMapper"
```

---

### Task 2: Backend — Enrich ListUsersResp with roles and permissions

**Files:**
- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/ListUsersResp.java`
- Modify: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/UserServiceImpl.java`

- [ ] **Step 1: Add grantedRoles and grantedPermissions to ListUsersResp.java**

Add the following fields after the `updatedAt` field:

```java
    /**
     * 已授予角色
     */
    List<RoleBriefResp> grantedRoles;

    /**
     * 已授予权限（取并集后的 permission code 列表）
     */
    List<String> grantedPermissions;
```

And add the import at the top:

```java
import java.util.List;
```

The complete file:

```java
package __NAMESPACE__.dto.resp;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-07
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ListUsersResp {

    /**
     * 业务主键
     */
    String userCode;

    /**
     * 用户名
     */
    String username;

    /**
     * 用户昵称
     */
    String nickName;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime lastLoginAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime updatedAt;

    /**
     * 已授予角色
     */
    List<RoleBriefResp> grantedRoles;

    /**
     * 已授予权限（取并集后的 permission code 列表）
     */
    List<String> grantedPermissions;

}
```

- [ ] **Step 2: Modify UserServiceImpl.listUsers() to enrich response**

Replace the `listUsers` method body in `UserServiceImpl.java`. After the existing DTO mapping loop, add batch role/permission enrichment logic.

The new `listUsers` method:

```java
    @Override
    public PageResult<ListUsersResp> listUsers(ListUsersReq req) {
        final QueryUserParam queryUserParam = new QueryUserParam();
        queryUserParam.setUserCode(req.getUserCode());
        queryUserParam.setUsername(req.getUsername());
        queryUserParam.setNickName(req.getNickName());
        queryUserParam.setLastLoginAt(req.getLastLoginAtStart() == null ? null : req.getLastLoginAtStart());
        queryUserParam.setLastLoginAtEx(req.getLastLoginAtEnd() == null ? null : req.getLastLoginAtEnd());
        queryUserParam.setCreatedAt(req.getCreatedAtStart() == null ? null : req.getCreatedAtStart());
        queryUserParam.setCreatedAtEx(req.getCreatedAtEnd() == null ? null : req.getCreatedAtEnd());
        queryUserParam.setOffset((req.getPageNum() - 1) * req.getPageSize());
        queryUserParam.setLimit(req.getPageSize());
        long queryUserTotal = userMapper.countUser(queryUserParam);
        List<UserEntity> users = userMapper.queryUserEx(queryUserParam);
        if (users.isEmpty()) {
            return PageResult.empty();
        }
        List<ListUsersResp> dtos = new ArrayList<>();
        for (UserEntity user : users) {
            ListUsersResp dto = new ListUsersResp();
            dto.setUserCode(user.getUserCode());
            dto.setUsername(user.getUsername());
            dto.setNickName(user.getNickName());
            dto.setLastLoginAt(user.getLastLoginAt());
            dto.setCreatedAt(user.getCreatedAt());
            dto.setUpdatedAt(user.getUpdatedAt());
            dtos.add(dto);
        }

        // Enrich with granted roles and permissions
        List<Long> userIds = users.stream().map(UserEntity::getId).collect(Collectors.toList());
        List<UserRoleEntity> allUserRoles = userRoleMapper.queryByUserIds(userIds);

        // Build userId -> List<roleId> map
        Map<Long, List<Long>> userRoleMap = allUserRoles.stream()
                .collect(Collectors.groupingBy(UserRoleEntity::getUserId,
                        Collectors.mapping(UserRoleEntity::getRoleId, Collectors.toList())));

        // Collect all role IDs and batch-query role info
        List<Long> allRoleIds = allUserRoles.stream()
                .map(UserRoleEntity::getRoleId).distinct().collect(Collectors.toList());
        Map<Long, RoleEntity> roleMap = Collections.emptyMap();
        Map<Long, List<String>> rolePermMap = Collections.emptyMap();
        if (!allRoleIds.isEmpty()) {
            List<RoleEntity> roles = roleMapper.queryByIds(allRoleIds);
            roleMap = roles.stream().collect(Collectors.toMap(RoleEntity::getId, r -> r));
            rolePermMap = rolePermissionMapper.queryByRoleIds(allRoleIds).stream()
                    .collect(Collectors.groupingBy(RolePermissionEntity::getRoleId,
                            Collectors.mapping(RolePermissionEntity::getPermissionCode, Collectors.toList())));
        }

        // Enrich each dto
        for (int i = 0; i < users.size(); i++) {
            UserEntity user = users.get(i);
            ListUsersResp dto = dtos.get(i);
            List<Long> roleIds = userRoleMap.getOrDefault(user.getId(), Collections.emptyList());
            List<RoleBriefResp> grantedRoles = new ArrayList<>();
            Set<String> permSet = new LinkedHashSet<>();
            for (Long roleId : roleIds) {
                RoleEntity role = roleMap.get(roleId);
                if (role != null) {
                    grantedRoles.add(new RoleBriefResp().setBizId(role.getRoleCode()).setRoleName(role.getRoleName()));
                }
                List<String> perms = rolePermMap.getOrDefault(roleId, Collections.emptyList());
                permSet.addAll(perms);
            }
            dto.setGrantedRoles(grantedRoles);
            dto.setGrantedPermissions(new ArrayList<>(permSet));
        }

        return PageResult.of(queryUserTotal, dtos);
    }
```

Add the following imports to `UserServiceImpl.java`:

```java
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import __NAMESPACE__.dto.resp.RoleBriefResp;
import __NAMESPACE__.entity.RolePermissionEntity;
import __NAMESPACE__.mapper.RolePermissionMapper;
```

Add the `RolePermissionMapper` field injection:

```java
    @Resource
    private RolePermissionMapper rolePermissionMapper;
```

- [ ] **Step 3: Add `queryByRoleIds` to RolePermissionMapper**

Note: `RoleMapper.queryByIds(List<Long> ids)` already exists — no changes needed there.

Add to `RolePermissionMapper.java` (after existing methods):

```java
    List<RolePermissionEntity> queryByRoleIds(@Param("roleIds") List<Long> roleIds);
```

Add to `RolePermissionMapper.xml` (after the existing `queryPermissionCodesByRoleIds` select):

```xml
    <select id="queryByRoleIds" resultType="__NAMESPACE__.entity.RolePermissionEntity">
        SELECT id, role_id, permission_code, created_at
        FROM role_permission
        WHERE role_id IN (<foreach collection="roleIds" item="one" separator=",">#{one}</foreach>)
    </select>
```

- [ ] **Step 4: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/ListUsersResp.java \
  app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/UserServiceImpl.java \
  app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mapper/RolePermissionMapper.java \
  app-generator/src/main/resources/backend-skeleton/src/main/resources/mapper/RolePermissionMapper.xml
git commit -m "feat: enrich user list response with granted roles and permissions"
```

---

### Task 3: Frontend — Rename Role menu to "角色权限"

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/builtin-form.yml`

- [ ] **Step 1: Change Role form title and desc**

In `builtin-form.yml`, change lines 61-62:

```yaml
# Before
      title: 角色
      desc: 角色

# After
      title: 角色权限
      desc: 角色权限
```

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/builtin-form.yml
git commit -m "feat: rename Role menu from '角色' to '角色权限'"
```

---

### Task 4: Frontend — Rewrite UserPage.vue with custom permissions column

**Files:**
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/pages/UserPage.vue`

- [ ] **Step 1: Rewrite UserPage.vue**

Replace the entire content of `UserPage.vue` with the following implementation that uses `NDataTable` directly (instead of the core DataTable component) to support a custom "已授予权限" column:

```vue
<script setup lang="ts">
import { h, ref, computed, onMounted } from 'vue'
import type { FormDef } from '@/schema/types'
import { useCrudPage } from '@/core/composables/useCrudPage'
import SearchForm from '@/core/SearchForm.vue'
import EditModal from '@/core/EditModal.vue'
import {
  NDataTable, NButton, NSpace, NPopconfirm, NModal, NCheckbox, NCheckboxGroup,
  NTag, NPopover,
  useMessage
} from 'naive-ui'
import type { DataTableColumn, PaginationProps } from 'naive-ui'
import request from '@/utils/request'
import { checkPermission } from '@/directives/usePermission'

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

// ─── Permission Groups (for popover display) ───────────────
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

const allPermissionGroups = ref<PermissionGroup[]>([])

onMounted(async () => {
  try {
    const res = await request.post('/api/v1/permission/listPermissions')
    allPermissionGroups.value = res.data.data as PermissionGroup[]
  } catch (_) {
    // silent — popover will show codes if groups fail to load
  }
})

function getPermissionTitle(code: string): string {
  for (const group of allPermissionGroups.value) {
    const found = group.permissions.find(p => p.code === code)
    if (found) return found.title
  }
  return code
}

function getGroupedPermissions(codes: string[]): { groupTitle: string; items: string[] }[] {
  const codeSet = new Set(codes)
  const result: { groupTitle: string; items: string[] }[] = []
  for (const group of allPermissionGroups.value) {
    const matched = group.permissions.filter(p => codeSet.has(p.code))
    if (matched.length > 0) {
      result.push({ groupTitle: group.groupTitle, items: matched.map(p => p.title) })
    }
  }
  return result
}

// ─── Table Columns ────────────────────────────────────────
const MAX_VISIBLE_TAGS = 3

const columns = computed<DataTableColumn[]>(() => {
  const cols: DataTableColumn[] = [
    { type: 'selection', fixed: 'left' },
    {
      title: `${props.schema.title}ID`,
      key: bizKey.value,
      width: 140,
      fixed: 'left',
      ellipsis: { tooltip: true },
    },
    {
      title: '用户名',
      key: 'username',
      width: 150,
      fixed: 'left',
      ellipsis: { tooltip: true },
    },
    {
      title: '用户昵称',
      key: 'nickName',
      width: 120,
      ellipsis: { tooltip: true },
    },
    {
      title: '已授予权限',
      key: '_grantedPermissions',
      width: 220,
      render(row: Record<string, any>) {
        const roles = (row.grantedRoles || []) as { bizId: string; roleName: string }[]
        const perms = (row.grantedPermissions || []) as string[]
        if (roles.length === 0) {
          return h('span', { style: 'color: #94a3b8; font-size: 13px' }, '暂无')
        }
        const visibleRoles = roles.slice(0, MAX_VISIBLE_TAGS)
        const overflow = roles.length - MAX_VISIBLE_TAGS
        const tags = visibleRoles.map(r =>
          h(NTag, { size: 'small', type: 'info', bordered: false }, { default: () => r.roleName })
        )
        if (overflow > 0) {
          tags.push(h(NTag, { size: 'small', bordered: false }, { default: () => `+${overflow}` }))
        }
        const tagRow = h('div', { style: 'display: flex; flex-wrap: wrap; gap: 4px; align-items: center' }, tags)

        const grouped = getGroupedPermissions(perms)
        const popoverContent = perms.length === 0
          ? h('div', { style: 'color: #94a3b8; padding: 8px' }, '暂无权限')
          : h('div', { style: 'max-width: 340px; max-height: 380px; overflow-y: auto; padding: 4px 0' }, [
              h('div', { style: 'font-size: 12px; color: #64748b; margin-bottom: 8px' },
                `已授予权限（共 ${perms.length} 项）`),
              ...grouped.map(g => h('div', { style: 'margin-bottom: 10px' }, [
                h('div', { style: 'font-weight: 600; font-size: 13px; color: #334155; margin-bottom: 4px' }, g.groupTitle),
                h('div', { style: 'display: flex; flex-wrap: wrap; gap: 4px 12px; padding-left: 8px' },
                  g.items.map(title => h('span', { style: 'font-size: 12px; color: #475569' }, `· ${title}`))
                ),
              ]))
            ])

        return h(NPopover, { trigger: 'hover', placement: 'bottom' }, {
          trigger: () => tagRow,
          default: () => popoverContent,
        })
      },
    },
    {
      title: '最后登录时间',
      key: 'lastLoginAt',
      width: 150,
      render(row: Record<string, any>) {
        const val = row.lastLoginAt
        if (!val) return h('span', { style: 'color: #cbd5e1' }, '-')
        const str = String(val)
        const spaceIdx = str.indexOf(' ')
        if (spaceIdx > 0) {
          return h('div', { style: 'display: flex; flex-direction: column; gap: 2px; line-height: 1.4' }, [
            h('span', { style: 'font-size: 13px; color: #374151' }, str.slice(0, spaceIdx)),
            h('span', { style: 'font-size: 13px; color: #374151' }, str.slice(spaceIdx + 1)),
          ])
        }
        return h('span', { style: 'font-size: 13px; color: #374151' }, str)
      },
    },
    {
      title: '创建时间',
      key: 'createdAt',
      width: 150,
      render(row: Record<string, any>) {
        const val = row.createdAt
        if (!val) return h('span', { style: 'color: #cbd5e1' }, '-')
        const str = String(val)
        const spaceIdx = str.indexOf(' ')
        if (spaceIdx > 0) {
          return h('div', { style: 'display: flex; flex-direction: column; gap: 2px; line-height: 1.4' }, [
            h('span', { style: 'font-size: 13px; color: #374151' }, str.slice(0, spaceIdx)),
            h('span', { style: 'font-size: 13px; color: #374151' }, str.slice(spaceIdx + 1)),
          ])
        }
        return h('span', { style: 'font-size: 13px; color: #374151' }, str)
      },
    },
    {
      title: '操作',
      key: '_actions',
      width: 200,
      fixed: 'right',
      render(row: Record<string, any>) {
        const btns = []
        if (checkPermission('GRANT_ROLE')) {
          btns.push(h(NButton, {
            size: 'small', quaternary: true, type: 'info',
            disabled: editingRowKey.value != null,
            onClick: () => handleOpenGrantModal(row),
          }, { default: () => '授予角色' }))
        }
        if (checkPermission(props.permissions?.update)) {
          btns.push(h(NButton, {
            size: 'small', quaternary: true, type: 'primary',
            disabled: editingRowKey.value != null,
            onClick: () => handleEdit(row),
          }, { default: () => '编辑' }))
        }
        if (checkPermission(props.permissions?.delete)) {
          btns.push(h(NPopconfirm, {
            onPositiveClick: () => handleDelete(row),
          }, {
            trigger: () => h(NButton, {
              size: 'small', quaternary: true, type: 'error',
              disabled: editingRowKey.value != null,
            }, { default: () => '删除' }),
            default: () => '确定要删除该记录吗？',
          }))
        }
        return h(NSpace, { wrap: false, size: 4 }, { default: () => btns })
      },
    },
  ]
  return cols
})

function handleTablePaginationUpdate(paginationUpdate: PaginationProps) {
  handlePaginationUpdate(paginationUpdate)
}

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
    fetchData()
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
          <NButton type="primary" v-permission="permissions?.create" @click="handleCreate">创建</NButton>
          <NPopconfirm
            :disabled="checkedRowKeys.length === 0"
            @positive-click="handleBatchDelete"
          >
            <template #trigger>
              <NButton
                type="error"
                :disabled="checkedRowKeys.length === 0"
                v-permission="permissions?.delete"
              >
                批量删除{{ checkedRowKeys.length > 0 ? `（${checkedRowKeys.length}）` : '' }}
              </NButton>
            </template>
            确定要删除选中的 {{ checkedRowKeys.length }} 条记录吗？
          </NPopconfirm>
        </NSpace>
      </div>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="tableLoading"
        :pagination="pagination"
        :checked-row-keys="checkedRowKeys"
        :row-key="(row: Record<string, any>) => row[bizKey]"
        :scroll-x="1100"
        @update:page="(page: number) => handleTablePaginationUpdate({ ...pagination, page })"
        @update:page-size="(size: number) => handleTablePaginationUpdate({ ...pagination, pageSize: size, page: 1 })"
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

Key changes from the original:
- Replaced `<DataTable>` with `<NDataTable>` for full column control
- Added `allPermissionGroups` loaded on mount for popover display
- Added computed `columns` with the custom "已授予权限" column using `NTag` + `NPopover`
- After grant success, calls `fetchData()` to refresh the table (so tags update immediately)
- Removed unused `NCheckbox` import (kept `NCheckboxGroup` and `NCheckbox` for grant modal)

- [ ] **Step 2: Commit**

```bash
git add app-generator/src/main/resources/frontend-skeleton/src/pages/UserPage.vue
git commit -m "feat: add granted permissions column with role tags and permission popover to user table"
```

---

### Task 5: Sync output directory

**Files:**
- Modify: `output/super-dsl-example/super-dsl-example-frontend/src/pages/UserPage.vue`
- Modify: `output/super-dsl-example/super-dsl-example-frontend/src/builtin-form.yml`

- [ ] **Step 1: Copy updated skeleton files to output**

```bash
cp app-generator/src/main/resources/frontend-skeleton/src/pages/UserPage.vue output/super-dsl-example/super-dsl-example-frontend/src/pages/UserPage.vue
cp app-generator/src/main/resources/frontend-skeleton/src/builtin-form.yml output/super-dsl-example/super-dsl-example-frontend/src/builtin-form.yml
```

- [ ] **Step 2: Copy updated backend skeleton files to output**

```bash
cp app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/dto/resp/ListUsersResp.java output/super-dsl-example/super-dsl-example-backend/src/main/java/com/allison1875/appgenerator/dto/resp/ListUsersResp.java
cp app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/service/impl/UserServiceImpl.java output/super-dsl-example/super-dsl-example-backend/src/main/java/com/allison1875/appgenerator/service/impl/UserServiceImpl.java
cp app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mapper/UserRoleMapper.java output/super-dsl-example/super-dsl-example-backend/src/main/java/com/allison1875/appgenerator/mapper/UserRoleMapper.java
cp app-generator/src/main/resources/backend-skeleton/src/main/resources/mapper/UserRoleMapper.xml output/super-dsl-example/super-dsl-example-backend/src/main/resources/mapper/UserRoleMapper.xml
cp app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mapper/RolePermissionMapper.java output/super-dsl-example/super-dsl-example-backend/src/main/java/com/allison1875/appgenerator/mapper/RolePermissionMapper.java
cp app-generator/src/main/resources/backend-skeleton/src/main/resources/mapper/RolePermissionMapper.xml output/super-dsl-example/super-dsl-example-backend/src/main/resources/mapper/RolePermissionMapper.xml
```

Note: When copying to the output directory, replace `__NAMESPACE__` with `com.allison1875.appgenerator` and `__NAMESPACE_PATH__` with `com/allison1875/appgenerator` in the file content. The skeleton files use placeholders; the output files use resolved package names.

- [ ] **Step 3: Commit**

```bash
git add output/
git commit -m "chore: sync skeleton changes to output example"
```

---

### Task 6: Verify frontend builds

- [ ] **Step 1: Run frontend type check**

```bash
cd output/super-dsl-example/super-dsl-example-frontend && npm run type-check
```

Expected: no type errors.

- [ ] **Step 2: Run frontend dev server and verify**

```bash
cd output/super-dsl-example/super-dsl-example-frontend && npm run dev
```

Verify in browser:
1. Sidebar shows "角色权限" (not "角色")
2. User table shows "已授予权限" column with role Tags
3. Hover on Tags shows Popover with grouped permissions
4. Users with no roles show "暂无"
5. Grant roles modal still works; after granting, Tags update immediately
