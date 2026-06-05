import { describe, it, expect, beforeEach } from 'vitest'
import { useAuthStore, roleHomeRoute } from './authStore'
import type { AuthUser } from './authStore'

const player: AuthUser = { id: 1, email: 'p@test.com', role: 'PLAYER',      locale: 'es' }
const operator: AuthUser = { id: 2, email: 'o@test.com', role: 'OPERATOR',   locale: 'es' }
const math: AuthUser    = { id: 3, email: 'm@test.com', role: 'MATH_ANALYST', locale: 'en' }

beforeEach(() => {
  useAuthStore.getState().clearAuth()
})

describe('authStore', () => {
  it('starts unauthenticated', () => {
    const { token, user, isAuthenticated } = useAuthStore.getState()
    expect(token).toBeNull()
    expect(user).toBeNull()
    expect(isAuthenticated()).toBe(false)
  })

  it('setAuth persists token and user', () => {
    useAuthStore.getState().setAuth('tok123', player)
    const { token, user, isAuthenticated } = useAuthStore.getState()
    expect(token).toBe('tok123')
    expect(user?.email).toBe('p@test.com')
    expect(isAuthenticated()).toBe(true)
  })

  it('clearAuth removes session', () => {
    useAuthStore.getState().setAuth('tok', player)
    useAuthStore.getState().clearAuth()
    expect(useAuthStore.getState().isAuthenticated()).toBe(false)
    expect(useAuthStore.getState().token).toBeNull()
  })
})

describe('roleHomeRoute', () => {
  it('PLAYER → /', ()             => expect(roleHomeRoute(player.role)).toBe('/'))
  it('OPERATOR → /operator', ()   => expect(roleHomeRoute(operator.role)).toBe('/operator'))
  it('MATH_ANALYST → /math', ()   => expect(roleHomeRoute(math.role)).toBe('/math'))
})
