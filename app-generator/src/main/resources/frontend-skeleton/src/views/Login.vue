<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NIcon, useMessage, type FormInst } from 'naive-ui'
import { GridOutline } from '@vicons/ionicons5'
import { useAuthStore, type UserInfo } from '@/stores/auth'
import request from '@/utils/request'
import type { RequestResult } from '@/utils/request'
import { firstFormPath } from '@/router'
import appDef from '@/app.json'

interface LoginResp {
  token: string
  currentUser: {
    nickName: string
    username: string
    permissions: string[]
  }
}

const router = useRouter()
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
    const { token, currentUser } = data.data
    const { username, nickName, permissions } = currentUser
    const userInfo: UserInfo = { username, nickName }
    authStore.setAuth(token, userInfo, permissions)
    const redirect = authStore.popRedirect() || firstFormPath
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
    <div class="login-bg-shapes">
      <i class="s" style="--d:80s; --x:30px; --y:-40px; --r:45deg; width:160px; height:160px; top:8%; left:5%; border-radius:50%;"></i>
      <i class="s" style="--d:100s; --x:-25px; --y:35px; --r:-30deg; width:200px; height:90px; top:18%; left:55%; border-radius:10px;"></i>
      <i class="s" style="--d:90s; --x:20px; --y:50px; --r:60deg; width:100px; height:100px; top:60%; left:10%; border-radius:50%;"></i>
      <i class="s" style="--d:110s; --x:-35px; --y:-30px; --r:-45deg; width:130px; height:130px; top:48%; left:65%; border-radius:14px;"></i>
      <i class="s" style="--d:95s; --x:40px; --y:25px; --r:30deg; width:80px; height:80px; top:78%; left:38%; border-radius:50%;"></i>
    </div>

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
  position: relative;
  overflow: hidden;
}

.login-bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.s {
  position: absolute;
  border: 1.5px solid rgba(24, 160, 88, 0.13);
  background: rgba(24, 160, 88, 0.04);
  animation: float var(--d) ease-in-out infinite alternate;
}

@keyframes float {
  from { transform: translate(0, 0) rotate(0deg); }
  to { transform: translate(var(--x), var(--y)) rotate(var(--r)); }
}

.login-card-wrapper {
  width: 100%;
  max-width: 400px;
  position: relative;
  z-index: 1;
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

@media (prefers-reduced-motion: reduce) {
  .s { animation: none; }
}
</style>
