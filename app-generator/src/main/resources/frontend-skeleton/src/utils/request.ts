import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

export interface ApiBaseResult<T = unknown> {
  code: number
  msg: string
  result: T
}

export interface PageResult<T> {
  count: number
  list: T[]
}

const request = axios.create({
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

request.interceptors.request.use(config => {
  const authStore = useAuthStore()
  const token = authStore.getToken()
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
})

request.interceptors.response.use(
  response => {
    const data = response.data as ApiBaseResult
    if (data.code === 200) {
      return response
    }
    if (data.code === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.hash = '#/login'
      return Promise.reject(new Error('认证已过期'))
    }
    return Promise.reject(new Error(data.msg || '请求失败'))
  },
  error => Promise.reject(error)
)

export default request
