import { describe, it, expect } from 'vitest'
import { nextAutoDecision } from './autoSpin'

describe('nextAutoDecision (auto-spin safeguards)', () => {
  it('continues while spins remain and the balance covers the bet (AC1)', () => {
    expect(nextAutoDecision(5, 10_000, 100, 0)).toBe('continue')
  })

  it('stops as done when no spins remain (AC2)', () => {
    expect(nextAutoDecision(0, 10_000, 100, 0)).toBe('done')
  })

  it('stops as insufficient when the balance is below the bet (AC5)', () => {
    expect(nextAutoDecision(5, 50, 100, 0)).toBe('insufficient')
  })

  it('stops at the configured balance threshold (AC3)', () => {
    expect(nextAutoDecision(5, 400, 100, 500)).toBe('threshold')
  })

  it('prioritises the safeguards over completion', () => {
    // Even with no spins left, an insufficient balance reports the safeguard (shows a pause).
    expect(nextAutoDecision(0, 50, 100, 0)).toBe('insufficient')
    expect(nextAutoDecision(0, 400, 100, 500)).toBe('threshold')
  })
})
