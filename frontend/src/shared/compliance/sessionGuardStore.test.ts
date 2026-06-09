import { describe, it, expect, beforeEach } from 'vitest'
import { useSessionGuard, isLossThresholdExceeded, LOSS_THRESHOLD_CENTS } from './sessionGuardStore'

beforeEach(() => useSessionGuard.getState().reset())

describe('sessionGuardStore', () => {
  it('starts with zero net loss', () => {
    expect(useSessionGuard.getState().netLossCents).toBe(0)
  })

  it('accumulates net loss across rounds (bet - win)', () => {
    const { recordRound } = useSessionGuard.getState()
    recordRound(1000, 0)     // loss 1000
    recordRound(1000, 400)   // loss 600
    expect(useSessionGuard.getState().netLossCents).toBe(1600)
  })

  it('never goes below zero on a net win', () => {
    const { recordRound } = useSessionGuard.getState()
    recordRound(1000, 5000)  // net win
    expect(useSessionGuard.getState().netLossCents).toBe(0)
  })

  it('flags the threshold once reached', () => {
    expect(isLossThresholdExceeded(LOSS_THRESHOLD_CENTS - 1)).toBe(false)
    expect(isLossThresholdExceeded(LOSS_THRESHOLD_CENTS)).toBe(true)
  })
})
