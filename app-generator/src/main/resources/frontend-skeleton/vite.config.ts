import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import mockApiPlugin from './plugins/vite-plugin-mock-api'

export default defineConfig({
  plugins: [
    vue(),
    mockApiPlugin()
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5180,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
