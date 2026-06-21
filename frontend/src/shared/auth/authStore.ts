import { create } from 'zustand'
import { persist } from 'zustand/middleware'

/** Authenticated user data kept in the session store. */
export interface AuthUser {
  id: number
  email: string
  role: 'PLAYER' | 'OPERATOR' | 'MATH_ANALYST' | 'ADMIN'
  locale: string
}

interface AuthState {
  token: string | null
  refreshToken: string | null
  user: AuthUser | null
  setAuth: (token: string, refreshToken: string, user: AuthUser) => void
  /** Replaces just the tokens after a silent refresh, keeping the current user (HU-13). */
  setTokens: (token: string, refreshToken: string) => void
  clearAuth: () => void
  isAuthenticated: () => boolean
}

/** Session store (tokens + user) persisted to localStorage under the "nova-auth" key. */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      refreshToken: null,
      user: null,

      setAuth: (token, refreshToken, user) => set({ token, refreshToken, user }),

      setTokens: (token, refreshToken) => set({ token, refreshToken }),

      clearAuth: () => set({ token: null, refreshToken: null, user: null }),

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
    case 'ADMIN':        return '/admin'
    case 'OPERATOR':     return '/operator'
    case 'MATH_ANALYST': return '/math'
    default:             return '/'
  }
}
