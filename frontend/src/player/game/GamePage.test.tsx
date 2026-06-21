import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'
import type { SpinResult } from '../api/playerApi'

// Shared, mutable handles between the mock factory and the tests.
const h = vi.hoisted(() => ({
  spin: vi.fn(),
  wallet: { balanceCents: 100_000 },
}))

vi.mock('../api/playerApi', () => ({
  default: { spin: h.spin },
  useGame: () => ({
    data: {
      id: 3, name: 'Frutas', theme: 'FRUITS', coverImageUrl: '', grid: { cols: 3, rows: 3 },
      minBetCents: 100, maxBetCents: 1000, betStepCents: 100, jackpotCents: 500000,
      config: { grid: { cols: 3, rows: 3 }, symbols: [], reels: [], paylines: [[1, 1, 1]], paytable: [] },
    },
    isLoading: false, isError: false,
  }),
  useWallet: () => ({ data: { balanceCents: h.wallet.balanceCents, currency: 'EUR' } }),
}))

import GamePage from './GamePage'

function spinResult(balancePostCents: number): SpinResult {
  return {
    roundId: Math.floor(Math.random() * 1e9), betCents: 100, lineBetCents: 33, winCents: 0,
    balancePreCents: balancePostCents + 100, balancePostCents,
    view: [['A', 'A', 'A'], ['A', 'A', 'A'], ['A', 'A', 'A']],
    winningPaylines: [], scatterCount: 0,
    freeSpins: { triggered: false, awarded: 0, rounds: [] },
  }
}

const renderGame = () => {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={['/play/3']}>
        <Routes><Route path="/play/:gameId" element={<GamePage />} /></Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

/** Waits until the bet has been initialised (so auto-spin can start). */
const waitForReady = () => waitFor(() => expect(screen.getByText(/1,00/)).toBeInTheDocument())

describe('GamePage auto-spin (HU-9)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => { h.spin.mockReset(); h.wallet.balanceCents = 100_000 })

  it('shows the progressive jackpot ticker when the game has a pool (HU-26)', async () => {
    renderGame()
    await waitForReady()
    expect(screen.getByText(/Bote progresivo/)).toBeInTheDocument()
    expect(screen.getByText(/5[.\s ]?000,00/)).toBeInTheDocument()
  })

  it('runs N spins then stops (AC1/AC2)', async () => {
    h.spin.mockImplementation(() => Promise.resolve(spinResult(99_000)))
    renderGame()
    await waitForReady()

    fireEvent.change(screen.getByLabelText('Nº de giros automáticos'), { target: { value: '3' } })
    fireEvent.click(screen.getByRole('button', { name: 'Auto' }))

    await waitFor(() => expect(h.spin).toHaveBeenCalledTimes(3))
    // Batch finished: the Auto button is back (no Stop button).
    await waitFor(() => expect(screen.getByRole('button', { name: 'Auto' })).toBeInTheDocument())
    expect(h.spin).toHaveBeenCalledTimes(3)
  })

  it('stops with a responsible-gaming pause when the balance drops below the threshold (AC2)', async () => {
    let n = 0
    h.spin.mockImplementation(() => { n++; return Promise.resolve(spinResult(100_000 - 100 * n)) })
    renderGame()
    await waitForReady()

    // Stop if balance < 999.00 € (99 900 cents): triggers after the 2nd spin (post 99 800).
    fireEvent.change(screen.getByLabelText('Parar si saldo < (€)'), { target: { value: '999' } })
    fireEvent.click(screen.getByRole('button', { name: 'Auto' }))

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent(/juego responsable/i))
    expect(h.spin).toHaveBeenCalledTimes(2)
  })

  it('does not spin when the balance is below the bet (AC4)', async () => {
    h.wallet.balanceCents = 50 // below the 100-cent bet
    renderGame()
    await waitFor(() => expect(screen.getByText(/0,50/)).toBeInTheDocument()) // balance rendered

    fireEvent.click(screen.getByRole('button', { name: 'Auto' }))

    expect(h.spin).not.toHaveBeenCalled()
    expect(screen.getByRole('alert')).toHaveTextContent(/juego responsable/i)
  })

  it('stops the batch when "Detener" is pressed (AC3)', async () => {
    let resolveFirst: (r: SpinResult) => void = () => {}
    h.spin.mockImplementation(() => new Promise<SpinResult>(res => { resolveFirst = res }))
    renderGame()
    await waitForReady()

    fireEvent.change(screen.getByLabelText('Nº de giros automáticos'), { target: { value: '5' } })
    fireEvent.click(screen.getByRole('button', { name: 'Auto' }))

    // First spin is in flight → the Stop button shows the remaining count.
    const stop = await screen.findByRole('button', { name: 'Detener (5)' })
    fireEvent.click(stop)

    // Resolving the in-flight spin must NOT chain another one.
    resolveFirst(spinResult(99_000))
    await waitFor(() => expect(screen.getByRole('button', { name: 'Auto' })).toBeInTheDocument())
    expect(h.spin).toHaveBeenCalledTimes(1)
  })
})
