import axios from 'axios';
// Same-origin Vite proxy in development; configure VITE_API_URL for a separate API.
const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '', timeout: 15000 });
api.interceptors.request.use(config => {
  if (config.url.startsWith('/api/')) {
    try {
      const session = JSON.parse(sessionStorage.getItem('incident-session'));
      if (session?.token) config.headers.Authorization = `Bearer ${session.token}`;
    } catch { sessionStorage.removeItem('incident-session'); }
  }
  return config;
});
api.interceptors.response.use(response => response, error => {
  if (error.response?.status === 401 && error.config?.url.startsWith('/api/')) {
    sessionStorage.removeItem('incident-session');
    window.location.replace('/login?expired=1');
  }
  return Promise.reject(error);
});
export const errorMessage = error => error.response?.data?.message || 'Le serveur ne répond pas. Vérifiez que le backend est démarré, puis réessayez.';
export default api;
