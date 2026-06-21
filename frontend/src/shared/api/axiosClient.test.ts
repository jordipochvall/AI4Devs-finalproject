import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import type { AxiosAdapter } from 'axios'

// Mock the auth API so the interceptor's dynamic import resolves to controllable spies and never
// performs a real /auth/refresh call (which would recurse into the same axios instance). The spies
// are created via vi.hoisted so they exist when the (hoisted) vi.mock factory runs.
const { refresh, logout } = vi.hoisted(() => ({ refresh: vi.fn(), logout: vi.fn() }))
vi.mock('../auth/authApi', () => ({ authApi: { refresh, logout } }))

import api from './axiosClient'
import { useAuthStore } from '../auth/authStore'
import type { AuthUser } from '../auth/authStore'

const player: AuthUser = { id: 1, email: 'p@test.com', role: 'PLAYER', locale: 'es' }

/** Adapter that 401s a protected call until the request carries the rotated "Bearer NEW" token. */
function makeAdapter(): { adapter: AxiosAdapter; calls: () => number } {
  let calls = 0
  const adapter: AxiosAdapter = config => {
    calls++
    const authorized = config.headers?.Authorization === 'Bearer NEW'
    if (authorized) {
      return Promise.resolve({ data: { ok: true }, status: 200, statusText: 'OK', headers: {}, config })
    }
    // A custom adapter must reject on a bad status itself (axios only applies validateStatus in its
    // native adapters). Mimic the AxiosError shape the interceptor reads (.response.status, .config).
    return Promise.reject({
      isAxiosError: true,
      config,
      response: { data: {}, status: 401, statusText: 'Unauthorized', headers: {}, config },
    })
  }
  return { adapter, calls: () => calls }
}

let restoreLocation: () => void

beforeEach(() => {
  refresh.mockReset()
  logout.mockReset()
  useAuthStore.getState().setAuth('OLD', 'R1', player)

  // Stub window.location so the redirect assertion works under jsdom.
  const original = window.location
  delete (window as unknown as { location?: Location }).location
  ;(window as unknown as { location: { href: string } }).location = { href: '' }
  restoreLocation = () => { (window as unknown as { location: Location }).location = original }
})

afterEach(() => {
  restoreLocation()
})

describe('axiosClient refresh interceptor (HU-13)', () => {
  it('AC1: renews on 401 and retries the original request transparently', async () => {
    const { adapter } = makeAdapter()
    api.defaults.adapter = adapter
    refresh.mockResolvedValue({ token: 'NEW', refreshToken: 'R2', tokenType: 'Bearer', expiresIn: 3600, user: player })

    const resp = await api.get('/protected')

    expect(resp.data).toEqual({ ok: true })
    expect(refresh).toHaveBeenCalledTimes(1)
    expect(useAuthStore.getState().token).toBe('NEW')
    expect(useAuthStore.getState().refreshToken).toBe('R2')
  })

  it('AC3: concurrent 401s trigger a single refresh (single-flight)', async () => {
    const { adapter } = makeAdapter()
    api.defaults.adapter = adapter
    refresh.mockImplementation(async () => {
      await new Promise(r => setTimeout(r, 10)) // widen the window so requests overlap
      return { token: 'NEW', refreshToken: 'R2', tokenType: 'Bearer', expiresIn: 3600, user: player }
    })

    const results = await Promise.all([
      api.get('/protected'),
      api.get('/protected'),
      api.get('/protected'),
    ])

    results.forEach(r => expect(r.data).toEqual({ ok: true }))
    expect(refresh).toHaveBeenCalledTimes(1)
  })

  it('AC2: a failed refresh clears the session and redirects to /login', async () => {
    const { adapter } = makeAdapter()
    api.defaults.adapter = adapter
    refresh.mockRejectedValue(new Error('refresh expired'))

    await expect(api.get('/protected')).rejects.toBeDefined()

    expect(useAuthStore.getState().token).toBeNull()
    expect(window.location.href).toBe('/login')
  })
})
