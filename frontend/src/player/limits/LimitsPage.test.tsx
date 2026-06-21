import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

const { setLimit, selfExclude } = vi.hoisted(() => ({ setLimit: vi.fn(), selfExclude: vi.fn() }))

vi.mock('../api/playerApi', () => ({
  useSetLimit: () => ({ mutate: setLimit, isPending: false }),
  useSelfExclude: () => ({ mutate: selfExclude, isPending: false }),
}))

import LimitsPage from './LimitsPage'

const renderPage = () => render(<MemoryRouter><LimitsPage /></MemoryRouter>)

describe('LimitsPage (HU-19)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => { setLimit.mockReset(); selfExclude.mockReset() })

  it('AC1: submitting a daily loss limit posts it in cents', () => {
    renderPage()
    fireEvent.change(screen.getByLabelText(/Pérdida máxima diaria/), { target: { value: '50' } })
    fireEvent.click(screen.getByText('Guardar límite'))
    expect(setLimit).toHaveBeenCalledTimes(1)
    expect(setLimit.mock.calls[0][0]).toEqual({ limitType: 'LOSS', period: 'DAILY', amountCents: 5000 })
  })

  it('self-exclusion posts the number of days', () => {
    renderPage()
    fireEvent.change(screen.getByLabelText(/Días de autoexclusión/), { target: { value: '7' } })
    fireEvent.click(screen.getByText('Autoexcluirme'))
    expect(selfExclude).toHaveBeenCalledTimes(1)
    expect(selfExclude.mock.calls[0][0]).toBe(7)
  })

  it('rejects an invalid amount client-side without calling the API', () => {
    renderPage()
    fireEvent.change(screen.getByLabelText(/Pérdida máxima diaria/), { target: { value: '-5' } })
    fireEvent.click(screen.getByText('Guardar límite'))
    expect(setLimit).not.toHaveBeenCalled()
    expect(screen.getByText(/importe válido/)).toBeInTheDocument()
  })
})
