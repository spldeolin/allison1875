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
  props: { schema: menu.form },
  meta: { title: menu.form.title, group: menu.group, icon: menu.icon, order: menu.order }
}))

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
      { path: '', redirect: dslRoutes.length > 0 ? dslRoutes[0].path : '/login' }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const requiresAuth = to.matched.some(r => r.meta.requiresAuth !== false)
  if (requiresAuth) {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }
})

export default router
