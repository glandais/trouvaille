import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import basicSsl from '@vitejs/plugin-basic-ssl'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    basicSsl({
      name: 'localhost',
      domains: ['localhost'],
    }),
  ],
  build: {
    rollupOptions: {
      output: {
        manualChunks: (id) => {
          if (id.includes('node_modules/axios/')) return 'axios'
          if (id.includes('node_modules/prosemirror')) return 'prosemirror'
          if (id.includes('node_modules/@toast-ui')) return 'tui'
          if (id.includes('node_modules/@vue')) return 'vue'
          if (id.includes('node_modules/vue')) return 'vue2'
          if (id.includes('trouvaille-front/src')) return 'trouvaille'
        }
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5174,
    proxy: {
      '/api': 'http://localhost:8080',
    }
  }
})
