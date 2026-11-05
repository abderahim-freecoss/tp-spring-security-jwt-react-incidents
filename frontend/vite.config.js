import { defineConfig } from 'vite';
export default defineConfig({ esbuild: { jsx: 'automatic' }, server: { port: 5173, strictPort: true, proxy: { '/auth': 'http://127.0.0.1:8080', '/api': 'http://127.0.0.1:8080' } } });
