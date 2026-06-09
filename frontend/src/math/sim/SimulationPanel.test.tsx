import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'
import type { SimulationStatus } from '../api/mathApi'

const mutate = vi.fn()
let simData: SimulationStatus | undefined

// Mock the data hooks so the panel does not hit the network.
vi.mock('../api/mathApi', () => ({
  useLaunchSimulation: () => ({ mutate, isPending: false, isError: false, error: null }),
  useSimulation: () => ({ data: simData }),
  useExplain: () => ({ mutate: vi.fn(), isPending: false }), // embedded ExplainBox (HU-8)
}))

import SimulationPanel from './SimulationPanel'

const CONFIG = { reels: [['A', 'B', 'A'], ['A', 'B', 'B']] }

const COMPLETED: SimulationStatus = {
  simulationId: 1, status: 'COMPLETED', numSpins: 1000, betCents: 100,
  startedAt: '', completedAt: '', durationMs: 1234,
  rtpEmpirical: 0.95, rtpStdError: 0.001, rtpBaseGame: 0.80, rtpFreeSpins: 0.15,
  hitFrequency: 0.25, volatility: 5.2, maxWinMultiplier: 120, freeSpinTriggerFreq: 0.01,
  longestDryStreak: 42,
  prizeDistribution: { '0': 900, '(0,1]': 50, '(1,2]': 50 },
  convergenceSample: [{ spins: 100, rtp: 0.90 }, { spins: 200, rtp: 0.94 }],
  rtpBreakdown: { baseGame: 0.80, freeSpins: 0.15, bySymbol: { A: 0.50, B: 0.45 } },
  errorMessage: null,
}

const renderPanel = () =>
  render(<SimulationPanel configId={15} rtpTarget={0.95} config={CONFIG} />)

describe('SimulationPanel', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => { mutate.mockClear(); simData = undefined })

  it('launches a simulation when "Simular" is pressed', () => {
    renderPanel()
    fireEvent.click(screen.getByRole('button', { name: 'Simular' }))
    expect(mutate).toHaveBeenCalledOnce()
  })

  it('shows a running indicator while the simulation runs (AC3)', () => {
    simData = { ...COMPLETED, status: 'RUNNING', numSpins: 10_000_000 }
    renderPanel()
    expect(screen.getByRole('status')).toBeInTheDocument()
  })

  it('renders the dashboard with the RTP, CI and target verdict when completed (AC4)', () => {
    simData = COMPLETED
    renderPanel()
    expect(screen.getByTestId('sim-dashboard')).toBeInTheDocument()
    expect(screen.getByText('95.00%')).toBeInTheDocument()
    // target 0.95 is inside the CI → green verdict text.
    expect(screen.getByText(/dentro del intervalo de confianza/)).toBeInTheDocument()
  })
})
