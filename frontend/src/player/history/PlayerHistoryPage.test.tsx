import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeAll, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

vi.mock('../api/playerApi', () => ({
  useWalletTransactions: () => ({
    data: {
      content: [
        { id: 1, type: 'RECHARGE', amountCents: 500000, balanceAfterCents: 500000, gameRoundId: null, createdAt: '2026-06-01T10:00:00Z' },
        { id: 2, type: 'BET', amountCents: -100, balanceAfterCents: 499900, gameRoundId: 9, createdAt: '2026-06-01T10:05:00Z' },
      ],
      page: 0, size: 10, totalElements: 2, totalPages: 1,
    },
    isLoading: false, isError: false,
  }),
  usePlayerRounds: () => ({
    data: {
      content: [
        { id: 9, gameId: 3, betCents: 100, winCents: 0, balancePostCents: 499900, freeSpin: false, createdAt: '2026-06-01T10:05:00Z' },
      ],
      page: 0, size: 10, totalElements: 1, totalPages: 1,
    },
    isLoading: false, isError: false,
  }),
}))

import PlayerHistoryPage from './PlayerHistoryPage'

const renderPage = () => render(<MemoryRouter><PlayerHistoryPage /></MemoryRouter>)

describe('PlayerHistoryPage (HU-14)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })

  it('AC1: lists wallet movements with localized type and formatted amounts', () => {
    renderPage()
    expect(screen.getByText('Recarga')).toBeInTheDocument()
    expect(screen.getByText('Apuesta')).toBeInTheDocument()
    // Amounts render formatted as currency with two decimals (locale-agnostic check).
    expect(screen.getAllByText(/[,.]00/).length).toBeGreaterThan(0)
  })

  it('AC2: switching to the rounds tab shows the player rounds', () => {
    renderPage()
    fireEvent.click(screen.getByText('Partidas'))
    expect(screen.getByText(/#3/)).toBeInTheDocument()
  })

  it('AC3: pagination buttons disable at the extremes (single page)', () => {
    renderPage()
    expect(screen.getByText('Anterior')).toBeDisabled()
    expect(screen.getByText('Siguiente')).toBeDisabled()
  })
})
