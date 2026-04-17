import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap'
import App from './App.vue'
import router from './router'
import './assets/styles/theme.css'
import VChart from 'vue-echarts'

const app = createApp(App)
app.use(createPinia())

app.use(router)
app.component('v-chart', VChart)

app.mount('#app')