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
  build: {
    target: 'es2020',
    cssCodeSplit: true,
    rollupOptions: {
      output: {
        manualChunks: {
          'naive-ui': ['naive-ui'],
          'vue-vendor': ['vue', 'vue-router', 'pinia']
        }
      }
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
