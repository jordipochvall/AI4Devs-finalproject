import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import { AxiosError } from 'axios'
import i18n from '../../shared/i18n/i18n'

// mutate calls onError with a 422 AxiosError so we can assert the inline validation message.
const { mutate } = vi.hoisted(() => ({ mutate: vi.fn() }))

vi.mock('../api/operatorApi', () => ({
  useOperatorGames: () => ({
    data: [{
      id: 1, code: 'fruits-3x3', name: 'Frutas', theme: 'FRUITS',
      minBetCents: 100, maxBetCents: 10000, betStepCents: 100,
      allowedCurrencies: ['EUR'], active: true, activeConfigId: 5, paylineCount: 5,
    }],
    isLoading: false, isError: false,
  }),
  useUpdateGame: () => ({ mutate, isPending: false }),
}))

import OperatorGamesPage from './OperatorGamesPage'

const renderPage = () => render(<MemoryRouter><OperatorGamesPage /></MemoryRouter>)

describe('OperatorGamesPage (HU-15)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => mutate.mockReset())

  it('AC1: lists the operator games with editable commercial fields', () => {
    renderPage()
    expect(screen.getByText(/Frutas/)).toBeInTheDocument()
    expect(screen.getByText('5')).toBeInTheDocument() // payline count
    // Currencies field is pre-filled and editable.
    expect(screen.getByDisplayValue('EUR')).toBeInTheDocument()
    // Bet fields are editable inputs.
    expect(screen.getByDisplayValue('10000')).toBeInTheDocument()
  })

  it('AC2: a 422 on save shows the backend detail inline', () => {
    mutate.mockImplementation((_vars, opts) =>
      opts?.onError?.(new AxiosError('bad', 'ERR_BAD_REQUEST', undefined, undefined,
        { status: 422, data: { detail: 'Apuesta no múltiplo de líneas' } } as never)),
    )
    renderPage()
    fireEvent.click(screen.getByText('Guardar'))

    expect(mutate).toHaveBeenCalledTimes(1)
    expect(screen.getByText('Apuesta no múltiplo de líneas')).toBeInTheDocument()
  })

  it('saves the edited commercial config via the API', () => {
    renderPage()
    fireEvent.change(screen.getByLabelText(/Apuesta máx/), { target: { value: '20000' } })
    fireEvent.click(screen.getByText('Guardar'))

    expect(mutate).toHaveBeenCalledTimes(1)
    expect(mutate.mock.calls[0][0]).toMatchObject({ gameId: 1, payload: { maxBetCents: 20000, active: true } })
  })
})
