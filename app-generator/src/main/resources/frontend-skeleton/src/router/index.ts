import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import formDefs from 'virtual:form-dsl'
import { upperCamelToKebab } from '@/utils/naming'
import { useAuthStore } from '@/stores/auth'

const CrudPage = () => import('@/core/CrudPage.vue')
const DashboardLayout = () => import('@/layouts/DashboardLayout.vue')
const Login = () => import('@/views/Login.vue')

const dslRoutes: RouteRecordRaw[] = formDefs.map(def => ({
  path: `/${upperCamelToKebab(def.name)}`,
  name: def.name,
  component: CrudPage,
  props: { schema: def },
  meta: { title: def.title, group: def.group, icon: def.icon, order: def.order }
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
