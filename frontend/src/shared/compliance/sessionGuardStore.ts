import { create } from 'zustand'

/** Session loss threshold (in cents) above which a responsible-gaming pause is suggested. */
export const LOSS_THRESHOLD_CENTS = 50_000 // 500 €

interface SessionGuardState {
  /** Cumulative net loss of the current session, in cents (never negative). */
  netLossCents: number
  /** Records a finished round, accumulating the net loss (bet − win). */
  recordRound: (betCents: number, winCents: number) => void
  /** Resets the session counter (e.g. on logout). */
  reset: () => void
}

/**
 * Client-side guard that tracks the session's net loss to trigger a responsible-gaming
 * notice (HU-12). Persistent loss limits and self-exclusion are post-MVP (1.5-D7).
 */
export const useSessionGuard = create<SessionGuardState>(set => ({
  netLossCents: 0,
  recordRound: (betCents, winCents) =>
    set(s => ({ netLossCents: Math.max(0, s.netLossCents + (betCents - winCents)) })),
  reset: () => set({ netLossCents: 0 }),
}))

/** Whether the accumulated session loss has reached the pause threshold. */
export function isLossThresholdExceeded(netLossCents: number): boolean {
  return netLossCents >= LOSS_THRESHOLD_CENTS
}
