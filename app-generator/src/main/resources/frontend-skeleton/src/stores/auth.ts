import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import type { RequestResult } from '@/utils/request'

const STORAGE_KEY_TOKEN = 'form_web_token'
const STORAGE_KEY_USER = 'form_web_user'
const STORAGE_KEY_PERMISSIONS = 'form_web_permissions'
const STORAGE_KEY_REDIRECT = 'form_web_redirect'

export interface UserInfo {
  username: string
  nickName: string
}

interface CurrentUserResp {
  nickName: string
  username: string
  permissions: string[]
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(STORAGE_KEY_TOKEN) || '')
  const userInfo = ref<UserInfo | null>(
    JSON.parse(localStorage.getItem(STORAGE_KEY_USER) || 'null')
  )
  const permissions = ref<string[]>(
    JSON.parse(localStorage.getItem(STORAGE_KEY_PERMISSIONS) || '[]')
  )

  const isAuthenticated = computed(() => !!token.value)

  function setAuth(t: string, user: UserInfo, perms: string[]) {
    token.value = t
    userInfo.value = user
    permissions.value = perms
    localStorage.setItem(STORAGE_KEY_TOKEN, t)
    localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(user))
    localStorage.setItem(STORAGE_KEY_PERMISSIONS, JSON.stringify(perms))
  }

  function getToken(): string {
    return token.value
  }

  function hasPermission(perm: string): boolean {
    if (!permissions.value || permissions.value.length === 0) return true
    return permissions.value.includes(perm)
  }

  async function fetchCurrentUser(): Promise<boolean> {
    try {
      const { data } = await request.post<RequestResult<CurrentUserResp>>('/api/v1/authc/getCurrentUser')
      const resp = data.data
      userInfo.value = { username: resp.username, nickName: resp.nickName }
      permissions.value = resp.permissions
      localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(userInfo.value))
      localStorage.setItem(STORAGE_KEY_PERMISSIONS, JSON.stringify(resp.permissions))
      return true
    } catch {
      return false
    }
  }

  async function logoutApi(): Promise<void> {
    try {
      await request.post('/api/v1/authc/logout')
    } catch {
      // ignore
    }
    clearLocal()
  }

  async function updateSelfPassword(password: string): Promise<void> {
    await request.post('/api/v1/authc/updateSelfPassword', { password })
  }

  function clearLocal() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem(STORAGE_KEY_TOKEN)
    localStorage.removeItem(STORAGE_KEY_USER)
    localStorage.removeItem(STORAGE_KEY_PERMISSIONS)
  }

  function logout() {
    clearLocal()
  }

  function saveRedirect(fullPath: string) {
    localStorage.setItem(STORAGE_KEY_REDIRECT, fullPath)
  }

  function popRedirect(): string | null {
    const path = localStorage.getItem(STORAGE_KEY_REDIRECT)
    localStorage.removeItem(STORAGE_KEY_REDIRECT)
    return path
  }

  return {
    token, userInfo, permissions, isAuthenticated,
    setAuth, getToken, hasPermission, logout,
    fetchCurrentUser, logoutApi, updateSelfPassword,
    saveRedirect, popRedirect
  }
})
