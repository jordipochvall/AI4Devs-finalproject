import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeAll, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

vi.mock('../api/operatorApi', () => ({
  useDashboard: () => ({
    data: {
      from: '1970-01-01T00:00:00Z', to: '2026-06-20T00:00:00Z',
      activePlayers: 7, ggrCents: 123400, totalRounds: 40,
      topGames: [
        { gameId: 3, name: 'Frutas', theme: 'FRUITS', rounds: 25 },
        { gameId: 5, name: 'Espacial', theme: 'SPACE', rounds: 15 },
      ],
    },
    isLoading: false, isError: false,
  }),
}))

import OperatorDashboardPage from './OperatorDashboardPage'

const renderPage = () => render(<MemoryRouter><OperatorDashboardPage /></MemoryRouter>)

describe('OperatorDashboardPage (HU-16)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })

  it('AC1: renders the KPI values and the most-played games', () => {
    renderPage()
    expect(screen.getByText('7')).toBeInTheDocument()       // active players
    expect(screen.getByText('40')).toBeInTheDocument()      // total rounds
    expect(screen.getByText('Frutas')).toBeInTheDocument()  // top game
    expect(screen.getByText('25')).toBeInTheDocument()      // its round count
  })

  it('AC2: GGR is formatted as currency with two decimals', () => {
    renderPage()
    // 123400 cents → currency with two decimals (locale-agnostic check).
    expect(screen.getAllByText(/[,.]00/).length).toBeGreaterThan(0)
  })

  it('renders a date-range filter', () => {
    renderPage()
    expect(screen.getByText('Desde')).toBeInTheDocument()
    expect(screen.getByText('Hasta')).toBeInTheDocument()
  })
})
