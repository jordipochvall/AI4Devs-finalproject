import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeAll, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

// Mock the data hooks so the test does not depend on the network.
vi.mock('../api/playerApi', () => ({
  useGames: () => ({
    data: [
      { id: 1, name: 'Egipcio', theme: 'EGYPTIAN', coverImageUrl: '/c.jpg', grid: { cols: 5, rows: 3 } },
      { id: 2, name: 'Frutas',  theme: 'FRUITS',   coverImageUrl: '/f.jpg', grid: { cols: 3, rows: 3 } },
    ],
    isLoading: false,
    isError: false,
  }),
  useWallet: () => ({ data: { balanceCents: 100000, currency: 'EUR' }, isLoading: false, isError: false }),
}))

import LobbyPage from './LobbyPage'

const renderLobby = () =>
  render(<MemoryRouter><LobbyPage /></MemoryRouter>)

describe('LobbyPage', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })

  it('shows the covers of the active games (AC1)', () => {
    renderLobby()
    expect(screen.getByText('Egipcio')).toBeInTheDocument()
    expect(screen.getByText('Frutas')).toBeInTheDocument()
  })

  it('shows the formatted balance in the header (AC2)', () => {
    renderLobby()
    // 100000 cents → "1.000,00 €" (the thousands separator depends on the environment's ICU)
    expect(screen.getByText(/1[.]?000,00/)).toBeInTheDocument()
  })

  it('translates the game theme', () => {
    renderLobby()
    expect(screen.getByText(/Egipcio · 5x3/)).toBeInTheDocument()
  })
})
