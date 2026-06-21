import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, beforeAll, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

const { useIntegrity } = vi.hoisted(() => ({ useIntegrity: vi.fn() }))
vi.mock('../api/operatorApi', () => ({ useIntegrity }))

import IntegrityCheck from './IntegrityCheck'

describe('IntegrityCheck (HU-20)', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })

  it('shows an intact chain result', () => {
    useIntegrity.mockReturnValue({
      data: { from: '', to: '', checked: 12, consistent: true, firstBrokenRoundId: null },
      isLoading: false,
    })
    render(<IntegrityCheck />)
    fireEvent.click(screen.getByText('Verificar integridad'))
    expect(screen.getByText(/Cadena íntegra/)).toBeInTheDocument()
  })

  it('shows the first broken round when the chain is tampered', () => {
    useIntegrity.mockReturnValue({
      data: { from: '', to: '', checked: 5, consistent: false, firstBrokenRoundId: 42 },
      isLoading: false,
    })
    render(<IntegrityCheck />)
    fireEvent.click(screen.getByText('Verificar integridad'))
    expect(screen.getByText(/#42/)).toBeInTheDocument()
  })
})
