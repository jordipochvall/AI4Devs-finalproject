import { render, screen, fireEvent, act } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import { AxiosError } from 'axios'
import i18n from '../../shared/i18n/i18n'

const { generate } = vi.hoisted(() => ({ generate: vi.fn() }))
vi.mock('../api/operatorApi', () => ({ useGenerateRfj: () => ({ mutate: generate, isPending: false }) }))

import OperatorReportsPage from './OperatorReportsPage'

const renderPage = () => render(<MemoryRouter><OperatorReportsPage /></MemoryRouter>)

describe('OperatorReportsPage (HU-21)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => generate.mockReset())

  it('AC1: generating shows the aggregates and integrity status', () => {
    renderPage()
    fireEvent.click(screen.getByText('Generar informe'))

    expect(generate).toHaveBeenCalledTimes(1)
    const opts = generate.mock.calls[0][1] as { onSuccess: (r: unknown) => void }
    act(() => opts.onSuccess({
      operatorId: 1, periodFrom: '2026-05-01T00:00:00Z', periodTo: '2026-06-01T00:00:00Z',
      totalRounds: 40, activePlayers: 7, totalWageredCents: 100000, totalWonCents: 94000,
      ggrCents: 6000, integrityConsistent: true, integrityChecked: 40, generatedAt: '2026-06-01T00:00:00Z',
    }))

    expect(screen.getByText('40')).toBeInTheDocument()
    expect(screen.getByText(/íntegra/)).toBeInTheDocument()
  })

  it('AC3: a 422 (broken integrity) blocks generation with the offending round', () => {
    renderPage()
    fireEvent.click(screen.getByText('Generar informe'))

    const opts = generate.mock.calls[0][1] as { onError: (e: unknown) => void }
    act(() => opts.onError(new AxiosError('x', 'ERR', undefined, undefined,
      { status: 422, data: { firstBrokenRoundId: 33 } } as never)))

    expect(screen.getByText(/#33/)).toBeInTheDocument()
  })
})
