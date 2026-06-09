import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'
import type { ReplayResult } from '../api/operatorApi'

let replayState: { data?: ReplayResult; isLoading: boolean; isError: boolean; error?: unknown }

vi.mock('../api/operatorApi', () => ({
  useReplay: () => replayState,
}))

import ReplayPage from './ReplayPage'

const REPLAY: ReplayResult = {
  roundId: 90412, gameId: 3, gameConfigId: 15, rngSeed: -488113844992001023,
  config: { grid: { cols: 3, rows: 3 }, symbols: [], reels: [], paylines: [[1, 1, 1]], paytable: [] },
  result: {
    roundId: 90412, betCents: 100, lineBetCents: 33, winCents: 750,
    balancePreCents: 98500, balancePostCents: 99150,
    view: [['STAR', 'COMET', 'COMET'], ['K', 'STAR', 'K'], ['COMET', 'A', 'PLANET']],
    winningPaylines: [{ paylineIndex: 0, symbol: 'COMET', count: 3, winCents: 750 }],
    scatterCount: 1,
    freeSpins: { triggered: false, awarded: 0, rounds: [] },
  },
}

const renderAt = () =>
  render(
    <MemoryRouter initialEntries={['/operator/replay/90412']}>
      <Routes>
        <Route path="/operator/replay/:roundId" element={<ReplayPage />} />
      </Routes>
    </MemoryRouter>,
  )

describe('ReplayPage', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => { replayState = { isLoading: false, isError: false } })

  it('shows the metadata and replays the recorded grid (AC1/AC2)', () => {
    replayState = { data: REPLAY, isLoading: false, isError: false }
    renderAt()
    expect(screen.getByText('90412')).toBeInTheDocument()             // round id metadata
    expect(screen.getByTestId('slot-grid')).toBeInTheDocument()        // SlotGame renders the view
    expect(screen.getByText('PLANET')).toBeInTheDocument()             // a recorded symbol
    expect(screen.getByRole('button', { name: 'Reproducir de nuevo' })).toBeInTheDocument()
  })

  it('reuses <SlotGame> in replay mode without a spin button (AC6)', () => {
    replayState = { data: REPLAY, isLoading: false, isError: false }
    renderAt()
    expect(screen.queryByRole('button', { name: 'Girar' })).not.toBeInTheDocument()
  })

  it('shows "round not found" on a 404 (AC5)', () => {
    replayState = { isLoading: false, isError: true, error: { response: { status: 404 } } }
    renderAt()
    expect(screen.getByText('Partida no encontrada.')).toBeInTheDocument()
  })
})
