import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

const { mutate } = vi.hoisted(() => ({ mutate: vi.fn() }))

vi.mock('../api/operatorApi', () => ({
  usePlayers: () => ({
    data: {
      content: [
        { id: 1, email: 'player1@nova.test', locale: 'es', active: true, balanceCents: 100000, currency: 'EUR' },
      ],
      page: 0, size: 10, totalElements: 1, totalPages: 1,
    },
    isLoading: false,
    isError: false,
  }),
  useRecharge: () => ({ mutate, isPending: false }),
}))

import OperatorPlayersPage from './OperatorPlayersPage'

const renderPage = () => render(<MemoryRouter><OperatorPlayersPage /></MemoryRouter>)

describe('OperatorPlayersPage', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => mutate.mockReset())

  it('shows the players with their balance (AC1)', () => {
    renderPage()
    expect(screen.getByText('player1@nova.test')).toBeInTheDocument()
    expect(screen.getByText(/1[.]?000,00/)).toBeInTheDocument()
  })

  it('the dialog validates amount > 0 on the client and does not call the server (AC2)', () => {
    renderPage()
    fireEvent.click(screen.getByText('Recargar'))
    fireEvent.change(screen.getByLabelText('Importe (€)'), { target: { value: '0' } })
    fireEvent.click(screen.getByText('Confirmar recarga'))

    expect(screen.getByText(/mayor que 0/)).toBeInTheDocument()
    expect(mutate).not.toHaveBeenCalled()
  })

  it('with a valid amount it invokes the recharge with an Idempotency-Key (AC3/AC4)', () => {
    renderPage()
    fireEvent.click(screen.getByText('Recargar'))
    fireEvent.change(screen.getByLabelText('Importe (€)'), { target: { value: '50' } })
    fireEvent.click(screen.getByText('Confirmar recarga'))

    expect(mutate).toHaveBeenCalledTimes(1)
    const args = mutate.mock.calls[0][0]
    expect(args).toMatchObject({ playerId: 1, amountCents: 5000 })
    expect(typeof args.idempotencyKey).toBe('string')
    expect(args.idempotencyKey.length).toBeGreaterThan(0)
  })
})
