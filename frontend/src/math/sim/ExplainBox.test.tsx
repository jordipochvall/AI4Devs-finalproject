import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import i18n from '../../shared/i18n/i18n'

// Behaviour of the mutate call, set per test; the mock forwards (variables, options) to it.
const h = vi.hoisted(() => ({
  behavior: (_vars: { simulationId: number; question: string }, _opts: { onSuccess: (r: { answer: string }) => void; onError: (e: unknown) => void }) => {},
}))

vi.mock('../api/mathApi', () => ({
  useExplain: () => ({ mutate: (vars: never, opts: never) => h.behavior(vars, opts), isPending: false }),
}))

import ExplainBox from './ExplainBox'

describe('ExplainBox', () => {
  beforeAll(async () => { await i18n.changeLanguage('es') })
  beforeEach(() => { h.behavior = () => {} })

  it('shows the AI answer and keeps the question in the thread (AC2)', () => {
    h.behavior = (_vars, opts) => opts.onSuccess({ answer: 'El retorno es saludable.' })
    render(<ExplainBox simulationId={1} />)

    fireEvent.change(screen.getByPlaceholderText(/Pregunta sobre/), { target: { value: '¿RTP ok?' } })
    fireEvent.click(screen.getByRole('button', { name: 'Preguntar' }))

    expect(screen.getByText(/¿RTP ok\?/)).toBeInTheDocument()
    expect(screen.getByText(/El retorno es saludable\./)).toBeInTheDocument()
  })

  it('disables itself with a notice when the AI is unavailable (503) (AC3)', () => {
    h.behavior = (_vars, opts) => opts.onError({ isAxiosError: true, response: { status: 503 } })
    render(<ExplainBox simulationId={1} />)

    fireEvent.change(screen.getByPlaceholderText(/Pregunta sobre/), { target: { value: 'algo' } })
    fireEvent.click(screen.getByRole('button', { name: 'Preguntar' }))

    expect(screen.getByText(/IA no disponible/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Preguntar' })).not.toBeInTheDocument()
  })
})
