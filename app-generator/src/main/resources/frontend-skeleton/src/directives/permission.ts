import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

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
