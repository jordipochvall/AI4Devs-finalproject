import { create } from 'zustand'
import { persist } from 'zustand/middleware'

/** Authenticated user data kept in the session store. */
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

/** Session store (token + user) persisted to localStorage under the "nova-auth" key. */
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
      name: 'nova-auth',
    },
  ),
)

/** Returns the home route for an authenticated user's role. */
export const roleHomeRoute = (role: string): string => {
  switch (role) {
    case 'OPERATOR':     return '/operator'
    case 'MATH_ANALYST': return '/math'
    default:             return '/'
  }
}
