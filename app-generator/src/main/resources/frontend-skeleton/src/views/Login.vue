<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
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

// ==================== Canvas 沙箱长方体背景动画 ====================

const bgCanvas = ref<HTMLCanvasElement | null>(null)
let animFrameId = 0

interface SandboxRow {
  y: number
  boxW: number
  boxH: number
  gap: number
  speed: number
  offset: number
  color: string
  borderColor: string
}

function initCanvas() {
  const canvas = bgCanvas.value as HTMLCanvasElement
  if (!canvas) return
  const ctx = canvas.getContext('2d') as CanvasRenderingContext2D
  if (!ctx) return

  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resize()
  window.addEventListener('resize', resize)

  const rowColors = [
    { fill: 'rgba(24,160,88,0.04)',   border: 'rgba(24,160,88,0.09)'  },
    { fill: 'rgba(32,128,240,0.03)',  border: 'rgba(32,128,240,0.07)' },
    { fill: 'rgba(240,160,32,0.03)',  border: 'rgba(240,160,32,0.07)' },
    { fill: 'rgba(100,180,120,0.03)', border: 'rgba(100,180,120,0.08)' },
    { fill: 'rgba(80,120,220,0.03)',  border: 'rgba(80,120,220,0.06)' },
    { fill: 'rgba(200,80,80,0.02)',   border: 'rgba(200,80,80,0.06)'  },
    { fill: 'rgba(24,160,88,0.03)',   border: 'rgba(24,160,88,0.07)'  },
  ]

  const ROW_COUNT = 5
  const rows: SandboxRow[] = Array.from({ length: ROW_COUNT }, (_, i) => {
    const c = rowColors[i % rowColors.length]
    const boxW = 240 + Math.random() * 180
    const boxH = 84 + Math.random() * 42
    const gap  = 180 + Math.random() * 120
    const speed = (0.1 + Math.random() * 0.15) * (i % 2 === 0 ? 1 : -1)
    return {
      y: 0,
      boxW, boxH, gap,
      speed,
      offset: Math.random() * (boxW + gap) * -1,
      color: c.fill,
      borderColor: c.border,
    }
  })

  const distributeRows = () => {
    const h = canvas.height
    rows.forEach((row, i) => {
      row.y = (h / (ROW_COUNT + 1)) * (i + 1)
    })
  }
  distributeRows()
  window.addEventListener('resize', distributeRows)

  function drawBox(ctx: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, r: number, fill: string, stroke: string) {
    ctx.beginPath()
    ctx.moveTo(x + r, y)
    ctx.lineTo(x + w - r, y)
    ctx.quadraticCurveTo(x + w, y, x + w, y + r)
    ctx.lineTo(x + w, y + h - r)
    ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h)
    ctx.lineTo(x + r, y + h)
    ctx.quadraticCurveTo(x, y + h, x, y + h - r)
    ctx.lineTo(x, y + r)
    ctx.quadraticCurveTo(x, y, x + r, y)
    ctx.closePath()
    ctx.fillStyle = fill
    ctx.fill()
    ctx.strokeStyle = stroke
    ctx.lineWidth = 1
    ctx.stroke()
  }

  function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    rows.forEach(row => {
      row.offset += row.speed
      const unit = row.boxW + row.gap
      if (row.speed > 0 && row.offset > unit) row.offset -= unit
      if (row.speed < 0 && row.offset < -unit) row.offset += unit

      const startX = row.offset % unit - unit
      const count = Math.ceil(canvas.width / unit) + 2
      for (let i = 0; i < count; i++) {
        const x = startX + i * unit
        const y = row.y - row.boxH / 2
        drawBox(ctx, x, y, row.boxW, row.boxH, 6, row.color, row.borderColor)
      }
    })

    animFrameId = requestAnimationFrame(animate)
  }

  animate()
}

onMounted(() => {
  initCanvas()
})

onBeforeUnmount(() => {
  if (animFrameId) cancelAnimationFrame(animFrameId)
})

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
    <canvas ref="bgCanvas" class="login-bg-canvas"></canvas>

    <div class="login-bg">
      <div class="login-bg-circle circle-1"></div>
      <div class="login-bg-circle circle-2"></div>
      <div class="login-bg-circle circle-3"></div>
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
  background: linear-gradient(135deg, #f7fbf9 0%, #f2f9f3 50%, #faf4fb 100%);
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.login-bg-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.login-bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.04;
}

.circle-1 {
  width: 500px;
  height: 500px;
  background: #18a058;
  top: -150px;
  left: -150px;
}

.circle-2 {
  width: 400px;
  height: 400px;
  background: #2080f0;
  bottom: -100px;
  right: -100px;
}

.circle-3 {
  width: 300px;
  height: 300px;
  background: #f0a020;
  top: 50%;
  left: 60%;
  transform: translate(-50%, -50%);
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
</style>
