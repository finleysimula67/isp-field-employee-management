import axios from 'axios'
import { appNavigate } from '../lib/navigate'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'https://allinone-backend-xoh0.onrender.com/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 120000,
  // ✅ Never follow redirects via axios — if the backend sends a 302,
  // let it fail loudly instead of silently chasing it cross-origin
  maxRedirects: 0,
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token && !config.url?.startsWith('/auth/')) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (res) => res,
  (err) => {
    // ✅ Ignore redirect errors (3xx) — these are expected on cold starts
    // and should not trigger logout or navigation
    if (err.response?.status >= 300 && err.response?.status < 400) {
      return Promise.reject(err)
    }

    if (err.response?.status === 401 && !err.config?.url?.startsWith('/auth/')) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // ✅ Guard: don't redirect if already on /login to prevent redirect loop
      if (window.location.pathname !== '/login') {
        appNavigate('/login')
      }
    }
    return Promise.reject(err)
  }
)

export default client
