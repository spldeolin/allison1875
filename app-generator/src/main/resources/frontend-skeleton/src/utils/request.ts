import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

// Backend RequestResult<T> shape (from backend-skeleton/common/RequestResult.java)
export interface RequestResult<T = unknown> {
  errorCode: string | null
  data: T
  errorMsg: string | null
  traceId: string
}

// Keep this for list responses
export interface PageResult<T> {
  total: number
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
    const data = response.data as RequestResult
    if (data.errorCode === null) {
      return response
    }
    if (data.errorCode === '401') {
      const authStore = useAuthStore()
      authStore.logout()
      return Promise.reject(new Error('认证已过期'))
    }
    return Promise.reject(new Error(data.errorMsg || '请求失败'))
  },
  error => Promise.reject(error)
)

export default request
