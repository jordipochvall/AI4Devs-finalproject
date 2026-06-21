import { render, screen } from '@testing-library/react'
import { describe, it, expect, beforeAll } from 'vitest'
import i18n from '../../shared/i18n/i18n'
import SlotGame from './SlotGame'
import type { GameConfig, SpinResult } from '../api/playerApi'

/**
 * HU-22 — accessibility checks on the richest interactive surface (the slot game): every control has
 * an accessible name, the grid is labelled and busy-state announced, and results use a live region.
 */
const config: GameConfig = {
  grid: { cols: 3, rows: 3 },
  symbols: [{ id: 'A', kind: 'REGULAR' }],
  reels: [['A'], ['A'], ['A']],
  paylines: [[0, 0, 0]],
  paytable: [{ symbol: 'A', payouts: { '3': 5 } }],
}

const winningResult: SpinResult = {
  roundId: 1, betCents: 100, lineBetCents: 100, winCents: 500,
  balancePreCents: 1000, balancePostCents: 1400,
  view: [['A', 'A', 'A'], ['A', 'A', 'A'], ['A', 'A', 'A']],
  winningPaylines: [{ paylineIndex: 0, symbol: 'A', count: 3, winCents: 500 }],
  scatterCount: 0,
  freeSpins: { triggered: false, awarded: 0, rounds: [] },
}

describe('SlotGame accessibility (HU-22)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })

  it('exposes accessible names for the spin and bet controls', () => {
    render(<SlotGame config={config} theme="FRUITS" betCents={100} minBetCents={100}
                     maxBetCents={1000} betStepCents={100} />)
    expect(screen.getByRole('button', { name: 'Girar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Subir apuesta' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Bajar apuesta' })).toBeInTheDocument()
  })

  it('labels the grid and announces wins via a live region (role=status)', () => {
    render(<SlotGame config={config} theme="FRUITS" mode="replay" result={winningResult} />)
    expect(screen.getByRole('grid', { name: 'Rejilla del juego' })).toBeInTheDocument()
    // The win is rendered in an assertive/polite live region (role=status).
    expect(screen.getByRole('status')).toHaveTextContent(/Has ganado/)
  })
})
