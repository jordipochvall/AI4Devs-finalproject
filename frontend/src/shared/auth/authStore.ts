import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface AuthUser {
  id: number
  email: string
  role: 'PLAYER' | 'OPERATOR' | 'MATH_ANALYST'
  locale: string
}

interface AuthState {
  token: string | null
  user: AuthUser | null
  setAuth: (token: string, user: AuthUser) => void
  clearAuth: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,

      setAuth: (token, user) => set({ token, user }),

      clearAuth: () => set({ token: null, user: null }),

      isAuthenticated: () => !!get().token,
    }),
    {
      name: 'nova-auth',  // clave en localStorage
    }
  )
)

/** Devuelve la ruta de inicio según el rol del usuario autenticado. */
export const roleHomeRoute = (role: string): string => {
  switch (role) {
    case 'OPERATOR':     return '/operator'
    case 'MATH_ANALYST': return '/math'
    default:             return '/'
  }
}
