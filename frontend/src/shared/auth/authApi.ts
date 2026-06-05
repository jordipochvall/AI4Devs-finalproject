import api from '../api/axiosClient'
import type { AuthUser } from './authStore'

export interface AuthResponse {
  token: string
  tokenType: string
  expiresIn: number
  user: AuthUser
}

export interface RegisterPayload {
  email: string
  password: string
  birthDate: string  // ISO: "YYYY-MM-DD"
  locale?: string
}

export interface LoginPayload {
  email: string
  password: string
}

export const authApi = {
  register: (payload: RegisterPayload) =>
    api.post<AuthResponse>('/auth/register', payload).then(r => r.data),

  login: (payload: LoginPayload) =>
    api.post<AuthResponse>('/auth/login', payload).then(r => r.data),
}
