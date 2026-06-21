import { authApi } from './authApi'
import { useAuthStore } from './authStore'

/**
 * Ends the session: revokes the refresh token server-side (best-effort, fire-and-forget) and clears
 * the local session (HU-13 AC3). Revocation failures are ignored — the local session is cleared
 * regardless so the user is always logged out.
 */
export function logoutSession(): void {
  const { refreshToken, clearAuth } = useAuthStore.getState()
  if (refreshToken) {
    authApi.logout(refreshToken).catch(() => { /* revocation is best-effort */ })
  }
  clearAuth()
}
