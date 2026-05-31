<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NIcon, useMessage, type FormInst } from 'naive-ui'
import { GridOutline } from '@vicons/ionicons5'
import { useAuthStore, type UserInfo } from '@/stores/auth'
import request from '@/utils/request'
import type { RequestResult } from '@/utils/request'
import appDef from '@/app.json'

interface LoginResp {
  token: string
  nickName: string
  username: string
  permissions: string[]
}

const router = useRouter()
const route = useRoute()
const message = useMessage()
const authStore = useAuthStore()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formData = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const { data } = await request.post<RequestResult<LoginResp>>('/api/v1/authc/login', formData.value)
    const { token, nickName, username, permissions } = data.data
    const userInfo: UserInfo = {
      id: username,
      username,
      displayName: nickName
    }
    authStore.setAuth(token, userInfo, permissions)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    message.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card-wrapper">
      <NCard class="login-card" :bordered="false">
        <div class="login-header">
          <div class="login-logo">
            <NIcon size="24" color="#18a058">
              <GridOutline />
            </NIcon>
          </div>
          <h1 class="login-title">{{ appDef.title }}</h1>
        </div>

        <NForm ref="formRef" :model="formData" :rules="rules" size="large">
          <NFormItem path="username" :show-label="false">
            <NInput
              v-model:value="formData.username"
              placeholder="用户名"
              @keyup.enter="handleLogin"
            />
          </NFormItem>
          <NFormItem path="password" :show-label="false">
            <NInput
              v-model:value="formData.password"
              type="password"
              placeholder="密码"
              show-password-on="click"
              @keyup.enter="handleLogin"
            />
          </NFormItem>
          <NButton
            type="primary"
            block
            strong
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            登录
          </NButton>
        </NForm>
      </NCard>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #ecfdf5 0%, #f0fdf4 50%, #f0f9ff 100%);
  padding: 24px;
}

.login-card-wrapper {
  width: 100%;
  max-width: 400px;
}

.login-card {
  border-radius: 20px !important;
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.05),
    0 20px 40px -4px rgba(24, 160, 88, 0.08);
  padding: 16px 8px !important;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ecfdf5;
  border-radius: 14px;
  margin: 0 auto 16px;
}

.login-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  letter-spacing: -0.5px;
}

.login-btn {
  margin-top: 8px;
  height: 42px !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  border-radius: 10px !important;
}
</style>
