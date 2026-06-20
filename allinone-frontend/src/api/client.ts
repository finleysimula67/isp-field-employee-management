import axios from 'axios'
import { appNavigate } from '../lib/navigate'

const rawUrl = import.meta.env.VITE_API_URL || 'https://allinone-backend-xoh0.onrender.com'
const apiBase = rawUrl.replace(/\/?(api\/?)?$/, '') + '/api'

const client = axios.create({
  baseURL: apiBase,
  headers: { 'Content-Type': 'application/json' },
  timeout: 120000,
  maxRedirects: 0, // ✅ never chase redirects cross-origin
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
    // ✅ ignore 3xx — don't treat redirects as auth failures
    if (err.response?.status >= 300 && err.response?.status < 400) {
      return Promise.reject(err)
    }
    if (err.response?.status === 401 && !err.config?.url?.startsWith('/auth/')) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (window.location.pathname !== '/login') {
        appNavigate('/login')
      }
    }
    return Promise.reject(err)
  }
)

export default client
