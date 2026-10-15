/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    // Pages are split into their own chunks; the Employees page is ~500 kB (149 kB gzipped),
    // almost all of it the MUI data grid, which cannot be split further
    chunkSizeWarningLimit: 600,
  },
  server: {
    // Forward API calls to the Spring Boot backend during development
    proxy: {
      '/api': 'http://localhost:8081',
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/setupTests.ts',
  },
})
