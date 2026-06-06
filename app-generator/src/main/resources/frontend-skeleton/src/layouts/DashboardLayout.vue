<script setup lang="ts">
import { computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NLayout, NLayoutSider, NMenu, NIcon, type MenuOption } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import * as icons from '@vicons/ionicons5'
import appDef from '@/app.json'
import type { AppDef } from '@/schema/types'

const app = appDef as AppDef

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

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
    if (!authStore.hasPermission(r.name as string)) continue
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

function handleLogout() {
  authStore.logout()
  router.push('/login')
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
        <div class="user-info" @click="handleLogout">
          <div class="user-meta">
            <span class="user-name">{{ authStore.userInfo?.displayName || '用户' }}</span>
            <span class="user-action">退出登录</span>
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
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.user-info:hover {
  background: #f8fafc;
}

.user-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-action {
  font-size: 12px;
  color: #94a3b8;
  transition: color 0.15s ease;
}

.user-info:hover .user-action {
  color: #18a058;
}

.main-content-layout :deep(.n-layout-scroll-container) {
  overflow-x: auto !important;
}
</style>
