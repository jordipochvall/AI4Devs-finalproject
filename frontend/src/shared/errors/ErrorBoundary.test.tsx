import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import '../i18n/i18n'
import { ErrorBoundary } from './ErrorBoundary'

function Bomb(): never {
  throw new Error('boom')
}

describe('ErrorBoundary (HU-39)', () => {
  it('renders its children when nothing fails', () => {
    render(
      <ErrorBoundary>
        <p>All good</p>
      </ErrorBoundary>,
    )
    expect(screen.getByText('All good')).toBeInTheDocument()
  })

  it('shows a recoverable message instead of a blank page when a child throws', () => {
    // React logs the error to the console by default; keep the test output clean.
    vi.spyOn(console, 'error').mockImplementation(() => {})

    render(
      <ErrorBoundary>
        <Bomb />
      </ErrorBoundary>,
    )

    expect(screen.getByRole('alert')).toBeInTheDocument()
    expect(screen.getByRole('button')).toBeInTheDocument()

    vi.restoreAllMocks()
  })
})
