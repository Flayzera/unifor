import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import { initAccessibility } from './composables/useAccessibility'

initAccessibility()
createApp(App).use(router).mount('#app')
