import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import appDef from '@/app.json'
import type { AppDef } from '@/schema/types'
import { upperCamelToKebab } from '@/utils/naming'
import { useAuthStore } from '@/stores/auth'

const CrudPage = () => import('@/core/CrudPage.vue')
const DashboardLayout = () => import('@/layouts/DashboardLayout.vue')
const Login = () => import('@/views/Login.vue')

// Auto-discover page overrides: src/pages/{FormName}Page.vue
const pageOverrides = import.meta.glob('../pages/*Page.vue') as Record<string, () => Promise<any>>

function resolvePageComponent(formName: string) {
  const key = `../pages/${formName}Page.vue`
  return pageOverrides[key] || CrudPage
}

const app = appDef as AppDef

const dslRoutes: RouteRecordRaw[] = app.menus.map(menu => ({
  path: `/${upperCamelToKebab(menu.form.name)}`,
  name: menu.form.name,
  component: resolvePageComponent(menu.form.name),
  props: { schema: menu.form, permissions: menu.permissions },
  meta: { title: menu.form.title, group: menu.group, icon: menu.icon, order: menu.order }
}))

const firstFormPath = dslRoutes.length > 0 ? dslRoutes[0].path : '/login'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: DashboardLayout,
    meta: { requiresAuth: true },
    children: [
      ...dslRoutes,
      { path: '', redirect: firstFormPath }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  const isLoginPage = to.path === '/login'

  if (!authStore.isAuthenticated) {
    if (!isLoginPage) {
      authStore.saveRedirect(to.fullPath)
      return { path: '/login' }
    }
    return
  }

  const ok = await authStore.fetchCurrentUser()
  if (!ok) {
    authStore.logout()
    if (!isLoginPage) {
      authStore.saveRedirect(to.fullPath)
      return { path: '/login' }
    }
    return
  }

  authStore.fetchPermissionTitles()

  if (isLoginPage) {
    const saved = authStore.popRedirect()
    return saved || firstFormPath
  }

  // Permission-based route guard
  const menuDef = app.menus.find(m => m.form.name === to.name)
  const listPermission = menuDef?.permissions?.list
  if (listPermission && !authStore.hasPermission(listPermission)) {
    const firstAccessible = dslRoutes.find(r => {
      const md = app.menus.find(m => m.form.name === r.name)
      const perm = md?.permissions?.list
      return !perm || authStore.hasPermission(perm)
    })
    return firstAccessible ? { path: firstAccessible.path } : { path: '/login' }
  }
})

export { firstFormPath }
export default router
