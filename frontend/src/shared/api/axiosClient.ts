import axios from 'axios'
import { useAuthStore } from '../auth/authStore'
import i18n from '../i18n/i18n'

const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

// Añade el JWT a todas las peticiones si hay sesión activa
api.interceptors.request.use(config => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  // AC5: propaga el idioma activo de i18n (refleja la conmutación en caliente)
  config.headers['Accept-Language'] = i18n.language?.slice(0, 2) ?? 'es'
  return config
})

// 401 con token activo → la sesión expiró; limpia y redirige a /login
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const hasToken = !!useAuthStore.getState().token
      if (hasToken) {
        useAuthStore.getState().clearAuth()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api
