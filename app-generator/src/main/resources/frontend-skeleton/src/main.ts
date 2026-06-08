import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles/global.css'
import appDef from './app.json'

document.title = appDef.title || appDef.name

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
