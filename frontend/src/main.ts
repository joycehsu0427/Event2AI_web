import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import VueKonva from 'vue-konva';

import App from './App.vue'
import router from './router'

const app = createApp(App)

console.log('main.ts is running');

app.use(createPinia())
app.use(router)
app.use(VueKonva); // Add VueKonva

app.mount('#app')
