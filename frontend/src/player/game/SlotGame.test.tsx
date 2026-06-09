import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, beforeAll, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'
import SlotGame from './SlotGame'
import type { GameConfig, SpinResult } from '../api/playerApi'

/** Minimal 3x3 config (one middle payline). */
const CONFIG_3x3: GameConfig = {
  grid: { cols: 3, rows: 3 },
  symbols: [
    { id: 'A', kind: 'REGULAR' },
    { id: 'B', kind: 'REGULAR' },
  ],
  reels: [['A', 'B', 'A'], ['A', 'B', 'A'], ['A', 'B', 'A']],
  paylines: [[1, 1, 1]],
  paytable: [{ symbol: 'A', payouts: { '3': 5 } }],
}

/** 5x3 config to prove the component is game-agnostic (AC5). */
const CONFIG_5x3: GameConfig = {
  grid: { cols: 5, rows: 3 },
  symbols: [{ id: 'X', kind: 'REGULAR' }],
  reels: [['X', 'X', 'X'], ['X', 'X', 'X'], ['X', 'X', 'X'], ['X', 'X', 'X'], ['X', 'X', 'X']],
  paylines: [[1, 1, 1, 1, 1]],
  paytable: [{ symbol: 'X', payouts: { '5': 10 } }],
}

function spinResult(over: Partial<SpinResult> = {}): SpinResult {
  return {
    roundId: 1, betCents: 100, lineBetCents: 100, winCents: 500,
    balancePreCents: 100000, balancePostCents: 100400,
    view: [['W', 'C', 'C'], ['K', 'C', 'K'], ['C', 'A', 'P']],
    winningPaylines: [{ paylineIndex: 0, symbol: 'C', count: 3, winCents: 500 }],
    scatterCount: 0,
    freeSpins: { triggered: false, awarded: 0, rounds: [] },
    ...over,
  }
}

describe('SlotGame', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })

  it('renders the grid from the spin view in their positions (AC1)', () => {
    render(<SlotGame config={CONFIG_3x3} theme="FRUITS" result={spinResult()} stepMs={0} />)
    const grid = screen.getByTestId('slot-grid')
    expect(grid).toHaveStyle({ gridTemplateColumns: 'repeat(3, 1fr)' })
    // 9 cells for a 3x3 grid.
    expect(grid.querySelectorAll('.slot-cell')).toHaveLength(9)
    // The view symbol at column 0, row 0 is "W".
    expect(grid.querySelector('[data-symbol="W"]')).not.toBeNull()
  })

  it('calls onSpinClick when Spin is pressed (AC2)', () => {
    const onSpinClick = vi.fn()
    render(<SlotGame config={CONFIG_3x3} theme="FRUITS" betCents={100}
                     minBetCents={100} maxBetCents={500} betStepCents={100}
                     onSpinClick={onSpinClick} stepMs={0} />)
    fireEvent.click(screen.getByRole('button', { name: 'Girar' }))
    expect(onSpinClick).toHaveBeenCalledOnce()
  })

  it('highlights the winning payline cells', () => {
    render(<SlotGame config={CONFIG_3x3} theme="FRUITS" result={spinResult()} stepMs={0} />)
    // Middle payline [1,1,1] → the three middle-row cells are highlighted.
    expect(document.querySelectorAll('.slot-cell-win')).toHaveLength(3)
  })

  it('plays the free-spins cinematic, animating each round (AC3)', async () => {
    const result = spinResult({
      freeSpins: {
        triggered: true, awarded: 1,
        rounds: [spinResult({
          roundId: 2, betCents: 0, winCents: 300, multiplier: 3,
          view: [['F', 'F', 'F'], ['F', 'F', 'F'], ['F', 'F', 'F']],
          winningPaylines: [{ paylineIndex: 0, symbol: 'F', count: 3, winCents: 300 }],
        } as Partial<SpinResult>)],
      },
    })
    render(<SlotGame config={CONFIG_3x3} theme="FRUITS" result={result} stepMs={0} />)
    // After the cinematic, the free-spin view ("F") and its label are shown.
    await waitFor(() => expect(screen.getByText('Tirada gratis 1/1')).toBeInTheDocument())
    expect(document.querySelector('[data-symbol="F"]')).not.toBeNull()
  })

  it('shows an error without altering the grid (AC4)', () => {
    render(<SlotGame config={CONFIG_3x3} theme="FRUITS"
                     errorMessage="Saldo insuficiente o apuesta no válida." stepMs={0} />)
    expect(screen.getByRole('alert')).toHaveTextContent('Saldo insuficiente')
    // No result passed → the grid keeps the initial view (top of the reels), no win cells.
    expect(document.querySelectorAll('.slot-cell-win')).toHaveLength(0)
  })

  it('is game-agnostic: a 5x3 config renders 15 cells (AC5)', () => {
    render(<SlotGame config={CONFIG_5x3} theme="SPACE" stepMs={0} />)
    expect(screen.getByTestId('slot-grid').querySelectorAll('.slot-cell')).toHaveLength(15)
  })

  it('hides the spin controls in replay mode (AC6)', () => {
    render(<SlotGame config={CONFIG_3x3} theme="FRUITS" mode="replay" result={spinResult()} stepMs={0} />)
    expect(screen.queryByRole('button', { name: 'Girar' })).not.toBeInTheDocument()
  })

  it('starts auto-spin with the chosen count (HU-9 AC1)', () => {
    const onAutoStart = vi.fn()
    render(<SlotGame config={CONFIG_3x3} theme="FRUITS" betCents={100}
                     minBetCents={100} maxBetCents={500} betStepCents={100}
                     onAutoStart={onAutoStart} stepMs={0} />)
    fireEvent.click(screen.getByRole('button', { name: 'Auto' }))
    expect(onAutoStart).toHaveBeenCalledWith(10, 0) // default count 10, no stop-below
  })

  it('shows a Stop button with the remaining count while auto-spinning (HU-9 AC4)', () => {
    const onAutoStop = vi.fn()
    render(<SlotGame config={CONFIG_3x3} theme="FRUITS" betCents={100}
                     autoRemaining={5} onAutoStop={onAutoStop} stepMs={0} />)
    const stop = screen.getByRole('button', { name: 'Detener (5)' })
    fireEvent.click(stop)
    expect(onAutoStop).toHaveBeenCalledOnce()
  })

  it('shows the responsible-gaming pause message when auto-spin is paused (HU-9 AC3)', () => {
    render(<SlotGame config={CONFIG_3x3} theme="FRUITS" betCents={100} autoPaused stepMs={0} />)
    expect(screen.getByRole('alert')).toHaveTextContent(/juego responsable/i)
  })
})
