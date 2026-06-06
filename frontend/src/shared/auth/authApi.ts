import api from '../api/axiosClient'
import type { AuthUser } from './authStore'

/** Authentication response returned by register/login. */
export interface AuthResponse {
  token: string
  tokenType: string
  expiresIn: number
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

/** Auth API calls. */
export const authApi = {
  register: (payload: RegisterPayload) =>
    api.post<AuthResponse>('/auth/register', payload).then(r => r.data),

  login: (payload: LoginPayload) =>
    api.post<AuthResponse>('/auth/login', payload).then(r => r.data),
}
