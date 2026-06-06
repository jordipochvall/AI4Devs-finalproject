import axios from 'axios'
import { useAuthStore } from '../auth/authStore'
import i18n from '../i18n/i18n'

/** Axios instance for the API, pre-configured with auth and language interceptors. */
const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

// Attach the JWT to every request when there is an active session.
api.interceptors.request.use(config => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  // Propagate the active i18n language (reflects hot switching).
  config.headers['Accept-Language'] = i18n.language?.slice(0, 2) ?? 'es'
  return config
})

// On 401 with an active token the session expired: clear it and redirect to /login.
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
  },
)

export default api
