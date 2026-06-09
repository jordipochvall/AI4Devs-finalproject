import { render, screen, fireEvent, act } from '@testing-library/react'
import { describe, it, expect, beforeAll, beforeEach } from 'vitest'
import i18n from '../i18n/i18n'
import ComplianceBanner from './ComplianceBanner'
import { useSessionGuard, LOSS_THRESHOLD_CENTS } from './sessionGuardStore'

describe('ComplianceBanner', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => useSessionGuard.getState().reset())

  it('shows the DGOJ seal, +18 and the responsible gaming link (AC1)', () => {
    render(<ComplianceBanner />)
    expect(screen.getByText(/DGOJ/)).toBeInTheDocument()
    expect(screen.getByText('+18')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Juego responsable' })).toBeInTheDocument()
  })

  it('opens the responsible gaming dialog (AC2)', () => {
    render(<ComplianceBanner />)
    fireEvent.click(screen.getByRole('button', { name: 'Juego responsable' }))
    expect(screen.getByText(/entretenimiento/)).toBeInTheDocument()
  })

  it('shows the pause notice only when the loss threshold is exceeded (AC3)', () => {
    render(<ComplianceBanner />)
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()

    act(() => useSessionGuard.setState({ netLossCents: LOSS_THRESHOLD_CENTS }))
    expect(screen.getByRole('alert')).toBeInTheDocument()
  })
})
