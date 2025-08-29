import { defineConfig } from 'vite'

export default defineConfig({
  // Vite配置用于Kotlin/JS项目
  build: {
    outDir: 'build/dist',
    sourcemap: true,
    rollupOptions: {
      input: {
        main: 'build/js/packages/webApp/kotlin/webApp.js'
      }
    }
  },
  server: {
    port: 3000,
    open: true
  },
  css: {
    devSourcemap: true
  }
})
