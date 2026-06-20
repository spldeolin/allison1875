<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NLayout, NLayoutSider, NMenu, NIcon, NPopconfirm, NModal, NForm, NFormItem, NInput, NButton,
  useMessage, type MenuOption, type FormInst
} from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import * as icons from '@vicons/ionicons5'
import appDef from '@/app.json'
import type { AppDef, MenuDef } from '@/schema/types'

const app = appDef as AppDef

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const message = useMessage()

interface RouteMeta {
  title?: string
  group?: string
  icon?: string
  order?: number
}

const menuOptions = computed<MenuOption[]>(() => {
  const dslRoutes = router.getRoutes().filter(r => r.meta?.group)

  const groups = new Map<string, { routes: typeof dslRoutes; order: number }>()
  for (const r of dslRoutes) {
    const meta = r.meta as RouteMeta
    const group = meta.group!
    const menuDef = app.menus.find(m => m.form.name === r.name)
    const listPermission = menuDef?.permissions?.list
    if (listPermission && !authStore.hasPermission(listPermission)) continue
    if (!groups.has(group)) {
      groups.set(group, { routes: [], order: meta.order ?? 99 })
    }
    groups.get(group)!.routes.push(r)
  }

  const sortedGroups = [...groups.entries()].sort((a, b) => a[1].order - b[1].order)

  const totalItems = sortedGroups.reduce((sum, [, { routes }]) => sum + routes.length, 0)
  const useStaticGroup = totalItems <= 8

  return sortedGroups.map(([groupName, { routes }]) => {
    const sortedRoutes = routes.sort((a, b) => ((a.meta as RouteMeta).order ?? 99) - ((b.meta as RouteMeta).order ?? 99))
    return {
      label: groupName,
      key: groupName,
      type: useStaticGroup ? 'group' as const : undefined,
      children: sortedRoutes.map(r => {
        const meta = r.meta as RouteMeta
        const iconComp = meta.icon ? (icons as any)[meta.icon] : undefined
        return {
          label: meta.title,
          key: r.path,
          icon: iconComp ? () => h(NIcon, null, { default: () => h(iconComp) }) : undefined
        }
      })
    }
  })
})

const activeKey = computed(() => route.path)

function handleMenuUpdate(key: string) {
  router.push(key)
}

// Logout
const loggingOut = ref(false)
async function handleLogout() {
  loggingOut.value = true
  await authStore.logoutApi()
  loggingOut.value = false
  router.push('/login')
}

// Change password
const showPasswordModal = ref(false)
const passwordFormRef = ref<FormInst | null>(null)
const passwordForm = ref({ password: '', confirmPassword: '' })
const changingPassword = ref(false)

const passwordRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (value !== passwordForm.value.password) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

function openPasswordModal() {
  passwordForm.value = { password: '', confirmPassword: '' }
  showPasswordModal.value = true
}

async function handleChangePassword() {
  try {
    await passwordFormRef.value?.validate()
  } catch {
    return
  }
  changingPassword.value = true
  try {
    await authStore.updateSelfPassword(passwordForm.value.password)
    message.success('密码修改成功')
    showPasswordModal.value = false
  } catch (e: any) {
    message.error(e.message || '修改失败')
  } finally {
    changingPassword.value = false
  }
}
</script>

<template>
  <NLayout has-sider class="dashboard-layout">
    <NLayoutSider
      bordered
      :width="240"
      :native-scrollbar="false"
      class="dashboard-sidebar"
    >
      <div class="sidebar-logo">
        <div class="logo-icon">
          <NIcon size="20" color="#18a058">
            <component :is="icons.GridOutline" />
          </NIcon>
        </div>
        <span class="logo-text">{{ app.title }}</span>
      </div>

      <NMenu
        :options="menuOptions"
        :value="activeKey"
        :indent="20"
        @update:value="handleMenuUpdate"
      />

      <div class="sidebar-footer">
        <div class="user-info">
          <div class="user-meta">
            <span class="user-name">{{ authStore.userInfo?.nickName || '用户' }}</span>
            <span class="user-sub">{{ authStore.userInfo?.username || '' }}</span>
          </div>
          <div class="user-actions">
            <div class="change-pwd-btn" @click="openPasswordModal">修改密码</div>
            <NPopconfirm
              :positive-button-props="{ type: 'error', size: 'small' }"
              :negative-button-props="{ size: 'small' }"
              positive-text="确认退出"
              negative-text="取消"
              @positive-click="handleLogout"
            >
              <template #trigger>
                <div class="logout-btn" title="退出登录">
                  <NIcon size="18" color="#94a3b8">
                    <component :is="icons.LogOutOutline" />
                  </NIcon>
                </div>
              </template>
              确定要退出登录吗？
            </NPopconfirm>
          </div>
        </div>
      </div>
    </NLayoutSider>

    <NLayout
      content-style="padding: 20px; background: #f5f7fa; height: 100vh; overflow: hidden; display: flex; flex-direction: column"
      class="main-content-layout"
    >
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </NLayout>
  </NLayout>

  <NModal
    v-model:show="showPasswordModal"
    preset="card"
    title="修改密码"
    :style="{ width: '400px' }"
    :mask-closable="false"
  >
    <NForm ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-placement="top">
      <NFormItem label="新密码" path="password">
        <NInput
          v-model:value="passwordForm.password"
          type="password"
          placeholder="请输入新密码"
          show-password-on="click"
        />
      </NFormItem>
      <NFormItem label="确认密码" path="confirmPassword">
        <NInput
          v-model:value="passwordForm.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password-on="click"
          @keyup.enter="handleChangePassword"
        />
      </NFormItem>
    </NForm>
    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 8px">
        <NButton @click="showPasswordModal = false">取消</NButton>
        <NButton type="primary" :loading="changingPassword" @click="handleChangePassword">确认修改</NButton>
      </div>
    </template>
  </NModal>
</template>

<style scoped>
.dashboard-layout {
  height: 100vh;
  overflow: hidden;
}

.dashboard-sidebar {
  background: #ffffff !important;
  border-right: 1px solid #e5e7eb !important;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 16px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 8px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ecfdf5;
  border-radius: 10px;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.3px;
}

.sidebar-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  border-top: 1px solid #f1f5f9;
  background: #ffffff;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 10px;
  transition: background-color 0.15s ease;
}

.user-info:hover {
  background: #f8fafc;
}

.user-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-sub {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.change-pwd-btn {
  font-size: 12px;
  color: #18a058;
  cursor: pointer;
  white-space: nowrap;
  padding: 0 8px;
  height: 32px;
  line-height: 32px;
  border-radius: 6px;
  opacity: 0;
  transition: opacity 0.15s ease, background-color 0.15s ease;
}

.user-info:hover .change-pwd-btn {
  opacity: 1;
}

.change-pwd-btn:hover {
  background: #f0fdf4;
}

.logout-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.15s ease;
  flex-shrink: 0;
}

.logout-btn:hover {
  background: #fef2f2;
}

.logout-btn:hover :deep(.n-icon) {
  color: #ef4444 !important;
}

.main-content-layout :deep(.n-layout-scroll-container) {
  overflow-x: auto !important;
}
</style>
