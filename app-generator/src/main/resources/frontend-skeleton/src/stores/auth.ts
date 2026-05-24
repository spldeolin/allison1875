import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY_TOKEN = 'form_web_token'
const STORAGE_KEY_USER = 'form_web_user'
const STORAGE_KEY_PERMISSIONS = 'form_web_permissions'

export interface UserInfo {
  id: string
  username: string
  displayName: string
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

  function getUserInfo(): UserInfo | null {
    return userInfo.value
  }

  function hasPermission(perm: string): boolean {
    return permissions.value.includes(perm)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem(STORAGE_KEY_TOKEN)
    localStorage.removeItem(STORAGE_KEY_USER)
    localStorage.removeItem(STORAGE_KEY_PERMISSIONS)
  }

  return { token, userInfo, permissions, isAuthenticated, setAuth, getToken, getUserInfo, hasPermission, logout }
})
