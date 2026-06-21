import axios from 'axios'
import api from '../api/axiosClient'
import type { AuthUser } from './authStore'

/** Authentication response returned by register/login/refresh. */
export interface AuthResponse {
  token: string
  tokenType: string
  expiresIn: number
  refreshToken: string
  user: AuthUser
}

/** Registration payload (birthDate as ISO "YYYY-MM-DD"). */
export interface RegisterPayload {
  email: string
  password: string
  birthDate: string
  locale?: string
}

/** Login payload. */
export interface LoginPayload {
  email: string
  password: string
}

/**
 * Auth API calls. {@link authApi.refresh} and {@link authApi.logout} bypass the configured `api`
 * instance (plain `axios`) so the response interceptor's refresh logic never recurses into itself.
 */
export const authApi = {
  register: (payload: RegisterPayload) =>
    api.post<AuthResponse>('/auth/register', payload).then(r => r.data),

  login: (payload: LoginPayload) =>
    api.post<AuthResponse>('/auth/login', payload).then(r => r.data),

  refresh: (refreshToken: string) =>
    axios.post<AuthResponse>('/api/v1/auth/refresh', { refreshToken }).then(r => r.data),

  logout: (refreshToken: string) =>
    axios.post('/api/v1/auth/logout', { refreshToken }),
}
