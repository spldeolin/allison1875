import { useAuthStore } from '@/stores/auth'

export function checkPermission(permission: string | undefined | null): boolean {
  if (!permission) return true
  const authStore = useAuthStore()
  return authStore.hasPermission(permission)
}
