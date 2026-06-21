import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import i18n from '../shared/i18n/i18n'

const { create, setActive } = vi.hoisted(() => ({ create: vi.fn(), setActive: vi.fn() }))

vi.mock('./api/adminApi', () => ({
  useOperators: () => ({
    data: [{ id: 1, code: 'novacasino-default', name: 'NovaCasino', active: true, createdAt: '2026-01-01T00:00:00Z' }],
    isLoading: false,
  }),
  useCreateOperator: () => ({ mutate: create, isPending: false }),
  useSetOperatorActive: () => ({ mutate: setActive, isPending: false }),
}))

import AdminPage from './AdminPage'

const renderPage = () => render(<MemoryRouter><AdminPage /></MemoryRouter>)

describe('AdminPage (HU-25)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => { create.mockReset(); setActive.mockReset() })

  it('AC: lists operators', () => {
    renderPage()
    expect(screen.getByText('novacasino-default')).toBeInTheDocument()
    expect(screen.getByText('NovaCasino')).toBeInTheDocument()
  })

  it('AC1: submitting the form onboards an operator with its initial user', () => {
    renderPage()
    fireEvent.change(screen.getByLabelText('Código'), { target: { value: 'acme' } })
    fireEvent.change(screen.getByLabelText('Nombre'), { target: { value: 'ACME' } })
    fireEvent.change(screen.getByLabelText('Email del operador'), { target: { value: 'op@acme.test' } })
    fireEvent.change(screen.getByLabelText('Contraseña del operador'), { target: { value: 'Sup3rSecret!' } })
    fireEvent.click(screen.getByText('Crear operador'))

    expect(create).toHaveBeenCalledTimes(1)
    expect(create.mock.calls[0][0]).toMatchObject({
      code: 'acme', name: 'ACME', operatorEmail: 'op@acme.test', operatorPassword: 'Sup3rSecret!',
    })
  })

  it('AC3: toggling status deactivates the operator', () => {
    renderPage()
    fireEvent.click(screen.getByText('Desactivar'))
    expect(setActive).toHaveBeenCalledWith({ id: 1, active: false })
  })
})
