import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, beforeAll, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

// Mock the data hook so the dialog does not hit the network.
vi.mock('../api/operatorApi', () => ({
  useRoundDetail: () => ({
    data: {
      roundId: 90412, gameId: 3, playerId: 7, gameConfigId: 15,
      betCents: 100, winCents: 500, balancePreCents: 100000, balancePostCents: 100400,
      freeSpin: false, createdAt: '2026-05-04T09:12:33Z',
      view: [['B', 'A', 'B'], ['B', 'A', 'B'], ['B', 'A', 'B']],
      winningPaylines: [{ paylineIndex: 0, symbol: 'A', count: 3, winCents: 500 }],
      scatterCount: 0,
    },
    isLoading: false, isError: false,
  }),
}))

import RoundDetailDialog from './RoundDetailDialog'

describe('RoundDetailDialog', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })

  it('renders amounts, the symbol grid and the winning line', () => {
    render(<RoundDetailDialog roundId={90412} onClose={() => {}} />)

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByText(/Detalle de la partida #90412/)).toBeInTheDocument()
    expect(screen.getByText(/1,00/)).toBeInTheDocument()                  // bet 1,00 €
    expect(screen.getByText(/Línea 0: A ×3/)).toBeInTheDocument()         // winning line (incl. 5,00 €)
    expect(screen.getAllByText('A').length).toBe(3)                       // middle row A,A,A in the grid
  })

  it('calls onClose from the close button', () => {
    const onClose = vi.fn()
    render(<RoundDetailDialog roundId={90412} onClose={onClose} />)
    fireEvent.click(screen.getByRole('button', { name: 'Cerrar' }))
    expect(onClose).toHaveBeenCalled()
  })
})
