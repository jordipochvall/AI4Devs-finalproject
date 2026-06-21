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

// Single-flight refresh: concurrent 401s share one /auth/refresh call (HU-13 AC3).
let refreshPromise: Promise<string> | null = null

/** Renews the session once, updating the store, and returns the new access token. */
async function refreshSession(): Promise<string> {
  const { authApi } = await import('../auth/authApi')
  try {
    const refreshToken = useAuthStore.getState().refreshToken
    if (!refreshToken) {
      throw new Error('no refresh token')
    }
    const resp = await authApi.refresh(refreshToken)
    useAuthStore.getState().setTokens(resp.token, resp.refreshToken)
    return resp.token
  } finally {
    refreshPromise = null
  }
}

/** Clears the session and bounces the user to the login screen. */
function forceLogin(): void {
  useAuthStore.getState().clearAuth()
  window.location.href = '/login'
}

// On 401 with an active token, try a silent refresh + retry; otherwise clear and go to /login.
api.interceptors.response.use(
  response => response,
  async error => {
    const original = error.config
    const status = error.response?.status
    const state = useAuthStore.getState()

    // Renew transparently only for an authenticated request that has not been retried and is not
    // itself an auth call (login/register live under /auth and must not trigger a refresh).
    const isRetryable =
      status === 401 &&
      !!state.token &&
      !!state.refreshToken &&
      original &&
      !original._retry &&
      !String(original.url ?? '').includes('/auth/')

    if (isRetryable) {
      original._retry = true
      try {
        const newToken = await (refreshPromise ??= refreshSession())
        original.headers = original.headers ?? {}
        original.headers.Authorization = `Bearer ${newToken}`
        return api(original) // AC1/AC4: replay the original request transparently
      } catch {
        forceLogin() // AC2: refresh failed → back to login
        return Promise.reject(error)
      }
    }

    // Unrecoverable 401 with an active session (no refresh token or already retried).
    if (status === 401 && state.token) {
      forceLogin()
    }
    return Promise.reject(error)
  },
)

export default api
