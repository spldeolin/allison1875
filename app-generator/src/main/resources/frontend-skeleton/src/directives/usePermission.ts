import { useAuthStore } from '@/stores/auth'

export function checkPermission(permission: string | undefined | null): boolean {
  if (!permission) return true
  const authStore = useAuthStore()
  return authStore.hasPermission(permission)
}

export function getPermissionTitle(code: string | undefined | null): string {
  if (!code) return ''
  const authStore = useAuthStore()
  return authStore.getPermissionTitle(code)
}
