import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// Bootstrap
import 'bootstrap/dist/css/bootstrap.min.css'

import '@/assets/css/main.css'    // assets 方案
import '@/assets/css/components.css' // 使用 @ 指向 src 目录

createApp(App).use(router).mount('#app')