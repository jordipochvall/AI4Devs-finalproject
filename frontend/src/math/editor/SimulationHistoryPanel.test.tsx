import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, beforeAll, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

const { useSimulationHistory, useSimulationExplanations } = vi.hoisted(() => ({
  useSimulationHistory: vi.fn(),
  useSimulationExplanations: vi.fn(),
}))

vi.mock('../api/mathApi', () => ({ useSimulationHistory, useSimulationExplanations }))

import SimulationHistoryPanel from './SimulationHistoryPanel'

describe('SimulationHistoryPanel (HU-18)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })

  it('AC1: lists simulations with status and empirical RTP', () => {
    useSimulationHistory.mockReturnValue({
      data: {
        content: [{ id: 5, gameConfigId: 100, status: 'COMPLETED', numSpins: 1000, betCents: 100, rtpEmpirical: 0.9612, startedAt: '2026-06-01T10:00:00Z', completedAt: '2026-06-01T10:01:00Z' }],
        page: 0, size: 10, totalElements: 1, totalPages: 1,
      },
      isLoading: false,
    })
    useSimulationExplanations.mockReturnValue({ data: [], isLoading: false })

    render(<SimulationHistoryPanel gameId={3} />)
    expect(screen.getByText('#5')).toBeInTheDocument()
    expect(screen.getByText('COMPLETED')).toBeInTheDocument()
    expect(screen.getByText('96.12%')).toBeInTheDocument()
  })

  it('AC2: expanding a simulation shows its AI Q&A thread', () => {
    useSimulationHistory.mockReturnValue({
      data: {
        content: [{ id: 5, gameConfigId: 100, status: 'COMPLETED', numSpins: 1000, betCents: 100, rtpEmpirical: 0.96, startedAt: '2026-06-01T10:00:00Z', completedAt: null }],
        page: 0, size: 10, totalElements: 1, totalPages: 1,
      },
      isLoading: false,
    })
    useSimulationExplanations.mockReturnValue({
      data: [{ question: '¿RTP ok?', answer: 'Sí, converge.', model: 'claude-haiku-4-5', askedAt: '2026-06-01T10:02:00Z' }],
      isLoading: false,
    })

    render(<SimulationHistoryPanel gameId={3} />)
    fireEvent.click(screen.getByText('Ver IA'))
    expect(screen.getByText(/¿RTP ok\?/)).toBeInTheDocument()
    expect(screen.getByText(/Sí, converge\./)).toBeInTheDocument()
  })
})
