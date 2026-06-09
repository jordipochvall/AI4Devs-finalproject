import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeAll, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

// Mock the data hooks so the page does not hit the network.
vi.mock('../api/operatorApi', () => ({
  usePlayers: () => ({ data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 } }),
  useRounds: () => ({
    data: {
      content: [
        { id: 90412, playerId: 7, gameId: 3, gameConfigId: 15, betCents: 100, winCents: 500,
          balancePostCents: 100400, freeSpin: false, triggeringRoundId: null, createdAt: '2026-05-04T09:12:33Z' },
      ],
      page: 0, size: 20, totalElements: 1, totalPages: 1,
    },
    isLoading: false, isError: false,
  }),
}))

import AuditPage from './AuditPage'

const renderPage = () =>
  render(<MemoryRouter initialEntries={['/operator/audit']}><AuditPage /></MemoryRouter>)

describe('AuditPage', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })

  it('lists rounds with the round id and a Replay action (AC6)', () => {
    renderPage()
    expect(screen.getByText('90412')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Replay' })).toBeInTheDocument()
  })

  it('formats the amounts per locale (AC4)', () => {
    renderPage()
    // bet 100 cents → 1,00 € ; win 500 cents → 5,00 € (thousands/decimal per ICU)
    expect(screen.getByText(/1,00/)).toBeInTheDocument()
    expect(screen.getByText(/5,00/)).toBeInTheDocument()
  })

  it('disables "previous" on the first page (AC5)', () => {
    renderPage()
    expect(screen.getByRole('button', { name: 'Anterior' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Siguiente' })).toBeDisabled() // single page
  })
})
