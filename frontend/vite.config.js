import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    define: {
        global: 'globalThis',
    },
    test: {
        environment: 'jsdom',
        include: ['src/tests/**/*.{test,spec}.{js,jsx,ts,tsx}'],
        exclude: ['node_modules', 'tests/**/*.spec.js']
    },

})