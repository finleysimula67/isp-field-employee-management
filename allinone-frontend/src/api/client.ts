import axios from 'axios'
import { appNavigate } from '../lib/navigate'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'https://allinone-backend-xoh0.onrender.com/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
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
