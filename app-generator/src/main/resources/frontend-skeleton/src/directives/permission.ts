import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

export const permissionDirective: Directive<HTMLElement, string | undefined | null> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | undefined | null>) {
    applyPermission(el, binding.value)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<string | undefined | null>) {
    applyPermission(el, binding.value)
  },
  beforeUnmount(el: HTMLElement) {
    removeTooltip(el)
  }
}

function applyPermission(el: HTMLElement, permission: string | undefined | null) {
  if (!permission) {
    restoreElement(el)
    return
  }
  const authStore = useAuthStore()
  if (!authStore.hasPermission(permission)) {
    disableElement(el, permission, authStore.getPermissionTitle(permission))
  } else {
    restoreElement(el)
  }
}

function disableElement(el: HTMLElement, permission: string, title: string) {
  el.classList.add('permission-disabled')
  el.setAttribute('data-permission-disabled', 'true')
  el.setAttribute('data-missing-permission', permission)
  el.setAttribute('data-missing-permission-title', title)

  if (!el._permissionClickBlocker) {
    el._permissionClickBlocker = (e: Event) => {
      e.stopImmediatePropagation()
      e.preventDefault()
    }
  }
  el.addEventListener('click', el._permissionClickBlocker, true)

  if (!el._permissionMouseEnter) {
    el._permissionMouseEnter = () => showTooltip(el, `未被授予“${el.getAttribute('data-missing-permission-title')}”权限`)
    el._permissionMouseLeave = () => removeTooltip(el)
  }
  el.addEventListener('mouseenter', el._permissionMouseEnter)
  el.addEventListener('mouseleave', el._permissionMouseLeave)
}

function restoreElement(el: HTMLElement) {
  el.classList.remove('permission-disabled')
  el.removeAttribute('data-permission-disabled')
  el.removeAttribute('data-missing-permission')

  if (el._permissionClickBlocker) {
    el.removeEventListener('click', el._permissionClickBlocker, true)
  }
  if (el._permissionMouseEnter) {
    el.removeEventListener('mouseenter', el._permissionMouseEnter)
    el.removeEventListener('mouseleave', el._permissionMouseLeave)
  }
  removeTooltip(el)
}

function showTooltip(el: HTMLElement, text: string) {
  removeTooltip(el)
  const tooltip = document.createElement('div')
  tooltip.className = 'permission-tooltip'
  tooltip.textContent = text
  document.body.appendChild(tooltip)

  const rect = el.getBoundingClientRect()
  tooltip.style.left = `${rect.left + rect.width / 2}px`
  tooltip.style.top = `${rect.top - 8}px`
  el._permissionTooltip = tooltip
}

function removeTooltip(el: HTMLElement) {
  if (el._permissionTooltip) {
    el._permissionTooltip.remove()
    el._permissionTooltip = null
  }
}

declare global {
  interface HTMLElement {
    _permissionClickBlocker?: (e: Event) => void
    _permissionMouseEnter?: () => void
    _permissionMouseLeave?: () => void
    _permissionTooltip?: HTMLDivElement | null
  }
}
